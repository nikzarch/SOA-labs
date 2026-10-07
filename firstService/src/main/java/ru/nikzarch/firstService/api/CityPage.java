package ru.nikzarch.firstService.api;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class CityPage {
    private List<CityResponse> content = new ArrayList<>();
    private int page;
    private int pageSize;
    private long totalElements;
    private int totalPages;

}
