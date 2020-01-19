package Worker;

import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import tp.Messages;

public class WorkerTest {
    public static void main(String[] args) {
        ZMQ.Context context = ZMQ.context(1);
        ZMQ.Socket pull = context.socket(SocketType.PULL);
        ZMQ.Socket subSocket = context.socket(SocketType.SUB);
        ZMQ.Socket push = context.socket(SocketType.PUSH);
        Notifier note = new Notifier(context);

        push.connect("tcp://localhost:" + 12349);
        pull.connect("tcp://localhost:" + 12348);
        subSocket.connect("tcp://localhost:"+12346);

        /*
        Worker worker = new Worker(pull,subSocket);
        SubImporters subImporters = new SubImporters(worker,subSocket);
        worker.start();
        subImporters.start();*/

        Messages.Message m = null;
        try{
            byte [] recv = pull.recv();
            System.out.println("Message 1");
            m = Messages.Message.parseFrom(recv);
            Messages.ManufacturerOrder manu = m.getManufacturerOrder();
            System.out.println(manu.toString());
            String key = String.valueOf(manu.getId()) + ":";
            subSocket.subscribe(key.getBytes());
            int len = key.getBytes().length;
            note.notify(manu);


            System.out.println("Message 2");
            recv = subSocket.recv();
            byte [] recv_real = new byte[recv.length-len];
            System.arraycopy(recv,len,recv_real,0,recv.length-len);
            m = Messages.Message.parseFrom(recv_real);
            Messages.ImporterOffer offer = m.getImporterOffer();
            offer = offer.toBuilder().setStateValue(2).build();
            note.notify(offer);
            System.out.println(offer.toString());

            System.out.println("Message 2");
            recv = subSocket.recv();
            recv_real = new byte[recv.length-len];
            System.arraycopy(recv,len,recv_real,0,recv.length-len);
            m = Messages.Message.parseFrom(recv_real);
            Messages.ImporterOffer offer1 = m.getImporterOffer();
            offer1 = offer1.toBuilder().setStateValue(1).build();
            note.notify(offer1);
            System.out.println(offer1.toString());

            /**
            System.out.println("Message 3");
            recv = subSocket.recv();
            recv_real = new byte[recv.length-len];
            System.arraycopy(recv,len,recv_real,0,recv.length-len);
            m = Messages.Message.parseFrom(recv_real);
            Messages.ImporterOffer offer2 = m.getImporterOffer();
            System.out.println(offer.toString());*/




            Messages.Reply r = Messages.Reply.newBuilder()
                    .setId(manu.getId())
                    .setManufacturer(manu.getManufacturer())
                    .setProduct(manu.getProduct())
                    .setResValue(1)
                    .setProfit((long)100.0)
                    .addOffers(offer)
                    .addOffers(offer1)
           //         .addOffers(offer2)
                    .build();
            Messages.Message reply = Messages.Message.newBuilder().setReply(r).build();
            push.send(reply.toByteArray());
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

}
