package Catalog.Representation;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

public class Importer {
    private String name;
    private Map<String, Offer> offers;
    private Map<String,Offer> historic;

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
    public Map<String, Offer> getOffers() {
        return offers;
    }

    @JsonProperty
    public Map<String,Offer> getHistoric() {
        return historic;
    }



    public void addOffer(Offer o){

        Long idOrder = o.getIdOrder();
        Long idOffer = o.getId();
        Tuple<Long, Long> tuple = new Tuple<>(idOrder,idOffer);
        this.offers.put(tuple.toString(),o);
    }


    public void addOfferHistoric(Long idOffer,Long idOrder,String state){
        Tuple<Long, Long> tuple = new Tuple<>(idOrder,idOffer);
        Offer offer = this.offers.get(tuple.toString());
        offer.setState(Integer.parseInt(state));
        this.historic.put(tuple.toString(),offer);
        this.offers.remove(tuple.toString());

    }
}
