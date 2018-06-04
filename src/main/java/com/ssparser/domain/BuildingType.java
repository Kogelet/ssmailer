package com.ssparser.domain;

public enum BuildingType {
    T103("103."),
    T104("104."),
    T119("119."),
    T467("467."),
    T602("602."),
    NEW("Jaun.");

    private String type;

    BuildingType(String type) {
        this.type = type;
    }
}
