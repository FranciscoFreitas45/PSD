package Worker;

import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import tp.Messages;

public class WorkerTest {
    public static void main(String[] args) {
        ZMQ.Context context = ZMQ.context(1);
        ZMQ.Socket pull = context.socket(SocketType.PULL);
        ZMQ.Socket subSocket = context.socket(SocketType.SUB);
        pull.connect("tcp://localhost:" + 12348);
        subSocket.connect("tcp://localhost:"+12346);

        Worker worker = new Worker(pull,subSocket);
        SubImporters subImporters = new SubImporters(worker,subSocket);
        worker.start();
        subImporters.start();

    }
}
