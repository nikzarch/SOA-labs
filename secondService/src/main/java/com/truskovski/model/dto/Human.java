package com.truskovski.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;


public record Human(
        @NotNull @Positive
        Integer age
) { }
