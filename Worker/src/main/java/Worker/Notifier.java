package Worker;

import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import tp.Messages;

public class Notifier {
    private ZMQ.Socket pub;

    public Notifier(ZMQ.Context context){
        pub = context.socket(SocketType.PUB);
        pub.connect("tcp://localhost:12350");
    }

    public synchronized void notify(Messages.ManufacturerOrder order){

        StringBuilder st = new StringBuilder();
        st.append("NEW ORDER FROM " + order.getManufacturer() + " ");
        st.append("PRODUCT: " + order.getProduct());

        String res = order.getManufacturer() + "|";

        pub.send(res + st.toString());
    }

    public synchronized void notify(Messages.ImporterOffer offer){
        StringBuilder st = new StringBuilder();
        st.append("NEW OFFER Nº:" + offer.getIdorder() + " ");
        st.append("Product: " + offer.getProduct() + " ");
        st.append("Price per unit: " + offer.getUnitPrice());

        String res = "ORDER" + offer.getIdorder() + "|";

        pub.send(res + st.toString());
    }
}
