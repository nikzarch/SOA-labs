package ru.nikzarch.firstService.error;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import ru.nikzarch.firstService.dto.ErrorResponse;

import java.util.List;
import java.util.Map;

@Provider
public class JsonProcessingExceptionMapper
        implements ExceptionMapper<ProcessingException> {

    @Override
    public Response toResponse(ProcessingException exception) {
        Throwable cause = exception;

        while (cause != null) {
            String message = cause.getMessage();

            if (cause instanceof IllegalArgumentException
                    && message != null
                    && message.startsWith("No enum constant ")) {

                String enumError = message.substring(
                        "No enum constant ".length()
                );

                int lastDot = enumError.lastIndexOf('.');

                if (lastDot > 0) {
                    String enumClass = enumError.substring(0, lastDot);
                    String invalidValue = enumError.substring(lastDot + 1);

                    String fieldName = switch (enumClass) {
                        case "ru.nikzarch.firstService.model.StandardOfLiving" ->
                                "standardOfLiving";
                        case "ru.nikzarch.firstService.model.Climate" ->
                                "climate";
                        case "ru.nikzarch.firstService.model.Government" ->
                                "government";
                        default -> "unknown";
                    };

                    return badRequest(Map.of(
                            fieldName,
                            "Недопустимое значение '" + invalidValue + "'"
                    ));
                }
            }

            cause = cause.getCause();
        }

        return badRequest(Map.of());
    }

    private Response badRequest(Map<String, String> details) {
        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(new ErrorResponse("Некорректный запрос", List.of(details)))
                .build();
    }
}