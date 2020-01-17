package Catalog.Representation;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Order {
    private Long id;
    private String manufacturer;
    private String product;
    private Long minQuantity;
    private Long maxQuantity;
    private double unitPrice;
    private Integer active ;
    private Long negotiation;


    public Order() {
        // Jackson deserialization
    }


    public Order(Long id,String manufacturer, String product, Long minQuantity, Long maxQuanity, double unitPrice,Long negotiation) {
        this.id=id;
        this.manufacturer = manufacturer;
        this.product = product;
        this.minQuantity = minQuantity;
        this.maxQuantity = maxQuanity;
        this.unitPrice = unitPrice;
        this.negotiation=negotiation;
        this.active= 0;
    }
    @JsonProperty
    public String getManufacturer() {
        return manufacturer;
    }
    @JsonProperty
    public Long getMinQuantity() {
        return minQuantity;
    }
    @JsonProperty
    public Long getMaxQuantity() {
        return maxQuantity;
    }
    @JsonProperty
    public double getUnitPrice() {
        return unitPrice;
    }

    @JsonProperty
    public String getProduct() {
        return product;
    }

    @JsonProperty
    public Long getid() {
        return id;
    }
    @JsonProperty
    public Integer getActive(){
        return active;
    }
    @JsonProperty
    public Long getNegotiation() {
        return negotiation;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setAcabou(Integer active){
        this.active=active;
    }



}
