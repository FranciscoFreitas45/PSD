package Worker;

import tp.Messages;
import tp.Messages.*;
import com.google.protobuf.InvalidProtocolBufferException;
import org.zeromq.ZMQ;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;


public class PullManufacturer extends Thread {

    private Queue<ImporterOffer> offers;
    private ManufacturerOrder order;
    ZMQ.Socket pull;
    ZMQ.Socket sub;
    ZMQ.Socket push;
    private Timer timer;
    private int len_Key;

    public PullManufacturer(ZMQ.Socket pull, ZMQ.Socket sub,ZMQ.Socket push) {
        this.pull = pull;
        this.sub = sub;
        this.push = push;
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
            while ((recv = pull.recv()) != null) {
                    System.out.println("RECEBI ORDEM");
                m = Messages.Message.parseFrom(recv);
                Messages.ManufacturerOrder manu = m.getManufacturerOrder();
                System.out.println(manu.toString());
                String key = String.valueOf(manu.getId()) + ":";
                sub.subscribe(key.getBytes());
                this.len_Key = key.getBytes().length;
                this.order = manu;
                //FAZER POST
                sleep(this.order.getNegotiation() * 1000);
                satisfyOffer();


            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void satisfyOffer(){
        Messages.Reply.Builder message = createReply();
        Messages.ImporterOffer offerImporter;
        //filtro por unitPrice;
        Queue<ImporterOffer> importerOffers = new PriorityQueue<>();
        this.offers.stream().filter(c->c.getUnitPrice() >this.order.getUnitPrice()).forEach(importerOffers::add);

        if(isQuantityEnough(importerOffers)){
            long MaxquantityOrder = order.getMaxQuantity();
            long sumQuantity=0;
            while(MaxquantityOrder > sumQuantity && !importerOffers.isEmpty()){
                ImporterOffer offer = importerOffers.poll();
                if(offer!= null) {
                    sumQuantity += offer.getQuantity();
                    if(sumQuantity > MaxquantityOrder){//caso ultrapassa o maximo ignora
                        sumQuantity -=offer.getQuantity();
                        offerImporter=setState(offer,2);
                        message.addOffers(offerImporter);
                    }
                    else{
                        offerImporter =setState(offer,1);
                        message.addOffers(offerImporter);
                    }
                                 }
            }
            while(!importerOffers.isEmpty()){
                ImporterOffer offer = importerOffers.poll();
                if(offer!= null) {
                    offerImporter=setState(offer,2);
                    message.addOffers(offerImporter);
                }
            }

        }
        else {
            while(!importerOffers.isEmpty()){
                ImporterOffer offer = importerOffers.poll();
                if(offer!= null) {
                    offerImporter =setState(offer,2);
                    message.addOffers(offerImporter);
                }
            }
        }

        Reply r = message.build();
        Messages.Message reply = Messages.Message.newBuilder().setReply(r).build();
        push.send(reply.toByteArray());

    }

    public boolean isQuantityEnough(Queue<ImporterOffer> offers){
        Long MinquantityOrder = order.getMinQuantity();
        Long quantity = offers.stream().mapToLong(ImporterOffer::getQuantity).sum();

        return MinquantityOrder < quantity;
    }


    public Messages.ImporterOffer  setState(ImporterOffer o , int state){
        Messages.ImporterOffer offer = Messages.ImporterOffer.newBuilder()
                                               .setId(o.getId())
                                                .setImporter(o.getImporter())
                                                .setProduct(o.getProduct())
                                                .setQuantity(o.getQuantity())
                                                .setUnitPrice(o.getUnitPrice())
                                                .setIdorder(order.getId())
                                                .setStateValue(state).build();
        return offer;
    }

    public Messages.Reply.Builder createReply(){
        Messages.Reply.Builder r = Messages.Reply.newBuilder()
                .setId(order.getId())
                .setManufacturer(order.getManufacturer())
                .setProduct(order.getProduct())
                .setResValue(0)
                .setProfit((long)100.0);
        return r;

    }
    private Comparator<Messages.ImporterOffer> comparePrice = Comparator.comparingDouble((ImporterOffer o) -> o.getUnitPrice() * o.getQuantity());


}