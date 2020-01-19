package Worker;


import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import java.util.Queue;

public class PubSubBroker {

    public static void main(String[] args) {
        ZMQ.Context context = ZMQ.context(1);
        ZMQ.Socket pubs = context.socket(SocketType.XSUB);
        ZMQ.Socket subs = context.socket(SocketType.XPUB);
        pubs.bind("tcp://*:"+12345);
        subs.bind("tcp://*:"+12346);
        new Proxy(context, pubs, subs).poll();
    }
}