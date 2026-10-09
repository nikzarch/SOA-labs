package ru.nikzarch.firstService.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import ru.nikzarch.firstService.model.Climate;
import ru.nikzarch.firstService.model.Government;
import ru.nikzarch.firstService.model.StandardOfLiving;

@Setter
@Getter
public class CityPatchRequestDto {

    @Size(min = 1, message = "Название должно состоять хотя бы из одного символа")
    private String name;

    @Valid
    private CoordinatesDto coordinates;

    @Positive(message = "Площадь должно быть положительным числом")
    @Max(value = Long.MAX_VALUE, message = "А куда мы пишем число которое не влезает в Long")
    @Min(value = Long.MIN_VALUE, message = "А куда мы пишем число которое не влезает в Long")
    private Long area;

    @Positive(message = "Население должно быть положительным числом")
    @Max(value = Long.MAX_VALUE, message = "А куда мы пишем число которое не влезает в Long")
    @Min(value = Long.MIN_VALUE, message = "А куда мы пишем число которое не влезает в Long")
    private Long population;

    private Double metersAboveSeaLevel;

    private Climate climate;

    private Government government;

    private StandardOfLiving standardOfLiving;

    @Valid
    private HumanDto governor;


    @Getter
    @Setter
    public static class CoordinatesDto {
        @Max(value = Integer.MAX_VALUE, message = "А куда мы пишем число которое не влезает в int")
        private Long x;

        @DecimalMin(value = "-594", inclusive = false, message = "Координата y должна быть больше -594")
        @DecimalMax(value = "1.7976931348623157e+308", message = "А куда мы пишем число которое не влезает в Double и как это ваще произошло")
        private Double y;
    }


}
