package Catalog.Representation;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

public class Importer {
    private String name;
    private Map<Long,Offer> offers;
    private Map<Long,Offer> historic;

    public Importer(String name) {
        this.name = name;
        this.historic = new HashMap<>();
        this.offers = new HashMap<>();
    }

    public Importer(){}

    @JsonProperty
    public String getName() {
        return name;
    }

    @JsonProperty
    public Map<Long,Offer> getOffers() {
        return offers;
    }

    @JsonProperty
    public Map<Long,Offer> getHistoric() {
        return historic;
    }



    public void addOffer(Offer o){
        this.offers.put(o.getId(),o);
    }

    public Offer getOffer(Long id){
        return this.offers.get(id);

    }

    public void addOfferHistoric(Long id){
        Offer offer = this.offers.get(id);
        this.historic.put(offer.getId(),offer);
        this.offers.remove(id);

    }
}
