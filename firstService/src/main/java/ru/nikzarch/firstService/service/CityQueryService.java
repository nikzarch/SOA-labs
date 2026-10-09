package ru.nikzarch.firstService.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;
import ru.nikzarch.firstService.dto.CityPageDto;
import ru.nikzarch.firstService.error.ApiException;
import ru.nikzarch.firstService.model.CityEntity;
import ru.nikzarch.firstService.model.StandardOfLiving;

import java.util.*;

@ApplicationScoped
public class CityQueryService {



    @PersistenceContext(unitName = "firstServicePU")
    private EntityManager entityManager;

    private final FilterParser filterParser = new FilterParser();

    @Transactional
    public CityPageDto findCities(
            int page,
            int pageSize,
            List<String> filters,
            List<String> orderedBy
    ) {
        validatePage(page, pageSize);

        List<FilterParser.ParsedFilter> parsedFilters =
                filterParser.parse(filters);

        String whereJpql = buildWhereJpql(parsedFilters);
        String orderByJpql = buildOrderByJpql(orderedBy);

        String dataJpql =
                "SELECT c FROM CityEntity c"
                        + whereJpql
                        + orderByJpql;

        TypedQuery<CityEntity> dataQuery =
                entityManager.createQuery(dataJpql, CityEntity.class);

        bindFilters(dataQuery, parsedFilters);

        List<CityEntity> entities = dataQuery
                .setFirstResult((page - 1) * pageSize)
                .setMaxResults(pageSize)
                .getResultList();

        String countJpql =
                "SELECT COUNT(c) FROM CityEntity c"
                        + whereJpql;

        TypedQuery<Long> countQuery =
                entityManager.createQuery(countJpql, Long.class);

        bindFilters(countQuery, parsedFilters);

        long total = countQuery.getSingleResult();

        CityPageDto result = new CityPageDto();
        result.setPage(page);
        result.setPageSize(pageSize);
        result.setTotalElements(total);
        result.setTotalPages(
                total == 0 ? 0 : (int) ((total + pageSize - 1L) / pageSize)
        );
        result.setContent(
                entities.stream().map(CityMapper::toResponse).toList()
        );

        return result;
    }

    private String buildWhereJpql(
            List<FilterParser.ParsedFilter> filters
    ) {
        if (filters.isEmpty()) {
            return "";
        }

        StringBuilder where = new StringBuilder(" WHERE ");

        for (int i = 0; i < filters.size(); i++) {
            if (i > 0) {
                where.append(" AND ");
            }

            FilterParser.ParsedFilter filter = filters.get(i);
            String parameter = "f" + i;

            where.append("(:")
                    .append(parameter)
                    .append(" IS NULL OR ")
                    .append(filter.path())
                    .append(" ")
                    .append(filter.operator())
                    .append(" :")
                    .append(parameter)
                    .append(")"); // (:f0 IS NULL OR c.area > :f0)
        }

        return where.toString();
    }

    private void bindFilters(
            Query query,
            List<FilterParser.ParsedFilter> filters
    ) {
        for (int i = 0; i < filters.size(); i++) {
            query.setParameter("f" + i, filters.get(i).value());
        }
    }

    private static final Map<String, String> ORDER_PATHS = Map.ofEntries(
            Map.entry("id", "c.id"),
            Map.entry("name", "c.name"),
            Map.entry("area", "c.area"),
            Map.entry("population", "c.population"),
            Map.entry("metersAboveSeaLevel", "c.metersAboveSeaLevel"),
            Map.entry("coordinates.x", "c.coordinates.x"),
            Map.entry("coordinates.y", "c.coordinates.y"),
            Map.entry("governor.age", "c.governor.age"),
            Map.entry("creationDate", "c.creationDate"),
            Map.entry("climate", "c.climate"),
            Map.entry("government", "c.government"),
            Map.entry("standardOfLiving", "c.standardOfLiving")
    );

    private String buildOrderByJpql(List<String> orderedBy) {
        if (orderedBy == null || orderedBy.isEmpty()) {
            return "";
        }

        List<String> orders = new ArrayList<>();

        for (String raw : orderedBy) {
            if (raw == null || raw.isBlank()) {
                throw ApiException.badRequest(
                        "Некорректный параметр сортировки"
                );
            }

            String[] parts = raw.split(":", 2);
            String field = parts[0].trim();
            String direction = parts.length == 2
                    ? parts[1].trim().toUpperCase(java.util.Locale.ROOT)
                    : "ASC";

            String path = ORDER_PATHS.get(field);

            if (path == null) {
                throw ApiException.badRequest(
                        "Неизвестное поле сортировки: " + field
                );
            }

            if (!direction.equals("ASC") && !direction.equals("DESC")) {
                throw ApiException.badRequest(
                        "Недопустимое направление сортировки: " + direction
                );
            }

            orders.add(path + " " + direction); // C.id DESC
        }

        return " ORDER BY " + String.join(", ", orders);
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
