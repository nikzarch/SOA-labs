package ru.nikzarch.firstService.api;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CityResponse extends CityRequest {
    private Integer id;
    private String creationDate;

}
