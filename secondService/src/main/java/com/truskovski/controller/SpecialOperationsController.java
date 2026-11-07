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

    private final SpecialOperationsService  specialOperationsService;

    @GetMapping("/calculate/to-largest")
    public ResponseEntity<RouteLength> routeToLargest() {
        return specialOperationsService.calculateRoute("area");
    }

    @GetMapping("/calculate/to-newest")
    public ResponseEntity<RouteLength> routeToNewest() {
        return specialOperationsService.calculateRoute("creationDate");
    }
}
