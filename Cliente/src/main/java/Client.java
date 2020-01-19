


import java.io.IOException;
import java.net.Socket;
import org.zeromq.ZMQ;


    public class Client {

        public static void main(String[] args) throws Exception, IOException {
            int server_port = Integer.parseInt(args[1]);

            Socket cli = new Socket(args[0], server_port);
            InfoCliente info = new InfoCliente(2);
            Reader reader = new Reader(cli,info);
            Readify read = new Readify();
            Stub stub = new Stub(cli,info,read);

            new Thread(read).start();
            stub.start();
            reader.start();




            //reader.start();
        }

}
