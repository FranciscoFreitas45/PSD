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

        Messages.Message m = null;
        byte [] recv = pull.recv();
        System.out.println("Message 1");
        recv = pull.recv();
        System.out.println("Message 2");
        try{
            /**
            System.out.println("Message 1");
            m = Messages.Message.parseFrom(recv);
            Messages.ManufacturerOrder manu = m.getManufacturerOrder();
            System.out.println(manu.toString());
            String key = String.valueOf(manu.getId()) + ":";
            subSocket.subscribe(key.getBytes());
            int len = key.getBytes().length;
            System.out.println("Message 2");
            recv = subSocket.recv();
            byte [] recv_real = new byte[recv.length-len];
            System.arraycopy(recv,len,recv_real,0,recv.length-len);
            m = Messages.Message.parseFrom(recv_real);
            Messages.ImporterOffer offer = m.getImporterOffer();
            System.out.println(offer.toString());
            System.out.println("Acabou");*/
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
