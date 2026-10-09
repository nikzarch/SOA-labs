package ru.nikzarch.firstService.service;

import ru.nikzarch.firstService.error.ApiException;
import ru.nikzarch.firstService.model.Climate;
import ru.nikzarch.firstService.model.Government;
import ru.nikzarch.firstService.model.StandardOfLiving;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class FilterParser {

    public List<ParsedFilter> parse(List<String> rawFilters) {
        List<ParsedFilter> result = new ArrayList<>();

        if (rawFilters == null) {
            return result;
        }

        for (String raw : rawFilters) {
            if (raw == null || raw.isBlank()) {
                throw ApiException.badRequest(
                        "Некорректный параметр фильтрации"
                );
            }

            String[] parts = raw.split(":", 3);

            if (parts.length != 3
                    || parts[0].isBlank()
                    || parts[1].isBlank()
                    || parts[2].isBlank()) {
                throw ApiException.badRequest(
                        "Некорректный параметр фильтрации"
                );
            }

            result.add(parseOne(
                    parts[0],
                    parts[1],
                    parts[2]
            ));
        }

        return result;
    }

    private ParsedFilter parseOne(
            String field,
            String op,
            String value
    ) {
        return switch (field) {
            case "name" ->
                    new ParsedFilter(
                            "c.name",
                            operator(op, field),
                            value
                    );

            case "area" ->
                    new ParsedFilter(
                            "c.area",
                            operator(op, field),
                            parseLong(value, field)
                    );

            case "population" ->
                    new ParsedFilter(
                            "c.population",
                            operator(op, field),
                            parseLong(value, field)
                    );

            case "metersAboveSeaLevel" ->
                    new ParsedFilter(
                            "c.metersAboveSeaLevel",
                            operator(op, field),
                            parseDouble(value, field)
                    );

            case "coordinates.x" ->
                    new ParsedFilter(
                            "c.coordinates.x",
                            operator(op, field),
                            parseInt(value, field)
                    );

            case "coordinates.y" ->
                    new ParsedFilter(
                            "c.coordinates.y",
                            operator(op, field),
                            parseDouble(value, field)
                    );

            case "governor.age" ->
                    new ParsedFilter(
                            "c.governor.age",
                            operator(op, field),
                            parseInt(value, field)
                    );

            case "creationDate" ->
                    new ParsedFilter(
                            "c.creationDate",
                            operator(op, field),
                            parseDateTime(value, field)
                    );

            case "climate" ->
                    enumFilter(
                            "c.climate", field, op, value, Climate.class
                    );

            case "government" ->
                    enumFilter(
                            "c.government", field, op, value, Government.class
                    );

            case "standardOfLiving" ->
                    enumFilter(
                            "c.standardOfLiving",
                            field,
                            op,
                            value,
                            StandardOfLiving.class
                    );

            default -> throw ApiException.badRequest(
                    "Неизвестное поле фильтра: " + field
            );
        };
    }

    private String operator(String op, String field) {
        return switch (op) {
            case "equals" -> "=";
            case "moreThan" -> ">";
            case "lessThan" -> "<";
            default -> throw ApiException.badRequest(
                    "Недопустимая операция " + op + " для поля " + field
            );
        };
    }

    private ParsedFilter enumFilter(
            String path,
            String field,
            String op,
            String value,
            Class<? extends Enum<?>> type
    ) {
        if (!"equals".equals(op)) {
            throw ApiException.badRequest(
                    "Для поля " + field
                            + " допустима только операция equals"
            );
        }

        try {
            Enum<?> parsed = Enum.valueOf(
                    type.asSubclass(Enum.class),
                    value
            );

            return new ParsedFilter(path, "=", parsed);
        } catch (IllegalArgumentException e) {
            throw ApiException.badRequest(
                    "Недопустимое значение для поля "
                            + field + ": " + value
            );
        }
    }

    private Integer parseInt(String value, String field) {
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            throw ApiException.badRequest(
                    "Значение поля " + field + " должно быть integer"
            );
        }
    }

    private Long parseLong(String value, String field) {
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException e) {
            throw ApiException.badRequest(
                    "Значение поля " + field + " должно быть long"
            );
        }
    }

    private Double parseDouble(String value, String field) {
        try {
            return Double.valueOf(value);
        } catch (NumberFormatException e) {
            throw ApiException.badRequest(
                    "Значение поля " + field + " должно быть double"
            );
        }
    }

    private LocalDateTime parseDateTime(
            String value,
            String field
    ) {
        try {
            return LocalDateTime.parse(value);
        } catch (DateTimeParseException e) {
            throw ApiException.badRequest(
                    "Дата и время поля " + field
                            + " должны быть в формате LocalDateTime"
            );
        }
    }

    public record ParsedFilter(
            String path,
            String operator,
            Object value
    ) {}
}