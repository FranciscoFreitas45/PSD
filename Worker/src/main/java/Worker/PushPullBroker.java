package Worker;

import org.zeromq.SocketType;
import org.zeromq.ZMQ;

public class PushPullBroker {

    public static void main(String[] args) {
        ZMQ.Context context = ZMQ.context(1);
        ZMQ.Socket pubs = context.socket(SocketType.PULL);
        ZMQ.Socket subs = context.socket(SocketType.PUSH);
        pubs.bind("tcp://*:"+12347);
        subs.bind("tcp://*:"+12348);
        new Proxy(context, pubs, subs).poll();
    }
}
