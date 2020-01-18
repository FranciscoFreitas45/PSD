package Worker;

public class Transaction {

    private String product;
    private String manufacturer;
    private String importer;
    private long quantity;
    private double price;

    public Transaction(String p, String m, String i, long q, float price) {
        this.product = p;
        this.manufacturer = m;
        this.importer = i;
        this.quantity = q;
        this.price = price;
    }

    public String getProduct() {
        return product;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public String getImporter() {
        return importer;
    }

    public long getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }


}