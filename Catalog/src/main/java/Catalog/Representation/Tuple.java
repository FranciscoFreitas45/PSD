package Catalog.Representation;

import java.util.Objects;

public class Tuple<X, Y> {
    public final X idOrder;
    public final Y idOffer;
    public Tuple(X x, Y y) {
        this.idOrder = x;
        this.idOffer = y;
    }

    public X getIdOrder() {
        return idOrder;
    }

    public Y getIdOffer(){
        return idOffer;
    }

    @Override
    public String toString() {
        return ""+this.idOffer+"-"+this.idOrder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tuple)) return false;
        Tuple<?, ?> tuple = (Tuple<?, ?>) o;
        return Objects.equals(getIdOrder(), tuple.getIdOrder()) &&
                Objects.equals(getIdOffer(), tuple.getIdOffer());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getIdOrder(), getIdOffer());
    }
}