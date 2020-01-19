package Worker;

import com.google.protobuf.InvalidProtocolBufferException;
import org.zeromq.ZMQ;
import tp.Messages;

import java.io.IOException;

public class SubImporters extends Thread {

    private PullManufacturer pull;
    private  ZMQ.Socket sub;

    public SubImporters(PullManufacturer worker,ZMQ.Socket sub){
        this.pull= worker;
        this.sub=sub;
    }

    public void run(){
        Messages.Message m;
        byte [] recv;
        try {
        while(true){
            recv = sub.recv();
            pull.addOffers(recv);
        }

        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
