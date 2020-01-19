package Worker;

import org.zeromq.SocketType;
import tp.Messages;
import tp.Messages.*;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import org.zeromq.ZMQ;

import java.util.*;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

public class Worker {

    private Queue<Messages.ImporterOffer> offers;
    private Messages.ManufacturerOrder order;
    private ArrayList<Transaction> deliveries; //resultado
    ZMQ.Socket pull;
    ZMQ.Socket push;
    ZMQ.Socket sub;

    // este amigo tem um socket PULL, e SUB do frontend para ele, e um push dele para o frontend
    // recebe pelo sub :
    // recebe pelo pull : ordens do frontend
    // envia pelo push os resultados das ordens em curso

    public Worker(ZMQ.Socket pull, ZMQ.Socket push, ZMQ.Socket sub, Queue<Messages.ImporterOffer> offers, Messages.ManufacturerOrder order){
        this.pull = pull;
        this.push = push;
        this.sub = sub;
        this.offers = new PriorityQueue<Messages.ImporterOffer>(comparePrice);
        this.order = order;
        this.deliveries = new ArrayList<>();
    }

    public void main(String[] args) throws InvalidProtocolBufferException {
        ZMQ.Context context = ZMQ.context(1);
        ZMQ.Socket pull = context.socket(SocketType.PULL);
        ZMQ.Socket push = context.socket(SocketType.PUSH);
        ZMQ.Socket subSocket = context.socket(SocketType.SUB);
        pull.connect("tcp://localhost:" + 12347);
        push.connect("tcp://localhost:" + 12348);
        subSocket.connect("tcp://localhost:"+12346);

        Timer timer = new Timer();

        while (true) {

            byte[] order_received = pull.recv();
            Message m_order = Messages.Message.parseFrom(order_received);
            String key_send = "" + ((Messages.Message) m_order).getManufacturerOrder().getId() + "-" + ((Messages.Message) m_order).getManufacturerOrder().getProduct();
            subSocket.subscribe(key_send.getBytes());
            order = ((Messages.Message) m_order).getManufacturerOrder();

            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    // cancelar order
                    order = null;
                    order.newBuilderForType().setActive(0);
                    subSocket.unsubscribe(key_send.getBytes());
                    // enviar para historico de orders do catálogo
                    //Message m = createOrderCancelledResponse(order);
                    //push.send(m.toByteArray());
                    // notificar ?
                }
            }, 2000);

            String key_receive = subSocket.recvStr();
            byte[] offer_received = subSocket.recv();
            Message m_offer = Messages.Message.parseFrom(offer_received);
            //Offer offer = createOffer(m_offer);
            //this.offers.add(offer);
            /*
            for(Offer off: this.offers){
                if(off != null){
                    // enviar para o catalogo


                }
            }*/

            push.send("resultadinhos");
        }

    }


    private Comparator<Messages.ImporterOffer> comparePrice = (Messages.ImporterOffer o1, Messages.ImporterOffer o2) -> Double.compare(o1.getUnitPrice(), o2.getUnitPrice());

}