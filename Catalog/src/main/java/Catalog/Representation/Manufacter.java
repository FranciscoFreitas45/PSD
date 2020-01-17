package Catalog.Representation;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Manufacter {
    private String name;
    private Set<Order> orders;
    private Set<Order> historic;

    public Manufacter(String name) {
        this.name = name;
        this.orders = new HashSet<>();
        this.historic=new HashSet<>();
    }

    public Manufacter(){

    }
    @JsonProperty
    public String getNome() {
        return name;
    }
    @JsonProperty
    public Set<Order> getOrdens() {
        return  new HashSet<>(orders);
    }
    @JsonProperty
    public Set<Order> getHistorico() {
        return  new HashSet<>(historic);
    }

    public void addOrder(Order o){
        long id = this.orders.size();
        o.setId(id);
        this.orders.add(o);
    }

    public void addOrderHistoric(Order o){
        this.historic.add(o);
    }

    public Order getOrder(Long id){
        Order order = null;
        Iterator<Order> it = this.orders.iterator();
        while(it.hasNext()){
             Order o = (Order)it.next();
            if(o.getid()==id)
                    return o;
        }
        return order;
    }
}


