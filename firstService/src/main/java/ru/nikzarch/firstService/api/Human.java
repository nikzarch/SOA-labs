package ru.nikzarch.firstService.api;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Human {
    @NotNull @Positive
    private Integer age;

}
