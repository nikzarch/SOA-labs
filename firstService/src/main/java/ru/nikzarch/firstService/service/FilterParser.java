package ru.nikzarch.firstService.service;

import jakarta.enterprise.context.ApplicationScoped;
import ru.nikzarch.firstService.error.ApiException;
import ru.nikzarch.firstService.error.ErrorCode;
import ru.nikzarch.firstService.model.Climate;
import ru.nikzarch.firstService.model.Government;
import ru.nikzarch.firstService.model.StandardOfLiving;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

@ApplicationScoped
public class FilterParser {
    private static final Set<String> COMPARISON_OPERATORS = Set.of("equals", "moreThan", "lessThan");
    private static final Set<String> EQUALS_ONLY = Set.of("equals");
    private static final Set<String> SPECIAL_TYPES = Set.of("EQUALS", "MIN", "MAX");
    private static final List<StandardOfLiving> LIVING_ORDER =
            List.of(StandardOfLiving.NIGHTMARE, StandardOfLiving.HIGH, StandardOfLiving.ULTRA_HIGH);

    private static final Map<String, Field> FIELDS = Map.ofEntries(
            Map.entry("id", new Field("c.id", Integer::valueOf, COMPARISON_OPERATORS)),
            Map.entry("name", new Field("c.name", Function.identity(), COMPARISON_OPERATORS)),
            Map.entry("coordinates.x", new Field("c.coordinateX", Integer::valueOf, COMPARISON_OPERATORS)),
            Map.entry("coordinates.y", new Field("c.coordinateY", Double::valueOf, COMPARISON_OPERATORS)),
            Map.entry("area", new Field("c.area", Long::valueOf, COMPARISON_OPERATORS)),
            Map.entry("population", new Field("c.population", Long::valueOf, COMPARISON_OPERATORS)),
            Map.entry("metersAboveSeaLevel", new Field("c.metersAboveSeaLevel", Double::valueOf, COMPARISON_OPERATORS)),
            Map.entry("climate", new Field("c.climate", Climate::valueOf, EQUALS_ONLY)),
            Map.entry("government", new Field("c.government", Government::valueOf, EQUALS_ONLY)),
            Map.entry("standardOfLiving", new Field("c.standardOfLiving", StandardOfLiving::valueOf, EQUALS_ONLY)),
            Map.entry("governor.age", new Field("c.governorAge", Integer::valueOf, COMPARISON_OPERATORS)),
            Map.entry("creationDate", new Field("c.creationDate", LocalDateTime::parse, COMPARISON_OPERATORS))
    );

    private static final Map<String, String> SPECIAL_FIELDS = Map.of(
            "name", "name",
            "area", "area",
            "standard-of-living", "standardOfLiving"
    );

    public List<ParsedFilter> parseFilters(List<String> rawFilters) {
        if (rawFilters == null || rawFilters.isEmpty()) {
            return List.of();
        }
        List<ParsedFilter> result = new ArrayList<>(rawFilters.size());
        for (String raw : rawFilters) {
            if (raw == null || raw.isBlank()) {
                throw ApiException.badRequest(ErrorCode.FILTER_INVALID);
            }

            String[] parts = raw.split(":", 3);
            if (parts.length != 3 || parts[0].isBlank() || parts[1].isBlank() || parts[2].isBlank()) {
                throw ApiException.badRequest(ErrorCode.FILTER_FORMAT);
            }
            result.add(parse(parts[0].trim(), parts[1].trim(), parts[2].trim()));
        }
        return List.copyOf(result);
    }

    private ParsedFilter parse(String name, String operator, String rawValue) {
        Field field = requireField(name);
        if (!field.operators().contains(operator)) {
            throw ApiException.badRequest(ErrorCode.FILTER_OPERATOR, operator, name);
        }
        return new ParsedFilter(field.path(), sqlOperator(operator), convert(field, name, rawValue));
    }

