package ru.nikzarch.firstService.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "cities")
@Getter
@Setter
@NoArgsConstructor
public class CityEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(name = "coordinate_x")
    private Integer coordinateX;

    @Column(name = "coordinate_y", nullable = false)
    private Double coordinateY;

    @Column(nullable = false)
    private Long area;

    @Column(nullable = false)
    private Long population;

    @Column(name = "meters_above_sea_level")
    private Double metersAboveSeaLevel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Climate climate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Government government;

    @Enumerated(EnumType.STRING)
    @Column(name = "standard_of_living", length = 32)
    private StandardOfLiving standardOfLiving;

    @Column(name = "governor_age", nullable = false)
    private Integer governorAge;

    @Column(name = "creation_date", updatable = false, nullable = false)
    private LocalDateTime creationDate = LocalDateTime.now();
}
