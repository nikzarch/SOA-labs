package ru.nikzarch.firstService.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HumanDto {
    @NotNull(message = "{validation.governor_age.required}")
    @Positive(message = "{validation.governor_age.positive}")
    private Integer age;
}
