package ru.nikzarch.firstService.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class HumanEntity {
    private Integer age;

}
