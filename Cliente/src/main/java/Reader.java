
import java.net.Socket;
import java.io.InputStream;
import java.io.IOException;

import com.google.protobuf.Message;
import org.zeromq.ZMQ;
import tp.Messages;


public class Reader extends Thread {
    private final static Integer MANUFACTURER = 1;
    private final static Integer IMPORTER = 2;
    private Socket cliSocket;
    private InputStream is;
    private InfoCliente info;

    Reader(Socket cliSocket,InfoCliente info) throws IOException {
        this.cliSocket = cliSocket;
        this.info=info;
        this.is = cliSocket.getInputStream();
    }

    public void run() {
        Messages.Message m;
        while(((m = readMessage()) != null)) {
            handler_Reply(m);
            info.awake();
        }

        System.out.println("\nConnection ended by the server.");
        System.exit(1);
    }

    public void handler_Reply(Messages.Message m){
       int status= readStatus(m);
       String response = readResponse(m);
        if(status==1) {
                switch (response) {
                    case "1":
                        info.setType(MANUFACTURER);
                        info.setLogged(true);
                        break;
                    case "2":
                        info.setType(IMPORTER);
                        info.setLogged(true);
                        break;
                    case "REGISTED":
                        break;
                    default:
                        System.out.println(response);

                }
            }
    else {
            System.out.println(response);
        }

    }


    private String readResponse(Messages.Message m) {
        return m.getResponse().getResponse();
    }

    private Integer readStatus(Messages.Message m){
        return m.getResponse().getStatus();
    }




    private Messages.Message readMessage() {
        Messages.Message m = null;

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
