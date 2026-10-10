package ru.nikzarch.firstService.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import ru.nikzarch.firstService.model.Climate;
import ru.nikzarch.firstService.model.Government;
import ru.nikzarch.firstService.model.StandardOfLiving;

@Setter
@Getter
public class CityCreateRequestDto {

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
    private Long population ;

    private Double metersAboveSeaLevel;

    private Climate climate;

    private Government government;

    private StandardOfLiving standardOfLiving;

    @Valid
    private HumanDto governor;

}
