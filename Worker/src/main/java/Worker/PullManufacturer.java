package Worker;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.simple.JSONObject;
import tp.Messages;
import tp.Messages.*;
import com.google.protobuf.InvalidProtocolBufferException;
import org.zeromq.ZMQ;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;


public class PullManufacturer extends Thread {

    private Queue<ImporterOffer> offers;
    private ManufacturerOrder order;
    ZMQ.Socket pull;
    ZMQ.Socket sub;
    ZMQ.Socket push;
    private Timer timer;
    private int len_Key;
    private Notifier note;

    public PullManufacturer(ZMQ.Socket pull, ZMQ.Socket sub,ZMQ.Socket push,Notifier note) {
        this.pull = pull;
        this.sub = sub;
        this.push = push;
        this.offers = new PriorityQueue<>(comparePrice);
        this.order = null;
        this.timer = new Timer();
        this.len_Key = 0;
        this.note = note;


    }

    public synchronized void addOffers(byte[] offer) throws IOException {
        Messages.Message m;
        byte[] recv_real = new byte[offer.length - len_Key];
        System.arraycopy(offer, len_Key, recv_real, 0, offer.length - len_Key);
        m = tp.Messages.Message.parseFrom(recv_real);
        tp.Messages.ImporterOffer off = m.getImporterOffer();
        this.offers.add(off);
        String jsonInputString = createJsonOffer(off,"0");
        postAPI("http://localhost:8080/importer/offer/"+off.getImporter(),jsonInputString);
        note.notify(off);
    }


    public void run() {
        byte[] recv;
        Messages.Message m = null;
        try {
            while ((recv = pull.recv()) != null) {
                m = Messages.Message.parseFrom(recv);
                Messages.ManufacturerOrder manu = m.getManufacturerOrder();
                note.notify(manu);
                String key = String.valueOf(manu.getId()) + ":";
                sub.subscribe(key.getBytes());
                this.len_Key = key.getBytes().length;
                this.order = manu;
                String jsonInputString = createJsonOrder("1");
                postAPI("http://localhost:8080/manufacturer/order/"+order.getManufacturer(),jsonInputString);
                sleep(this.order.getNegotiation() * 1000);
                Reply r = satisfyOffer();
                sub.unsubscribe(key.getBytes());
                Messages.Message reply = Messages.Message.newBuilder().setReply(r).build();
                push.send(reply.toByteArray());
                putAPI("http://localhost:8080/manufacturer/historic/",order.getManufacturer(),Long.toString(order.getId()),null,null);



            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Reply satisfyOffer() throws IOException {
        Messages.Reply.Builder message = createReply();
        Messages.ImporterOffer offerImporter;
        if(isQuantityEnough(this.offers)){

            long MaxquantityOrder = order.getMaxQuantity();
            long sumQuantity=0;
            while(MaxquantityOrder > sumQuantity && !this.offers.isEmpty()){
                ImporterOffer offer = this.offers.poll();

                if(offer!= null) {
                    sumQuantity += offer.getQuantity();

                    if(sumQuantity > MaxquantityOrder){//caso ultrapassa o maximo ignora

                        sumQuantity -=offer.getQuantity();
                        offerImporter=setState(offer,2);
                        putAPI("http://localhost:8080/importer/historic/",offer.getImporter(),Long.toString(offer.getId()),Long.toString(order.getId()),"2");

                        message.addOffers(offerImporter);
                    }
                    else{
                        offerImporter =setState(offer,1);
                        putAPI("http://localhost:8080/importer/historic/",offer.getImporter(),Long.toString(offer.getId()),Long.toString(order.getId()),"1");

                        message.addOffers(offerImporter);
                        message.setResValue(1);
                    }
                }
            }
            while(!this.offers.isEmpty()){
                ImporterOffer offer = this.offers.poll();
                if(offer!= null) {
                    offerImporter=setState(offer,2);
                    putAPI("http://localhost:8080/importer/historic/",offer.getImporter(),Long.toString(offer.getId()),Long.toString(order.getId()),"2");

                    message.addOffers(offerImporter);
                }
            }

        }
        else {
            while(!this.offers.isEmpty()){
                ImporterOffer offer = this.offers.poll();
                if(offer!= null) {
                    offerImporter =setState(offer,2);
                    putAPI("http://localhost:8080/importer/historic/",offer.getImporter(),Long.toString(offer.getId()),Long.toString(order.getId()),"2");
                    message.addOffers(offerImporter);
                    message.setResValue(0);
                }
            }
        }

        Reply r = message.build();
        return r;

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


    private void postAPI(String path,String json) throws IOException {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        try {
            HttpPost request = new HttpPost(path);
            StringEntity params = new StringEntity(json);
            request.addHeader("content-type", "application/json");
            request.setEntity(params);
            httpClient.execute(request);

        } catch (Exception ex) {
        } finally {
            httpClient.close();
        }

    }
    private void putAPI(String path,String name,String idOffer, String idOrder,String state) throws IOException {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        String pathPut;
        if(idOrder!=null)
        pathPut = path+name+"/"+idOffer+"/"+idOrder+"/"+state;
        else
        pathPut = path+name+"/"+idOffer;


        HttpPut request = new HttpPut(pathPut);
        httpClient.execute(request);
    }



    private String  createJsonOrder(String  state){
        JSONObject json = new JSONObject();
        json.put("id",""+this.order.getId());
        json.put("manufacturer",this.order.getManufacturer());
        json.put("Product",this.order.getProduct());
        json.put("minQuantity",""+this.order.getMinQuantity());
        json.put("maxQuantity",""+this.order.getMaxQuantity());
        json.put("unitPrice",""+this.order.getUnitPrice());
        json.put("negotiation",""+this.order.getNegotiation());
        json.put("state",state);

    return json.toString();
    }

    private String createJsonOffer(ImporterOffer offer, String state){
        JSONObject json = new JSONObject();
        json.put("id",""+offer.getId());
        json.put("importer",offer.getImporter());
        json.put("product",offer.getProduct());
        json.put("quantity",""+offer.getQuantity());
        json.put("unitPrice",""+offer.getUnitPrice());
        json.put("idOrder",""+offer.getIdorder());
        json.put("state",state);
        return json.toString();
    }



    private Comparator<Messages.ImporterOffer> comparePrice = Comparator.comparingDouble((ImporterOffer o) -> o.getUnitPrice() * o.getQuantity());


}