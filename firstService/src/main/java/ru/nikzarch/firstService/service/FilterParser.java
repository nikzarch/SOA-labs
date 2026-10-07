package ru.nikzarch.firstService.service;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import ru.nikzarch.firstService.error.ApiException;
import ru.nikzarch.firstService.model.CityEntity;
import ru.nikzarch.firstService.model.Climate;
import ru.nikzarch.firstService.model.Government;
import ru.nikzarch.firstService.model.StandardOfLiving;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class FilterParser {
    public List<Predicate> parse(List<String> rawFilters, Root<CityEntity> root, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();
        if (rawFilters == null) return predicates;
        for (String raw : rawFilters) {
            if (raw == null || raw.isBlank()) {
                throw ApiException.badRequest("Некорректный параметр фильтрации");
            }
            predicates.add(parseOne(raw, root, cb));
        }
        return predicates;
    }

    private Predicate parseOne(String raw, Root<CityEntity> root, CriteriaBuilder cb) {
        String[] parts = raw.split(":", 3);
        if (parts.length != 3 || parts[0].isBlank() || parts[1].isBlank() || parts[2].isBlank()) {
            throw ApiException.badRequest("Некорректный параметр фильтрации");
        }
        String field = parts[0];
        String op = parts[1];
        String value = parts[2];

        return switch (field) {
            case "name" -> stringPredicate(root.get("name"), op, value, cb, field);
            case "area" -> longPredicate(root.get("area"), op, value, cb, field);
            case "population" -> longPredicate(root.get("population"), op, value, cb, field);
            case "metersAboveSeaLevel" -> doublePredicate(root.get("metersAboveSeaLevel"), op, value, cb, field);
            case "coordinates.x" -> intPredicate(root.get("coordinates").get("x"), op, value, cb, field);
            case "coordinates.y" -> doublePredicate(root.get("coordinates").get("y"), op, value, cb, field);
            case "governor.age" -> intPredicate(root.get("governor").get("age"), op, value, cb, field);
            case "creationDate" -> dateTimePredicate(root.get("creationDate"), op, value, cb, field);
            case "climate" -> enumPredicate(root.get("climate"), op, value, Climate.class, cb, field);
            case "government" -> enumPredicate(root.get("government"), op, value, Government.class, cb, field);
            case "standardOfLiving" -> enumPredicate(root.get("standardOfLiving"), op, value, StandardOfLiving.class, cb, field);
            default -> throw ApiException.badRequest("Неизвестное поле фильтра: " + field);
        };
    }

    private Predicate stringPredicate(Path<String> path, String op, String value, CriteriaBuilder cb, String field) {
        return switch (op) {
            case "equals" -> cb.equal(path, value);
            case "moreThan" -> cb.greaterThan(path, value);
            case "lessThan" -> cb.lessThan(path, value);
            default -> invalidOp(field, op);
        };
    }

    private Predicate intPredicate(Path<Integer> path, String op, String value, CriteriaBuilder cb, String field) {
        Integer parsed = parseInt(value, field);
        return switch (op) {
            case "equals" -> cb.equal(path, parsed);
            case "moreThan" -> cb.gt(path, parsed);
            case "lessThan" -> cb.lt(path, parsed);
            default -> invalidOp(field, op);
        };
    }

    private Predicate longPredicate(Path<Long> path, String op, String value, CriteriaBuilder cb, String field) {
        Long parsed = parseLong(value, field);
        return switch (op) {
            case "equals" -> cb.equal(path, parsed);
            case "moreThan" -> cb.gt(path, parsed);
            case "lessThan" -> cb.lt(path, parsed);
            default -> invalidOp(field, op);
        };
    }

    private Predicate doublePredicate(Path<Double> path, String op, String value, CriteriaBuilder cb, String field) {
        Double parsed = parseDouble(value, field);
        return switch (op) {
            case "equals" -> cb.equal(path, parsed);
            case "moreThan" -> cb.gt(path, parsed);
            case "lessThan" -> cb.lt(path, parsed);
            default -> invalidOp(field, op);
        };
    }

    private Predicate dateTimePredicate(Path<LocalDateTime> path, String op, String value, CriteriaBuilder cb, String field) {
        LocalDateTime parsed;
        try {
            parsed = LocalDateTime.parse(value);
        } catch (DateTimeParseException e) {
            throw ApiException.badRequest("Дата и время поля " + field + " должны быть в формате LocalDateTime");
        }
        return switch (op) {
            case "equals" -> cb.equal(path, parsed);
            case "moreThan" -> cb.greaterThan(path, parsed);
            case "lessThan" -> cb.lessThan(path, parsed);
            default -> invalidOp(field, op);
        };
    }

    private <E extends Enum<E>> Predicate enumPredicate(Path<E> path, String op, String value, Class<E> type,
                                                         CriteriaBuilder cb, String field) {
        if (!"equals".equals(op)) {
            throw ApiException.badRequest("Для поля " + field + " допустима только операция equals");
        }
        E parsed;
        try {
            parsed = Enum.valueOf(type, value);
        } catch (IllegalArgumentException e) {
            throw ApiException.badRequest("Недопустимое значение для поля " + field + ": " + value);
        }
        return cb.equal(path, parsed);
    }

    private Predicate invalidOp(String field, String op) {
        throw ApiException.badRequest("Недопустимая операция " + op + " для поля " + field);
    }

    private Integer parseInt(String value, String field) {
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            throw ApiException.badRequest("Значение поля " + field + " должно быть integer");
        }
    }

    private Long parseLong(String value, String field) {
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException e) {
            throw ApiException.badRequest("Значение поля " + field + " должно быть long");
        }
    }

    private Double parseDouble(String value, String field) {
        try {
            return Double.valueOf(value);
        } catch (NumberFormatException e) {
            throw ApiException.badRequest("Значение поля " + field + " должно быть double");
        }
    }
}
