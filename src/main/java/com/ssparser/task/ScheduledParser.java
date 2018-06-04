package com.ssparser.task;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import com.ssparser.domain.Apartment;
import com.ssparser.service.ApartmentValidatorService;
import com.ssparser.service.EmailService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Component
public class ScheduledParser {

    private static final Logger log = LoggerFactory.getLogger(ScheduledParser.class);
    private List<String> notifiedLinks = new ArrayList<String>();

    @Autowired
    private ApartmentValidatorService apartmentValidatorService;

    @Autowired
    private EmailService emailService;

    @Value("${spring.mail.to}")
    private String emailTo;

    @Scheduled(cron = "${ssparser.cronTime}")
    public void retreiveRSS() {
        log.info("Parser started!");

        URL feedUrl = null;
        try {
            feedUrl = new URL("https://www.ss.com/lv/real-estate/flats/rss/");
        } catch (MalformedURLException e) {
            log.error("Unable to parse URL: {}", e.getMessage());
        }
        //Retrieving feed
        SyndFeedInput input = new SyndFeedInput();
        try {
            SyndFeed feed = input.build(new XmlReader(feedUrl));
            log.debug(feed.getLink());

            List<SyndEntry> entries = feed.getEntries();

            // Retrieving HTML from each entry
            entries.forEach(entry -> {
                String url = entry.getLink();
                try {
                    Document doc = Jsoup.connect(url).get();

                    // Get headtitle to check if it includes Pardod
                    String headTitle = doc.getElementsByClass("headtitle").get(0).html();
                    boolean isForSale = headTitle.contains("Pārdod");
                    boolean isRiga = headTitle.contains("Rīga");

                    log.debug("Parsing the link: {}",url);

                    if (isForSale && isRiga) {

                        String region = doc.selectFirst("#tdo_20").child(0).html();
                        String city = doc.selectFirst("#tdo_856").child(0).html();
                        String rooms = doc.selectFirst("#tdo_1").html();
                        String buildingType = doc.selectFirst("#tdo_6").html();
                        String priceFull = doc.selectFirst("#tdo_8").html();
                        String floor = doc.selectFirst("#tdo_4").html().split("/")[0];
                        String squaredMeters = doc.selectFirst("#tdo_3").html();


                        // Get price per meter
                        String pricePerMeterString = "";
                        double pricePerMeter = 0;
                        int open = priceFull.indexOf("(");
                        int close = priceFull.indexOf("/m2");
                        if(open != -1 && close != -1){
                            pricePerMeterString = priceFull.substring(open + 1, close - 1);
                            pricePerMeterString = pricePerMeterString.replaceAll("\\s","");
                            pricePerMeter = Double.parseDouble(pricePerMeterString.trim());
                        }

                        // Get full price
                        String priceString = "";
                        double price = 0;
                        close = priceFull.indexOf("€");
                        if(close != -1){
                            priceString = priceFull.substring(0, close);
                            priceString = priceString.replaceAll("\\s","");
                            price = Double.parseDouble(priceString);
                        }

                        log.debug("Link: {}", url);
                        log.debug("Region: {}", region);
                        log.debug("City: {}", city);
                        log.debug("Rooms: {}", rooms);
                        log.debug("Type: {}", buildingType);
                        log.debug("Price Full: {}", priceFull);
                        log.debug("Price: {}", String.valueOf(price));
                        log.debug("Price per meter: {}", String.valueOf(pricePerMeter));


                        // create Apartment object
                        Apartment appartment = new Apartment();
                        appartment.setBuildingType(buildingType);
                        appartment.setRegion(region);
                        appartment.setCity(city);
                        appartment.setFloor(Double.valueOf(floor));
                        appartment.setLink(url);
                        appartment.setPrice(price);
                        appartment.setPricePerMeter(pricePerMeter);
                        appartment.setRooms(Double.valueOf(rooms));
                        appartment.setSquaredMeters(Double.valueOf(squaredMeters));

                        // ApartmentValidator checks
                        if (apartmentValidatorService.isApproved(appartment)) {
                            //send mail
                            emailService.sendSimpleMessage(emailTo, "SS alert", appartment.toString());
                        };
                        log.debug("Apartment validation completed");
                    }

                }
                catch (IOException e) {
                    log.error("IOException: {}", e.getMessage());
                }
            });


        } catch (FeedException e) {
            log.error("FeedException: {}", e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            log.error("IOException: {}", e.getMessage());
        }


    }

}
