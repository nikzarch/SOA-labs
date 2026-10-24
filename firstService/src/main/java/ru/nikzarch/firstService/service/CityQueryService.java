package ru.nikzarch.firstService.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;
import ru.nikzarch.firstService.dto.CityPageDto;
import ru.nikzarch.firstService.error.ApiException;
import ru.nikzarch.firstService.model.CityEntity;
import ru.nikzarch.firstService.model.StandardOfLiving;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@ApplicationScoped
public class CityQueryService {

    private static final Set<String> SPECIAL_PARAMETERS =
            Set.of("name", "area", "standard-of-living");

    private static final Set<String> SPECIAL_TYPES =
            Set.of("EQUALS", "MIN", "MAX");

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

        List<CityEntity> cities = executeCitiesQuery(
                whereJpql,
                orderByJpql,
                parsedFilters,
                page,
                pageSize
        );

        long total = countCities(whereJpql, parsedFilters);

        return createPageDto(cities, page, pageSize, total);
    }

    private List<CityEntity> executeCitiesQuery(
            String whereJpql,
            String orderByJpql,
            List<FilterParser.ParsedFilter> filters,
            int page,
            int pageSize
    ) {
        String jpql = "SELECT c FROM CityEntity c"
                + whereJpql
                + orderByJpql;

        TypedQuery<CityEntity> query =
                entityManager.createQuery(jpql, CityEntity.class);

        bindFilters(query, filters);

        return query
                .setFirstResult((page - 1) * pageSize)
                .setMaxResults(pageSize)
                .getResultList();
    }

    private long countCities(
            String whereJpql,
            List<FilterParser.ParsedFilter> filters
    ) {
        String jpql = "SELECT COUNT(c) FROM CityEntity c" + whereJpql;

        TypedQuery<Long> query =
                entityManager.createQuery(jpql, Long.class);

        bindFilters(query, filters);

        return query.getSingleResult();
    }

    private CityPageDto createPageDto(
            List<CityEntity> cities,
            int page,
            int pageSize,
            long total
    ) {
        CityPageDto result = new CityPageDto();

        result.setPage(page);
        result.setPageSize(pageSize);
        result.setTotalElements(total);
        result.setTotalPages(
                total == 0 ? 0 : (int) ((total + pageSize - 1L) / pageSize)
        );
        result.setContent(
                cities.stream()
                        .map(CityMapper::toResponse)
                        .toList()
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
                    ? parts[1].trim().toUpperCase()
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
    public CityEntity findByParameter(
            String parameter,
            String value,
            String type
    ) {
        validateSpecialFilter(parameter, value, type);

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<CityEntity> query =
                cb.createQuery(CityEntity.class);
        Root<CityEntity> root = query.from(CityEntity.class);

        Predicate predicate =
                buildSpecialPredicate(root, parameter, value, type, cb);

        query.select(root).where(predicate);
        applySpecialOrdering(query, root, parameter, type, cb);

        return entityManager.createQuery(query)
                .setMaxResults(1)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    private void validateSpecialFilter(
            String parameter,
            String value,
            String type
    ) {
        if (parameter == null || value == null
                || value.isEmpty() || type == null) {
            throw ApiException.badRequest(
                    "Некорректный параметр фильтрации"
            );
        }

        if (!SPECIAL_PARAMETERS.contains(parameter)) {
            throw ApiException.badRequest(
                    "Недопустимый parameter: " + parameter
            );
        }

        if (!SPECIAL_TYPES.contains(type)) {
            throw ApiException.badRequest(
                    "Недопустимый type: " + type
            );
        }
    }

    private Predicate buildSpecialPredicate(
            Root<CityEntity> root,
            String parameter,
            String value,
            String type,
            CriteriaBuilder cb
    ) {
        return switch (parameter) {
            case "name" ->
                    buildNamePredicate(root.get("name"), value, type, cb);
            case "area" ->
                    buildAreaPredicate(root.get("area"), value, type, cb);
            case "standard-of-living" ->
                    buildLivingPredicate(
                            root.get("standardOfLiving"), value, type, cb
                    );
            default -> throw new IllegalStateException(
                    "Неизвестный параметр: " + parameter
            );
        };
    }

    private void applySpecialOrdering(
            CriteriaQuery<CityEntity> query,
            Root<CityEntity> root,
            String parameter,
            String type,
            CriteriaBuilder cb
    ) {
        if ("EQUALS".equals(type)) {
            query.orderBy(cb.asc(root.get("id")));
            return;
        }

        Expression<?> sortExpression = switch (parameter) {
            case "name" -> root.get("name");
            case "area" -> root.get("area");
            case "standard-of-living" ->
                    buildLivingRankExpression(root, cb);
            default -> throw new IllegalStateException(
                    "Неизвестный параметр сортировки: " + parameter
            );
        };

        Order primaryOrder = "MIN".equals(type)
                ? cb.asc(sortExpression)
                : cb.desc(sortExpression);

        query.orderBy(primaryOrder, cb.asc(root.get("id")));
    }

    private Predicate buildNamePredicate(
            Path<String> path,
            String value,
            String type,
            CriteriaBuilder cb
    ) {
        return switch (type) {
            case "EQUALS" -> cb.equal(path, value);
            case "MIN" -> cb.greaterThanOrEqualTo(path, value);
            case "MAX" -> cb.lessThanOrEqualTo(path, value);
            default -> throw new IllegalStateException();
        };
    }

    private Predicate buildAreaPredicate(
            Path<Long> path,
            String value,
            String type,
            CriteriaBuilder cb
    ) {
        long number;

        try {
            number = Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw ApiException.badRequest(
                    "Значение area должно быть long"
            );
        }

        return switch (type) {
            case "EQUALS" -> cb.equal(path, number);
            case "MIN" -> cb.greaterThanOrEqualTo(path, number);
            case "MAX" -> cb.lessThanOrEqualTo(path, number);
            default -> throw new IllegalStateException();
        };
    }

    private Predicate buildLivingPredicate(
            Path<StandardOfLiving> path,
            String value,
            String type,
            CriteriaBuilder cb
    ) {
        StandardOfLiving target = parseStandardOfLiving(value);

        if ("EQUALS".equals(type)) {
            return cb.equal(path, target);
        }

        int targetRank = livingRank(target);
        List<StandardOfLiving> matchingValues = new ArrayList<>();

        for (StandardOfLiving item : StandardOfLiving.values()) {
            int rank = livingRank(item);

            if ("MIN".equals(type) && rank >= targetRank) {
                matchingValues.add(item);
            } else if ("MAX".equals(type) && rank <= targetRank) {
                matchingValues.add(item);
            }
        }

        return path.in(matchingValues);
    }

    private StandardOfLiving parseStandardOfLiving(String value) {
        try {
            return StandardOfLiving.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw ApiException.badRequest(
                    "Недопустимое значение standard-of-living: " + value
            );
        }
    }

    private Expression<Integer> buildLivingRankExpression(
            Root<CityEntity> root,
            CriteriaBuilder cb
    ) {
        Path<StandardOfLiving> path = root.get("standardOfLiving");

        return cb.<Integer>selectCase()
                .when(cb.equal(path, StandardOfLiving.NIGHTMARE), 0)
                .when(cb.equal(path, StandardOfLiving.HIGH), 1)
                .when(cb.equal(path, StandardOfLiving.ULTRA_HIGH), 2)
                .otherwise(3);
    }

    private int livingRank(StandardOfLiving value) {
        return switch (value) {
            case NIGHTMARE -> 0;
            case HIGH -> 1;
            case ULTRA_HIGH -> 2;
        };
    }


    private void validatePage(int page, int pageSize) {
        if (page < 1) {
            throw ApiException.badRequest(
                    "page должен быть не меньше 1"
            );
        }

        if (pageSize < 1 || pageSize > 100) {
            throw ApiException.badRequest(
                    "pageSize должен быть от 1 до 100"
            );
        }

        try {
            Math.multiplyExact(page - 1, pageSize);
        } catch (ArithmeticException ex) {
            throw ApiException.badRequest(
                    "Слишком большой номер страницы"
            );
        }
    }
}