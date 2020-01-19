package Catalog.Representation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

public class Manufacturer {
    private String name;
    private Map<Long,Order> orders;
    private Map<Long,Order> historic;

    @JsonCreator
    public Manufacturer(String name) {
        this.name = name;
        this.orders = new HashMap<>();
        this.historic=new HashMap<>();
    }

    public Manufacturer(){

    }
    @JsonProperty
    public String getName() {
        return name;
    }
    @JsonProperty
    public Map<Long, Order> getOrders() {
        return  new HashMap<>(orders);
    }
    @JsonProperty
    public Map<Long,Order> getHistoric() {
        return  new HashMap<>(historic);
    }

    public void addOrder(Order o){
        this.orders.put(o.getid(),o);
    }


    public Order getOrder(Long id){
        return this.orders.get(id);

    }

    public void addOrderHistoric(Long id){
        Order order = this.orders.get(id);
        order.setState(0);
        this.historic.put(order.getid(),order);
        this.orders.remove(id);

    }

}


