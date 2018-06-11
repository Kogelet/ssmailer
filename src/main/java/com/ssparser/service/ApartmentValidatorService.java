package com.ssparser.service;

import com.ssparser.domain.Apartment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class ApartmentValidatorService {

    private static final Logger log = LoggerFactory.getLogger(ApartmentValidatorService.class);

    private List<Apartment> validatedApartments = new ArrayList<Apartment>();

    @Value("${ssmailer.floorsException}")
    private double floorException;

    // Riga
    @Value("${ssmailer.riga.maxNumberRooms}")
    private String rigaRooms;

    @Value("${ssmailer.riga.pricePerMeter}")
    private String rigaPricePerMeter;

    @Value("${ssmailer.riga.buildingTypes}")
    private String rigaBuildingTypes;


    // Balozi
    @Value("${ssmailer.balozi.maxNumberRooms}")
    private String baloziRooms;

    @Value("${ssmailer.balozi.pricePerMeter}")
    private String baloziPricePerMeter;

    @Value("${ssmailer.balozi.buildingTypes}")
    private String baloziBuildinTypes;



    public boolean isApproved(Apartment a) {

        if (validatedApartments.contains(a)) return false;
         else {
             validatedApartments.add(a);
        }

        log.debug("Validated apartments: {}", validatedApartments);

        // if it's the 1 floor, return false
        if (a.getFloor() == floorException) return false;

        //checking Riga conditions
        if (a.getRegion().equals("Rīga")) {

            List rigaListBuildingTypes = Arrays.asList(rigaBuildingTypes.split(","));
            boolean isGoodType = rigaListBuildingTypes.contains(a.getBuildingType());

            boolean isGoodRoomNumber = (a.getRooms() <= Double.valueOf(rigaRooms));

            boolean isGoodPrice = (a.getPricePerMeter() <= Double.valueOf(rigaPricePerMeter));

            return (isGoodType && isGoodRoomNumber && isGoodPrice);
        }
        else if (a.getCity().equals("Baloži") || a.getCity().equals("Mārupes pag.")) {

            boolean isGoodType = baloziBuildinTypes.equals(a.getBuildingType());

            boolean isGoodRoomNumber = (a.getRooms() <= Double.valueOf(baloziRooms));

            boolean isGoodPrice = (a.getPricePerMeter() <= Double.valueOf(baloziPricePerMeter));

            return (isGoodType && isGoodRoomNumber && isGoodPrice);
        }
            else return false;
    }

}
