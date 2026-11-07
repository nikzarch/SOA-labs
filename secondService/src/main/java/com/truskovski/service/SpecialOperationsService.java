package com.truskovski.service;

import com.truskovski.controller.exceptions.CitesEmptyException;
import com.truskovski.controller.exceptions.UnreachableServiceException;
import com.truskovski.model.dto.CityPage;
import com.truskovski.model.dto.Coordinates;
import com.truskovski.model.dto.RouteLength;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class SpecialOperationsService {

    @Value("${additionalProps.firstUrl}")
    private String firstServiceUrl;

    private final RestTemplate restTemplate;

    public Double calculateDistanceToACity(Coordinates coordinates) {
        Double INITIAL_Y = 0d;
        Double INITIAL_X = 0d;
        return Math.sqrt(
                Math.pow(coordinates.x() - INITIAL_X, 2) +
                        Math.pow(coordinates.y() - INITIAL_Y, 2)
        );
    }

    public ResponseEntity<RouteLength> calculateRoute(String sortField) {
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
            throw new UnreachableServiceException("error.first-service.unavailable", exception);
        }
        if (page == null || page.content() == null) {
            throw new UnreachableServiceException("error.first-service.invalid-response");
        }
        if (page.content().isEmpty()) {
            throw new CitesEmptyException("error.cities.empty");
        }
        var city = page.content().getFirst();
        double length = calculateDistanceToACity(city.coordinates());
        return ResponseEntity.ok(new RouteLength(length));
    }
}
