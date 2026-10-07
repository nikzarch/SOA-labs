package ru.nikzarch.firstService.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;
import ru.nikzarch.firstService.api.CityPage;
import ru.nikzarch.firstService.error.ApiException;
import ru.nikzarch.firstService.model.CityEntity;
import ru.nikzarch.firstService.model.StandardOfLiving;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@ApplicationScoped
public class CityQueryService {
    @PersistenceContext(unitName = "firstServicePU")
    private EntityManager entityManager;

    private final FilterParser filterParser = new FilterParser();

    @Transactional
    public CityPage findCities(int page, int pageSize, List<String> filters, List<String> orderedBy) {
        validatePage(page, pageSize);

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<CityEntity> query = cb.createQuery(CityEntity.class);
        Root<CityEntity> root = query.from(CityEntity.class);
        List<Predicate> predicates = filterParser.parse(filters, root, cb);
        query.select(root);
        if (!predicates.isEmpty()) query.where(predicates.toArray(Predicate[]::new));
        query.orderBy(buildOrders(orderedBy, root, cb));

        List<CityEntity> entities = entityManager.createQuery(query)
                .setFirstResult(Math.multiplyExact(page - 1, pageSize))
                .setMaxResults(pageSize)
                .getResultList();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<CityEntity> countRoot = countQuery.from(CityEntity.class);
        List<Predicate> countPredicates = filterParser.parse(filters, countRoot, cb);
        countQuery.select(cb.count(countRoot));
        if (!countPredicates.isEmpty()) countQuery.where(countPredicates.toArray(Predicate[]::new));
        long total = entityManager.createQuery(countQuery).getSingleResult();

        CityPage result = new CityPage();
        result.setPage(page);
        result.setPageSize(pageSize);
        result.setTotalElements(total);
        result.setTotalPages(total == 0 ? 0 : (int) ((total + pageSize - 1L) / pageSize));
        result.setContent(entities.stream().map(CityMapper::toResponse).toList());
        return result;
    }

    @Transactional
    public CityEntity findByParameter(String parameter, String value, String type) {
        if (parameter == null || value == null || value.isEmpty() || type == null) {
            throw ApiException.badRequest("Некорректный параметр фильтрации");
        }
        if (!Set.of("name", "area", "standard-of-living").contains(parameter)) {
            throw ApiException.badRequest("Недопустимый parameter: " + parameter);
        }
        if (!Set.of("EQUALS", "MIN", "MAX").contains(type)) {
            throw ApiException.badRequest("Недопустимый type: " + type);
        }

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<CityEntity> query = cb.createQuery(CityEntity.class);
        Root<CityEntity> root = query.from(CityEntity.class);
        Predicate predicate = switch (parameter) {
            case "name" -> buildNameSpecial(root.get("name"), value, type, cb);
            case "area" -> buildAreaSpecial(root.get("area"), value, type, cb);
            case "standard-of-living" -> buildLivingSpecial(root.get("standardOfLiving"), value, type, cb);
            default -> throw new IllegalStateException("unreachable");
        };
        query.select(root).where(predicate).orderBy(cb.asc(root.get("id")));
        return entityManager.createQuery(query).setMaxResults(1).getResultStream().findFirst().orElse(null);
    }

    private Predicate buildNameSpecial(Path<String> path, String value, String type, CriteriaBuilder cb) {
        return switch (type) {
            case "EQUALS" -> cb.equal(path, value);
            case "MIN" -> cb.greaterThanOrEqualTo(path, value);
            case "MAX" -> cb.lessThanOrEqualTo(path, value);
            default -> throw new IllegalStateException();
        };
    }

    private Predicate buildAreaSpecial(Path<Long> path, String value, String type, CriteriaBuilder cb) {
        long number;
        try {
            number = Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw ApiException.badRequest("Значение area должно быть long");
        }
        return switch (type) {
            case "EQUALS" -> cb.equal(path, number);
            case "MIN" -> cb.greaterThanOrEqualTo(path, number);
            case "MAX" -> cb.lessThanOrEqualTo(path, number);
            default -> throw new IllegalStateException();
        };
    }

    private Predicate buildLivingSpecial(Path<StandardOfLiving> path, String value, String type, CriteriaBuilder cb) {
        StandardOfLiving target;
        try {
            target = StandardOfLiving.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw ApiException.badRequest("Недопустимое значение standard-of-living: " + value);
        }
        if ("EQUALS".equals(type)) return cb.equal(path, target);

        List<StandardOfLiving> values = new ArrayList<>();
        for (StandardOfLiving item : StandardOfLiving.values()) {
            int rank = livingRank(item);
            int targetRank = livingRank(target);
            if ("MIN".equals(type) && rank >= targetRank) values.add(item);
            if ("MAX".equals(type) && rank <= targetRank) values.add(item);
        }
        return path.in(values);
    }

    private int livingRank(StandardOfLiving value) {
        return switch (value) {
            case NIGHTMARE -> 0;
            case HIGH -> 1;
            case ULTRA_HIGH -> 2;
        };
    }

    private List<Order> buildOrders(List<String> orderedBy, Root<CityEntity> root, CriteriaBuilder cb) {
        List<Order> result = new ArrayList<>();
        Set<String> fields = new LinkedHashSet<>();
        if (orderedBy != null) {
            for (String raw : orderedBy) {
                if (raw == null || raw.isBlank()) throw ApiException.badRequest("Некорректный параметр сортировки");
                String[] parts = raw.split(":", -1);
                if (parts.length != 2 || parts[0].isBlank() || parts[1].isBlank()) {
                    throw ApiException.badRequest("Некорректный параметр сортировки: " + raw);
                }
                String field = parts[0];
                String direction = parts[1].toLowerCase(Locale.ROOT);
                if (!Set.of("asc", "desc").contains(direction)) {
                    throw ApiException.badRequest("Недопустимое направление сортировки: " + direction);
                }
                if (!fields.add(field)) {
                    throw ApiException.badRequest("Поле сортировки указано более одного раза: " + field);
                }
                Path<?> path = sortPath(root, field);
                result.add("asc".equals(direction) ? cb.asc(path) : cb.desc(path));
            }
        }
        if (!fields.contains("id")) result.add(cb.asc(root.get("id")));
        return result;
    }

    private Path<?> sortPath(Root<CityEntity> root, String field) {
        return switch (field) {
            case "id", "name", "area", "population", "metersAboveSeaLevel", "creationDate", "climate", "government", "standardOfLiving" -> root.get(field);
            case "coordinates.x" -> root.get("coordinates").get("x");
            case "coordinates.y" -> root.get("coordinates").get("y");
            case "governor.age" -> root.get("governor").get("age");
            default -> throw ApiException.badRequest("Неизвестное поле сортировки: " + field);
        };
    }

    private void validatePage(int page, int pageSize) {
        if (page < 1) throw ApiException.badRequest("page должен быть не меньше 1");
        if (pageSize < 1 || pageSize > 100) throw ApiException.badRequest("pageSize должен быть от 1 до 100");
        try {
            Math.multiplyExact(page - 1, pageSize);
        } catch (ArithmeticException ex) {
            throw ApiException.badRequest("Слишком большой номер страницы");
        }
    }
}
