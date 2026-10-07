package ru.nikzarch.firstService.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@AllArgsConstructor
public class CoordinatesEntity {
    private Integer x;
    private Double y;

    public CoordinatesEntity() {
    }

}
