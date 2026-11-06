package ru.nikzarch.firstService.web;

import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;
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
import ru.nikzarch.firstService.dto.CityPageDto;
import ru.nikzarch.firstService.error.ApiException;
import ru.nikzarch.firstService.error.ErrorCode;
import ru.nikzarch.firstService.model.CityEntity;
import ru.nikzarch.firstService.service.CityMapper;
import ru.nikzarch.firstService.service.CityQueryService;
import ru.nikzarch.firstService.service.CityService;

import java.util.List;

@Path("/cities")
@Produces(MediaType.APPLICATION_JSON)
public class CityResource {
    @Inject
    CityService cityService;

    @Inject
    CityQueryService queryService;

    @Inject
    RequestValidator requestValidator;

    @Inject
    CityMapper mapper;

    @GET
    public Response getCities(@QueryParam("page") String pageRaw,
                              @QueryParam("pageSize") String pageSizeRaw,
                              @QueryParam("filters") List<String> filters,
                              @QueryParam("orderedBy") List<String> orderedBy) {
        int page = requestValidator.validateAndParsePositiveInteger(pageRaw, 1, "page", false);
        int pageSize = requestValidator.validateAndParsePositiveInteger(pageSizeRaw, 20, "pageSize", false);
        CityPageDto result = queryService.findCities(page, pageSize, filters, orderedBy);
        return Response.ok(result).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createCity(CityCreateRequestDto request) {
        requestValidator.validate(request);
        return Response.status(Response.Status.CREATED).entity(cityService.create(request)).build();
    }

    @Path("/{id}")
    @GET
    public Response getCityById(@PathParam("id") String rawId) {
        int id = requestValidator.validateAndParsePositiveInteger(rawId, 0, "id", true);
        return Response.ok(cityService.get(id)).build();
    }

    @Path("/{id}")
    @PATCH
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateCity(@PathParam("id") String rawId,
                               CityPatchRequestDto request) {
        int id = requestValidator.validateAndParsePositiveInteger(rawId, 0, "id", true);
        requestValidator.validate(request);
        return Response.ok(cityService.update(id, request)).build();
    }

    @Path("/{id}")
    @DELETE
    public Response deleteCity(@PathParam("id") String rawId) {
        int id = requestValidator.validateAndParsePositiveInteger(rawId, 0, "id", true);
        cityService.delete(id);
        return Response.noContent().build();
    }

    @Path("/{parameter}/{value}")
    @POST
    public Response findByParameter(@PathParam("parameter") String parameter,
                                    @PathParam("value") String value,
                                    @QueryParam("type") String type) {
        requestValidator.validateSpecialParam(parameter,value);
        CityEntity result = queryService.findByParameterSpecial(parameter, value, type);
        return Response.ok(mapper.toResponse(result)).build();
    }


}
