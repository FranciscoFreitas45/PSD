package Worker;

import Catalog.Representation.*;
import org.zeromq.ZMQ;
import java.util.Queue;

public class PubSubBroker {

    public static void main(String[] args) {
        ZMQ.Context context = ZMQ.context(1);
        ZMQ.Socket pubs = context.socket(ZMQ.XSUB);
        ZMQ.Socket subs = context.socket(ZMQ.XPUB);
        pubs.bind("tcp://*:" + 12345);
        subs.bind("tcp://*:" + 12346);
        ZMQ.proxy(pubs, subs, null);
    }
}