package Worker;

import Catalog.Representation.*;
import protobuf.Messages;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import org.zeromq.ZMQ;

import java.util.*;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

public class Worker {

    private Queue<Offer> offers;
    private Queue<Order> orders;
    private ArrayList<Transaction> deliveries; //resultado
    ZMQ.Socket pull;
    ZMQ.Socket push;
    ZMQ.Socket sub;

    // este amigo tem um socket PULL, e SUB do frontend para ele, e um push dele para o frontend
    // recebe pelo sub :
    // recebe pelo pull : ordens do frontend
    // envia pelo push os resultados das ordens em curso

    public Worker(ZMQ.Socket pull, ZMQ.Socket push, ZMQ.Socket sub, Queue<Offer> offers, Queue<Order> orders){
        this.pull = pull;
        this.push = push;
        this.sub = sub;
        this.offers = new PriorityQueue<Offer>(comparePrice);
        this.orders = new ArrayDeque<Order>();
        this.deliveries = new ArrayList<>();
    }

    public void main(String[] args) throws InvalidProtocolBufferException {
        ZMQ.Context context = ZMQ.context(1);
        ZMQ.Socket pull = context.socket(ZMQ.PULL);
        ZMQ.Socket push = context.socket(ZMQ.PUSH);
        ZMQ.Socket subSocket = context.socket(ZMQ.SUB);
        pull.connect("tcp://localhost:" + 12347);
        push.connect("tcp://localhost:" + 12348);
        subSocket.connect("tcp://localhost:"+12346);

        Worker negotiator = populate(push,pull,sub,Integer.parseInt(args[3]));
        Timer timer = new Timer();

        while (true) {

            byte[] order_received = pull.recv();
            Message m_order = Messages.Message.parseFrom(order_received);
            Order order = createOrder(m_order);
            String key_send = "" + ((Messages.Message) m_order).getManufacturerOrder().getId() + "-" + ((Messages.Message) m_order).getManufacturerOrder().getProduct();
            subSocket.subscribe(key_send.getBytes());
            this.orders.add(order);

            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    // cancelar order
                    orders.remove(order);
                    order.setAcabou(0);
                    subSocket.unsubscribe(key_send.getBytes());
                    // enviar para historico de orders do catálogo
                    Message m = createOrderCancelledResponse(order);
                    push.send(m.toByteArray());
                    // notificar ?
                }
            }, 2000);

            String key_receive = subSocket.recvStr();
            byte[] offer_received = subSocket.recv();
            Message m_offer = Messages.Message.parseFrom(offer_received);
            Offer offer = createOffer(m_offer);
            this.offers.add(offer);

            for(Offer off: this.offers){
                if(off != null){
                    // enviar para o catalogo


                }
            }

            push.send("resultadinhos");
        }

    }


    public static Worker populate(ZMQ.Socket push , ZMQ.Socket pull, ZMQ.Socket sub, int id) {

        PriorityQueue<Offer> offers = new PriorityQueue<>();
        ArrayDeque<Order> orders = new ArrayDeque<>();

        switch(id){
            case 1:
                orders.add(new Order(Long.valueOf(1),"Intel","i7",Long.valueOf(10),Long.valueOf(100),5.99,Long.valueOf(5)));
                orders.add(new Order(Long.valueOf(1),"ARM","Cortex",Long.valueOf(10),Long.valueOf(100),5.99,Long.valueOf(5)));
            case 2:
                orders.add(new Order(Long.valueOf(1),"Intel","i5",Long.valueOf(10),Long.valueOf(100),5.99,Long.valueOf(5)));
                orders.add(new Order(Long.valueOf(1),"IBM","POWER9",Long.valueOf(10),Long.valueOf(100),5.99,Long.valueOf(5)));
                orders.add(new Order(Long.valueOf(1),"Dell","Frontera",Long.valueOf(10),Long.valueOf(100),5.99,Long.valueOf(5)));
            case 3:
                orders.add(new Order(Long.valueOf(1),"NVIDIA","Lassen",Long.valueOf(10),Long.valueOf(100),5.99,Long.valueOf(5)));
                orders.add(new Order(Long.valueOf(1),"NVIDIA","RTX 2080",Long.valueOf(10),Long.valueOf(100),5.99,Long.valueOf(5)));
            default: break;
        }

        return new Worker(pull, push, sub, offers, orders);
    }


    public Message createOrderCancelledResponse(Order o){
        Message msg = Messages.Message.newBuilder().setType("RESPONSE").build();
        return msg;
    }

    public static Order createOrder(Message m) {
        String user = ((Messages.Message) m).getUser().getUsername();
        long id = ((Messages.Message) m).getManufacturerOrder().getId();
        String manufacturer = ((Messages.Message) m).getManufacturerOrder().getManufacturer();
        String product = ((Messages.Message) m).getManufacturerOrder().getProduct();
        long min = ((Messages.Message) m).getManufacturerOrder().getMinQuantity();
        long max = ((Messages.Message) m).getManufacturerOrder().getMaxQuantity();
        double price = ((Messages.Message) m).getManufacturerOrder().getUnitPrice();
        int active = ((Messages.Message) m).getManufacturerOrder().getActive();
        long negotiation = ((Messages.Message) m).getManufacturerOrder().getNegotiation();

        return new Order(id,manufacturer,product,min,max,price,negotiation);
    }

    public static Offer createOffer(Message m){
        String user = ((Messages.Message) m).getUser().getUsername();
        long id = ((Messages.Message) m).getImporterOffer().getId();
        String manufacturer = ((Messages.Message) m).getImporterOffer().getManufacturer();
        String product = ((Messages.Message) m).getImporterOffer().getProduct();
        double price = ((Messages.Message) m).getImporterOffer().getUnitPrice();
        long quantity = ((Messages.Message) m).getImporterOffer().getQuantity();

        return new Offer(id,manufacturer,product,quantity,price);
    }

    private boolean offerValid(Offer offer, Order order) {
        return (order != null) && (offer != null) && (order.getUnitPrice() <= offer.getUnitPrice());
    }

    private Comparator<Offer> comparePrice = (Offer o1, Offer o2) -> Double.compare(o1.getUnitPrice(), o2.getUnitPrice());

}