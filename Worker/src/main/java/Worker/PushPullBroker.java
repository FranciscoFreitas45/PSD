package Worker;

import org.zeromq.ZMQ;

public class PushPullBroker {

    public static void main(String[] args) {
        ZMQ.Context context = ZMQ.context(1);
        ZMQ.Socket push = context.socket(ZMQ.PULL);
        ZMQ.Socket pull = context.socket(ZMQ.PUSH);
        push.bind("tcp://*:" + 12348);
        pull.bind("tcp://*:" + 12347);
        ZMQ.proxy(push, pull, null);
    }
}
