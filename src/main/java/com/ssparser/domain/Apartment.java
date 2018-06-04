package com.ssparser.domain;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class Apartment {

    private String link;

    private String region;

    private String city;

    private double rooms;

    private String buildingType;

    private double price;

    private double pricePerMeter;

    private double squaredMeters;

    private double floor;

}