    public List<ParsedSort> parseSorts(List<String> orderedBy) {
        if (orderedBy == null || orderedBy.isEmpty()) {
            return List.of();
        }
        List<ParsedSort> result = new ArrayList<>(orderedBy.size());
        for (String raw : orderedBy) {
            if (raw == null || raw.isBlank()) {
                throw ApiException.badRequest(ErrorCode.SORT_INVALID);
            }
            String[] parts = raw.split(":", -1);
            if (parts.length > 2 || parts[0].isBlank()) {
                throw ApiException.badRequest(ErrorCode.SORT_FORMAT);
            }
            String fieldName = parts[0].trim();
            String direction = parts.length == 1 ? "ASC" : parts[1].trim().toUpperCase(java.util.Locale.ROOT);
            if (!direction.equals("ASC") && !direction.equals("DESC")) {
                throw ApiException.badRequest(ErrorCode.SORT_DIRECTION, direction);
            }
            result.add(new ParsedSort(requireField(fieldName).path(), direction));
        }
        return List.copyOf(result);
    }

    public ParsedSpecial parseSpecial(String parameter, String rawValue, String type) {
        String fieldName = parameter == null ? null : SPECIAL_FIELDS.get(parameter);

        if (fieldName == null) {
            throw ApiException.badRequest(
                    ErrorCode.SPECIAL_UNKNOWN_PARAMETER,
                    parameter
            );
        }

        if (rawValue == null || rawValue.isBlank()
                || type == null || !SPECIAL_TYPES.contains(type)) {
            throw ApiException.badRequest(ErrorCode.SPECIAL_INVALID);
        }

        Field field = requireField(fieldName);
        Object value = convert(field, parameter, rawValue);

        if ("EQUALS".equals(type)) {
            return new ParsedSpecial(
                    new ParsedFilter(field.path(), "=", value),
                    "c.id",
                    "ASC"
            );
        }

        if (value instanceof StandardOfLiving living) {
            int boundary = LIVING_ORDER.indexOf(living);

            List<StandardOfLiving> matching = new ArrayList<>();

            for (int i = 0; i < LIVING_ORDER.size(); i++) {
                boolean matches = "MIN".equals(type)
                        ? i > boundary
                        : i < boundary;

                if (matches) {
                    matching.add(LIVING_ORDER.get(i));
                }
            }

            if (matching.isEmpty()) {
                throw ApiException.notFound(
                        ErrorCode.MATCHING_CITY_NOT_FOUND
                );
            }

            return new ParsedSpecial(
                    new ParsedFilter(field.path(), "IN", matching),
                    "c.id",
                    "ASC"
            );
        }

        String operator = "MIN".equals(type) ? ">" : "<";

        return new ParsedSpecial(
                new ParsedFilter(field.path(), operator, value),
                "c.id",
                "ASC"
        );
    }

    private Field requireField(String name) {
        Field field = FIELDS.get(name);
        if (field == null) {
            throw ApiException.badRequest(ErrorCode.FILTER_UNKNOWN_FIELD, name);
        }
        return field;
    }

    private Object convert(Field field, String name, String value) {
        try {
            Object converted = field.convertFunction().apply(value);
            if (converted instanceof Double number && !Double.isFinite(number)) {
                throw new NumberFormatException();
            }
            return converted;
        } catch (IllegalArgumentException | DateTimeParseException ex) {
            throw ApiException.badRequest(ErrorCode.FILTER_VALUE, name, value);
        }
    }

    private String sqlOperator(String operation) {
        return switch (operation) {
            case "equals" -> "=";
            case "moreThan" -> ">";
            case "lessThan" -> "<";
            default -> throw new IllegalStateException("Unsupported internal operator: " + operation);
        };
    }

    private record Field(String path, Function<String, ?> convertFunction, Set<String> operators) {}

    public record ParsedFilter(String path, String operator, Object value) {}

    public record ParsedSort(String path, String direction) {}

    public record ParsedSpecial(ParsedFilter filter, String sortExpression, String direction) {}
}
