package ru.nikzarch.firstService.error;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {
    @Override
    public Response toResponse(WebApplicationException exception) {
        Response.StatusType status = exception.getResponse().getStatusInfo();
        if (exception instanceof NotFoundException) {
            return json(status.getStatusCode(), ErrorResponse.of("Объект с указанным id не найден"));
        }
        if (status.getStatusCode() >= 400 && status.getStatusCode() < 500) {
            return json(status.getStatusCode(), ErrorResponse.of("Некорректные параметры запроса"));
        }
        return json(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), ErrorResponse.of("Внутренняя ошибка сервера"));
    }

    private Response json(int status, ErrorResponse entity) {
        return Response.status(status).type(MediaType.APPLICATION_JSON_TYPE).entity(entity).build();
    }
}
