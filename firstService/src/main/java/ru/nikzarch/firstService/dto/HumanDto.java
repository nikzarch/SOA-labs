package ru.nikzarch.firstService.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class HumanDto {
    @NotNull
    @Positive(message = "Возраст должен быть положительным")
    @Max(value = Integer.MAX_VALUE, message = "А куда мы пишем число которое не влезает в int")
    @Min(value = Integer.MIN_VALUE,message = "А куда мы пишем число которое не влезает в int")
    private Long age;

}
