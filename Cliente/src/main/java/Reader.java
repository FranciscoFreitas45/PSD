
import java.net.Socket;
import java.io.InputStream;
import java.io.IOException;

import com.google.protobuf.Message;
import org.zeromq.ZMQ;
import tp.Messages;


public class Reader extends Thread {
    private Socket cliSocket;
    private InputStream is;
    private InfoCliente info;

    Reader(Socket cliSocket,InfoCliente info) throws IOException {
        this.cliSocket = cliSocket;
        this.info=info;
        this.is = cliSocket.getInputStream();
    }

    public void run() {
        String  content;
        Message m;
        Integer status;
        while(((m = readMessage()) != null)) {

            content = readResponse(m);

            status= readStatus(m);
            if(status==1){
                info.setLogged(true);
            }
            info.awake();
            System.out.println(content);

        }

        System.out.println("\nConnection ended by the server.");
        System.exit(1);
    }


    private String readResponse(Message m) {
        return ((Messages.Message)m).getResponse().getResponse();
    }

    private Integer readStatus(Message m){
        return ((Messages.Message)m).getResponse().getStatus();
    }




    private Message readMessage() {
        Message m = null;

        try {
            byte[] msg = recvMsg(is);
            m = Messages.Message.parseFrom(msg);
        } catch(Exception e) {
            System.out.println("Erro ao ler mensagem.");
        }
        return m;
    }

    private static byte[] recvMsg(InputStream inpustream) {
        try {

            byte len[] = new byte[4096];
            int count = inpustream.read(len);
            byte[] temp = new byte[count];
            for (int i = 0; i < count; i++) {
                temp[i] = len[i];
            }
            return temp;
        } catch (Exception e) {
            System.out.println("recvMsg() occur exception!" + e.toString());
        }
        return null;
    }

}
