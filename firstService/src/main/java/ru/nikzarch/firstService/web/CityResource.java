package ru.nikzarch.firstService.web;

import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import ru.nikzarch.firstService.dto.CityCreateRequestDto;
import ru.nikzarch.firstService.dto.CityPatchRequestDto;
import ru.nikzarch.firstService.dto.CityResponseDto;
import ru.nikzarch.firstService.dto.CityPageDto;
import ru.nikzarch.firstService.error.ApiException;
import ru.nikzarch.firstService.model.CityEntity;
import ru.nikzarch.firstService.service.CityMapper;
import ru.nikzarch.firstService.service.CityQueryService;
import ru.nikzarch.firstService.service.CityService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Path("/cities")
@Produces(MediaType.APPLICATION_JSON)
public class CityResource {
    @Inject
    CityService cityService;

    @Inject
    CityQueryService queryService;

    @Inject
    Validator validator;

    @GET
    public Response getCities(@QueryParam("page") String pageRaw,
                              @QueryParam("pageSize") String pageSizeRaw,
                              @QueryParam("filters") List<String> filters,
                              @QueryParam("orderedBy") List<String> orderedBy) {
        int page = parsePositiveInt(pageRaw, 1, "page", false);
        int pageSize = parsePositiveInt(pageSizeRaw, 20, "pageSize", false);
        CityPageDto result = queryService.findCities(page, pageSize, filters, orderedBy);
        return Response.ok(result).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createCity(CityCreateRequestDto request) {
        validate(request);
        CityResponseDto created = cityService.create(request);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @Path("/{id}")
    @GET
    public Response getCityById(@PathParam("id") String rawId) {
        int id = parsePositiveInt(rawId, 0, "id", true);
        return Response.ok(cityService.get(id)).build();
    }

    @Path("/{id}")
    @PATCH
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateCity(@PathParam("id") String rawId, CityPatchRequestDto request) {
        int id = parsePositiveInt(rawId, 0, "id", true);
        validate(request);
        return Response.ok(cityService.update(id, request)).build();
    }

    @Path("/{id}")
    @DELETE
    public Response deleteCity(@PathParam("id") String rawId) {
        int id = parsePositiveInt(rawId, 0, "id", true);
        cityService.delete(id);
        return Response.noContent().build();
    }

    @Path("/{parameter}/{value}")
    @POST
    public Response findByParameter(@PathParam("parameter") String parameter,
                                    @PathParam("value") String value,
                                    @QueryParam("type") String type) {
        CityEntity result = queryService.findByParameter(parameter, value, type);
        if (result == null) {
            throw ApiException.notFound("Объект с указанным параметром не найден");
        }
        return Response.ok(CityMapper.toResponse(result)).build();
    }

    private void validate(CityPatchRequestDto request) {
        if (request == null) {
            throw ApiException.badRequest("Тело запроса обязательно");
        }
        var violations = validator.validate(request);
        if (violations.isEmpty()) return;

        List<Map<String, String>> details = new ArrayList<>();
        for (ConstraintViolation<CityPatchRequestDto> violation : violations) {
            Map<String, String> detail = new LinkedHashMap<>();
            detail.put("field", violation.getPropertyPath().toString());
            detail.put("message", violation.getMessage());
            details.add(detail);
        }
        throw new ApiException(Response.Status.BAD_REQUEST.getStatusCode(), "Некорректный параметр объекта", details);
    }
    private void validate(CityCreateRequestDto request) {
        if (request == null) {
            throw ApiException.badRequest("Тело запроса обязательно");
        }
        var violations = validator.validate(request);
        if (violations.isEmpty()) return;

        List<Map<String, String>> details = new ArrayList<>();
        for (ConstraintViolation<CityCreateRequestDto> violation : violations) {
            Map<String, String> detail = new LinkedHashMap<>();
            detail.put("field", violation.getPropertyPath().toString());
            detail.put("message", violation.getMessage());
            details.add(detail);
        }
        throw new ApiException(Response.Status.BAD_REQUEST.getStatusCode(), "Некорректный параметр объекта", details);
    }

    private int parsePositiveInt(String raw, int defaultValue, String field, boolean required) {
        if (raw == null) {
            if (required) throw ApiException.badRequest("Параметр " + field + " обязателен");
            return defaultValue;
        }
        try {
            int value = Integer.parseInt(raw);
            if (value < 1) throw ApiException.badRequest(field + " должен быть не меньше 1");
            return value;
        } catch (NumberFormatException e) {
            throw ApiException.badRequest(field + " должен быть integer");
        }
    }
}
