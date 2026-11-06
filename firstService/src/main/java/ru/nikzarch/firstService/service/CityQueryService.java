package ru.nikzarch.firstService.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import ru.nikzarch.firstService.dto.CityPageDto;
import ru.nikzarch.firstService.error.ApiException;
import ru.nikzarch.firstService.error.ErrorCode;
import ru.nikzarch.firstService.model.CityEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class CityQueryService {
    @PersistenceContext(unitName = "firstServicePU")
    EntityManager entityManager;

    @Inject
    FilterParser parser;

    @Inject
    CityMapper mapper;

    @Transactional
    public CityPageDto findCities(int page, int pageSize, List<String> filters, List<String> orderedBy) {
        int offset = pageOffset(page, pageSize);
        List<FilterParser.ParsedFilter> parsed = parser.parseFilters(filters);
        List<FilterParser.ParsedSort> sorts = parser.parseSorts(orderedBy);

        String where = whereClause(parsed);
        String orderBy = orderByClause(sorts);
        TypedQuery<CityEntity> dataQuery = entityManager.createQuery(
                "SELECT c FROM CityEntity c" + where + orderBy, CityEntity.class);
        bind(dataQuery, parsed);
        List<CityEntity> cities = dataQuery.setFirstResult(offset).setMaxResults(pageSize).getResultList();

        TypedQuery<Long> countQuery = entityManager.createQuery(
                "SELECT COUNT(c) FROM CityEntity c" + where, Long.class);
        bind(countQuery, parsed);
        long total = countQuery.getSingleResult();

        CityPageDto result = new CityPageDto();
        result.setContent(cities.stream().map(mapper::toResponse).toList());
        result.setPage(page);
        result.setPageSize(pageSize);
        result.setTotalElements(total);
        result.setTotalPages(total == 0 ? 0
                : (int) ((total - 1) / pageSize + 1));
        return result;
    }

    @Transactional
    public CityEntity findByParameter(String parameter, String value, String type) {
        FilterParser.ParsedSpecial special = parser.parseSpecial(parameter, value, type);
        List<FilterParser.ParsedFilter> filters = List.of(special.filter());
        String jpql =
                "SELECT c FROM CityEntity c"
                        + whereClause(filters)
                        + " ORDER BY "
                        + special.sortExpression()
                        + " "
                        + special.direction()
                        + ", c.id ASC";
        TypedQuery<CityEntity> query = entityManager.createQuery(jpql, CityEntity.class);
        bind(query, filters);
        return query.setMaxResults(1).getResultStream().findFirst().orElse(null);
    }

    @Transactional
    public CityEntity findByParameterSpecial(String parameter, String value, String type) {
        return Optional.ofNullable(findByParameter(parameter,value,type)).orElseThrow(() -> ApiException.notFound(ErrorCode.MATCHING_CITY_NOT_FOUND));
    }

    private String whereClause(List<FilterParser.ParsedFilter> filters) {
        if (filters.isEmpty()) {
            return "";
        }
        List<String> conditions = new ArrayList<>(filters.size());
        for (int i = 0; i < filters.size(); i++) {
            FilterParser.ParsedFilter filter = filters.get(i);
            conditions.add(filter.path() + " " + filter.operator() + " :f" + i);
        }
        return " WHERE " + String.join(" AND ", conditions);
    }

    private String orderByClause(List<FilterParser.ParsedSort> sorts) {
        if (sorts.isEmpty()) {
            return " ORDER BY c.id ASC";
        }
        List<String> parts = new ArrayList<>(sorts.size() + 1);
        for (FilterParser.ParsedSort sort : sorts) {
            parts.add(sort.path() + " " + sort.direction());
        }
        if (sorts.stream().noneMatch(sort -> sort.path().equals("c.id"))) {
            parts.add("c.id ASC");
        }
        return " ORDER BY " + String.join(", ", parts);
    }

    private void bind(Query query, List<FilterParser.ParsedFilter> filters) {
        for (int i = 0; i < filters.size(); i++) {
            query.setParameter("f" + i, filters.get(i).value());
        }
    }

    private int pageOffset(int page, int pageSize) {
        if (page < 1) {
            throw ApiException.badRequest(ErrorCode.PARAM_POSITIVE, "page");
        }
        if (pageSize < 1 || pageSize > 100) {
            throw ApiException.badRequest(ErrorCode.PAGE_SIZE_RANGE);
        }
        try {
            return Math.multiplyExact(page - 1, pageSize);
        } catch (ArithmeticException ex) {
            throw ApiException.badRequest(ErrorCode.PAGE_OVERFLOW);
        }
    }
}
