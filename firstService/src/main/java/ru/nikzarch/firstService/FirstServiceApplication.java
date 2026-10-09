package ru.nikzarch.firstService;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import ru.nikzarch.firstService.error.ApiExceptionMapper;
import ru.nikzarch.firstService.error.ConstraintViolationMapper;
import ru.nikzarch.firstService.error.JsonProcessingExceptionMapper;
import ru.nikzarch.firstService.error.WebApplicationExceptionMapper;
import ru.nikzarch.firstService.web.CityController;

import java.util.Set;

@ApplicationPath("/")
public class FirstServiceApplication extends Application {
}
