import org.zeromq.SocketType;
import org.zeromq.ZMQ;

public class Readify implements Runnable {
    private ZMQ.Socket sub;

    public Readify(){
        ZMQ.Context context = ZMQ.context(1);
        this.sub = context.socket(SocketType.SUB);
        this.sub.connect("tcp://localhost:12351");
    }

    public void run(){
        while(true){
            byte [] msg_encoded = this.sub.recv();
            String m = new String(msg_encoded);
            System.out.println(m);
        }
    }

    public void subscribe(String key){
        this.sub.subscribe(key);
    }

    public void unsubscribe(String key){
        this.sub.unsubscribe(key);
    }
}
