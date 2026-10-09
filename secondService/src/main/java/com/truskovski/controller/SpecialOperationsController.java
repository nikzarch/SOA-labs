package com.truskovski.controller;

import com.truskovski.service.SpecialOperationsService;
import com.truskovski.controller.exceptions.CitesEmptyException;
import com.truskovski.controller.exceptions.UnreachableServiceException;
import com.truskovski.model.dto.CityPage;
import com.truskovski.model.dto.RouteLength;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/route")
@RequiredArgsConstructor
public class SpecialOperationsController {

    private final RestTemplate restTemplate;

    private final SpecialOperationsService specialOperationsService;

    @Value("${additionalProps.firstUrl}")
    private String firstServiceUrl;

    @GetMapping("/calculate/to-largest")
    public ResponseEntity<RouteLength> routeToLargest() {
        return calculateRoute("area");
    }

    @GetMapping("/calculate/to-newest")
    public ResponseEntity<RouteLength> routeToNewest() {
        return calculateRoute("creationDate");
    }

    private ResponseEntity<RouteLength> calculateRoute(String sortField) {
        CityPage page;
        try {
            var uri = UriComponentsBuilder.fromUriString(firstServiceUrl)
                    .queryParam("page", 1)
                    .queryParam("pageSize", 1)
                    .queryParam("orderedBy", sortField + ":desc")
                    .queryParam("orderedBy", "id:asc")
                    .build().encode().toUri();
            page = restTemplate.getForObject(uri, CityPage.class);
        } catch (RestClientException | IllegalArgumentException exception) {
            throw new UnreachableServiceException("Первый веб-сервис недоступен", exception);
        }
        if (page == null || page.content() == null) {
            throw new UnreachableServiceException("Первый веб-сервис вернул некорректный ответ");
        }
        if (page.content().isEmpty()) {
            throw new CitesEmptyException("В коллекции отсутствуют города");
        }
        var city = page.content().getFirst();
        double length = specialOperationsService.calculateDistanceToACity(city.coordinates());
        return ResponseEntity.ok(new RouteLength(length));
    }
}
