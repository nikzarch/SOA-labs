package ru.nikzarch.firstService.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class CityPageDto {
    private List<CityResponseDto> content = new ArrayList<>();

    private int page;

    private int pageSize;

    private long totalElements;

    private int totalPages;

}
