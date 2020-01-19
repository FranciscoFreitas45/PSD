package Worker;

import org.zeromq.SocketType;
import tp.Messages;
import tp.Messages.*;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import org.zeromq.ZMQ;

import java.nio.file.LinkOption;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

public class Worker extends Thread {

    private Queue<ImporterOffer> offers;
    private ManufacturerOrder order;
    ZMQ.Socket pull;
    ZMQ.Socket sub;
    private Timer timer;
    private int len_Key;

    // este amigo tem um socket PULL, e SUB do frontend para ele, e um push dele para o frontend
    // recebe pelo sub :
    // recebe pelo pull : ordens do frontend
    // envia pelo push os resultados das ordens em curso

    public Worker(ZMQ.Socket pull, ZMQ.Socket sub) {
        this.pull = pull;
        this.sub = sub;
        this.offers = new PriorityQueue<>(comparePrice);
        this.order = null;
        this.timer = new Timer();
        this.len_Key = 0;


    }


    public synchronized void addOffers(byte[] offer) throws InvalidProtocolBufferException {
        Messages.Message m;
        byte[] recv_real = new byte[offer.length - len_Key];
        System.arraycopy(offer, len_Key, recv_real, 0, offer.length - len_Key);
        m = tp.Messages.Message.parseFrom(recv_real);
        tp.Messages.ImporterOffer off = m.getImporterOffer();
        this.offers.add(off);

    }


    public void run() {
        byte[] recv;
        Messages.Message m = null;
        try {
            while ((recv = pull.recv()) == null) {
                m = Messages.Message.parseFrom(recv);
                Messages.ManufacturerOrder manu = m.getManufacturerOrder();
                System.out.println(manu.toString());
                String key = String.valueOf(manu.getId()) + ":";
                sub.subscribe(key.getBytes());
                this.len_Key = key.getBytes().length;
                //bloquear isto
                sleep(this.order.getNegotiation() * 1000);
                satisfyOffer();


            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void satisfyOffer(){
         //filtro por unitPrice;
        Queue<ImporterOffer> importerOffers = new PriorityQueue<>();
         this.offers.stream().filter(c->c.getUnitPrice() >this.order.getUnitPrice()).forEach(importerOffers::add);

        if(isQuantityEnough(importerOffers)){
            long MaxquantityOrder = order.getMaxQuantity();
            long sumQuantity=0;
            while(MaxquantityOrder > sumQuantity && importerOffers.size()>0){
                ImporterOffer offer = importerOffers.poll();
                if(offer!= null) {
                    sumQuantity = offer.getQuantity();
                    // notificar gajo
                }
            }

        }
        else {
            // cancelar order e offers
        }


    }

    public boolean isQuantityEnough(Queue<ImporterOffer> offers){
        Long MinquantityOrder = order.getMinQuantity();
        Long quantity = offers.stream().mapToLong(ImporterOffer::getQuantity).sum();

        return MinquantityOrder < quantity;
    }



    private Comparator<Messages.ImporterOffer> comparePrice = Comparator.comparingDouble((ImporterOffer o) -> o.getUnitPrice() * o.getQuantity());


}