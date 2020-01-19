package Worker;

import com.google.protobuf.InvalidProtocolBufferException;
import org.zeromq.ZMQ;
import tp.Messages;

public class SubImporters extends Thread {

    private Worker worker;
    private  ZMQ.Socket sub;

    public SubImporters(Worker worker,ZMQ.Socket sub){
        this.worker= worker;
        this.sub=sub;
    }

    public void run(){
        Messages.Message m;
        byte [] recv;
        try {
        while(true){
            recv = sub.recv();
            worker.addOffers(recv);
        }

        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

}
