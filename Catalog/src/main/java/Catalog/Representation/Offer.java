package Catalog.Representation;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Offer {
    private  Long id;
    private String manufacter;
    private String product;
    private Long quantity;
    private double unitPrice;
    private Long idOrder;
    private int  state;

    public Offer(){}

    public Offer(Long id,String manufacter, String product, Long quantity, double unitPrice,Long idOrder) {
        this.id=id;
        this.manufacter = manufacter;
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.idOrder=idOrder;
        this.state = 0;
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

    @JsonProperty
    public Long getIdOrder() { return idOrder; }

    @JsonProperty
    public Integer getState(){ return state; }


    public void setId(Long id) {
        this.id = id;
    }
}
