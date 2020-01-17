package Catalog.Representation;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Offer {
    private  Long id;
    private String manufacter;
    private String product;
    private Long quantity;
    private double unitPrice;

    public Offer(){}

    public Offer(Long id,String manufacter, String product, Long quantity, double unitPrice) {
        this.id=id;
        this.manufacter = manufacter;
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }
    @JsonProperty
    public Long getId() {return id; }
    @JsonProperty
    public String getManufacter() {
        return manufacter;
    }
    @JsonProperty
    public String getProduct() {
        return product;
    }
    @JsonProperty
    public Long getQuantity() {
        return quantity;
    }
    @JsonProperty
    public double getUnitPrice() {
        return unitPrice;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
