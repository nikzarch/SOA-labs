package ru.nikzarch.firstService.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
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

    @Embedded
    private CoordinatesEntity coordinates;

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

    @Embedded
    private HumanEntity governor;

    @Column(name = "creation_date", updatable = false)
    private LocalDateTime creationDate = LocalDateTime.now();
}
