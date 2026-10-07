package ru.nikzarch.firstService.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@AllArgsConstructor
@Getter
@Setter
public class HumanEntity {
    private Integer age;

    public HumanEntity() {
    }
}
