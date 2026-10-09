package ru.nikzarch.firstService.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CoordinatesDto {
    @Max(value = Integer.MAX_VALUE, message = "А куда мы пишем число которое не влезает в int")
    @Min(value = Integer.MIN_VALUE,message = "А куда мы пишем число которое не влезает в int")
    private Long x;

    @NotNull
    @DecimalMin(value = "-594", inclusive = false, message = "Координата y должна быть больше -594")
    @DecimalMax(value = "1.7976931348623157e+308", message = "А куда мы пишем число которое не влезает в Double и как это ваще произошло")
    private Double y;
}
