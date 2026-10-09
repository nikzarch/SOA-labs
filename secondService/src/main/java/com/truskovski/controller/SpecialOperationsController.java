package com.truskovski.controller;

import com.truskovski.service.SpecialOperationsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController("route")
@RequiredArgsConstructor
public class SpecialOperationsController {

    private final RestTemplate restTemplate = new RestTemplate();

    private final SpecialOperationsService specialOperationsService;

    @Value("${additionalProps.firstUrl}")
    private String firstServiceUrl;

    @GetMapping("/calculate/to-largest")
    public ResponseEntity<?> routeToLargest() {
        return ResponseEntity.ok(
                specialOperationsService.calculateDistanceToACity(
                        restTemplate.getForEntity(
                                firstServiceUrl + "/largest",
                                String.class)
                )
        );
    }

    @GetMapping("/calculate/to-newest")
    public ResponseEntity<?> routeToNewest() {
        return ResponseEntity.ok(
                specialOperationsService.calculateDistanceToACity(
                        restTemplate.getForEntity(
                                firstServiceUrl + "/newest",
                                String.class)
                )
        );
    }
}
