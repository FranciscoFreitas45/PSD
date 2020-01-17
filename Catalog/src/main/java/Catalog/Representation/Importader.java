package Catalog.Representation;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Importader {
    private String name;
    private Set<Offer> historic;

    public Importader(String name) {
        this.name = name;
        this.historic = new HashSet<>();
    }

    public Importader(){}

    @JsonProperty
    public String getName() {
        return name;
    }

    @JsonProperty
    public Set<Offer> getOffers() {
        return historic;
    }

    public void addOffer(Offer o){
        this.historic.add(o);
    }

    public Offer getOrder(Long id){
        Offer offer = null;
        Iterator<Offer> it = this.historic.iterator();
        while(it.hasNext()){
            if(it.next().getId()==id)
                offer=it.next();
        }
        return offer;
    }
}
