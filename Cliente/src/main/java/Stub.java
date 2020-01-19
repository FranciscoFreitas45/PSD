import com.google.gson.*;
import com.google.protobuf.Message;
import tp.Messages;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.List;
import java.util.Scanner;

public class Stub extends Thread {
    private final static String MENU_SEPARATOR = "-";
    private final static Integer MANUFACTURER = 1;
    private final static Integer IMPORTER = 2;


    private Socket cliSocket;
    private InfoCliente client;
    private PrintWriter out;
    private InputStream is;
    private OutputStream os;
    private Menu menu;
    private Readify r;

    Stub(Socket cliSocket, InfoCliente client,Readify r) throws IOException {
        this.cliSocket = cliSocket;
        this.client = client;
        this.is = cliSocket.getInputStream();
        this.os = cliSocket.getOutputStream();
        this.out = new PrintWriter(os, true);
        this.menu= new Menu();
        this.r = r;
    }

    public void run() {
        int option=0;

        while(true) {
            int status = showMenu();

            try {
                switch (status) {
                    case 0:
                        option = read_menu_output();
                        break;
                    case 1:
                             option=read_menu_Manufacturer_output();
                        break;
                    case 2:
                           option=read_menu_output_Importer();
                        break;
                }

                if ((option == 2 && status == 0) || (option == 5 && status == 1) || (option == 7 && status == 2))// sair em caso de Exit ou Logout
                    break;

            }catch(Exception e) { }
        }
        System.out.println("\nConnection ended!");
        System.exit(0);

    }

    public int showMenu(){
        String show;
        int status; // para saber que handler de menus usar
        if(this.client.isLogged()){
            if(this.client.getType()==IMPORTER) {
                show = listOptions(menu.getMainMenuImporterOptions());
                status=2;// handler para menu de MANUFACTER
            }
            else {
                show = listOptions(menu.getMainMenuManufacterOptions());
                status=1;//handler para menu de IMPORTER
            }
        }
        else {
            show = listOptions(menu.getInitialMenuOptions());
            status=0;
        }

        System.out.println(show);
        return status;
    }



    public int read_menu_output() throws IOException {
        int option = this.menu.readInt("");
        switch(option){
            case 0:
                menu_login();
                break;
            case 1:
                menu_register();
                break;
            case 2:
                break;

            }
        return option;
    }

    public int read_menu_Manufacturer_output() throws IOException {
        int option = this.menu.readInt("");
        switch(option){
            case 0:
                menu_Order();
                break;
            case 1:
                showManufacturer();
                break;
            case 2:
                showManufacturerOrders();
                break;
            case 3:
                showHistoricManufacturer();
                break;

            case 4:
               showHistoricImporter();
                break;

            case 5:
                break;
        }
        return option;

    }

    public void subscribeManufactor(){
        String manu = menu.readString("Manufactor to sub:");
        this.r.subscribe(manu+"|");
    }

    public void unsubscribeManufactor(){
        String manu = menu.readString("Manufactor to unsub:");
        this.r.unsubscribe(manu+"|");
    }


    public int read_menu_output_Importer() throws  IOException{
            int option = this.menu.readInt("");
            switch(option){
                case 0:
                      menu_Offer();
                    break;
                case 1:
                    showManufacturer();
                    break;
                case 2:
                    showManufacturerOrders();
                    break;
                case 3:
                    showHistoricManufacturer();
                    break;

                case 4:
                    showHistoricImporter();
                    break;

                case 5:
                    subscribeManufactor();
                    break;
                case 6:
                    unsubscribeManufactor();
                    break;
                case 7:
                    break;
            }
            return option;

        }




    private void menu_login() throws IOException {
            String username = menu.readString("Username: ");
            String password = menu.readString("Password: ");

            Messages.User c = Messages.User.newBuilder().setUsername(username).setPassword(password).build();
            Messages.Message req = Messages.Message.newBuilder().setType("LOGIN").setUser(c).build();
            byte[] result = req.toByteArray();
            os.write(result);

            this.client.waitReply();
    }

    private void menu_register() throws IOException{
        String username = menu.readString("Username: ");
        String password = menu.readString("Password: ");
        Integer type = menu.readInt("1 - manufacturer | 2 - Importer ");

        Messages.User c = Messages.User.newBuilder().setUsername(username).setPassword(password).setType(type).build();
        Message req = Messages.Message.newBuilder().setType("REGISTER").setUser(c).build();
        byte[] result = req.toByteArray();
        os.write(result);
        this.client.waitReply();

    }

    private void menu_Order() throws IOException {
        String product = menu.readString("Product: ");
        Integer minQuantity=menu.readInt("Minimum Quantity: ");
        Integer maxQuantity = menu.readInt("Maximum Quantity: ");
        double unitPrice =  (double) menu.readFloat(" Unit Price: ");
        Long negotiation = (long) menu.readInt("Time to negotiation:");

        Messages.ManufacturerOrder c = Messages.ManufacturerOrder.newBuilder().setProduct(product).setMinQuantity(minQuantity).setMaxQuantity(maxQuantity)
                .setUnitPrice(unitPrice).setNegotiation(negotiation).build();

        Message req = Messages.Message.newBuilder().setType("ORDER").setManufacturerOrder(c).build();
        byte[] result = req.toByteArray();
        os.write(result);

}


    private void menu_Offer() throws IOException {
        //String manufacturer = menu.readString("Manufacturer: ");
        String product=menu.readString("Product: ");
        Integer quantity = menu.readInt("Quantity: ");
        double unitPrice =  (double) menu.readFloat(" Unit Price: ");
        Integer idOrder =  menu.readInt("Id Order :");

        Messages.ImporterOffer c = Messages.ImporterOffer.newBuilder().setImporter("Ola").setProduct(product).setQuantity(quantity)
                                   .setUnitPrice(unitPrice).setIdorder(idOrder).build();
        Message req = Messages.Message.newBuilder().setType("OFFER").setImporterOffer(c).build();

        this.r.subscribe("ORDER" + idOrder + "|");

        byte[] result = req.toByteArray();
        os.write(result);
    }



    private void showManufacturerOrders() throws IOException {
        String nameManufacturer = menu.readString("Manufacturer: ");
        String path= "http://localhost:8080/manufacturer/"+nameManufacturer+"/orders";
        consumeApiRest(path);
    }


    private void showHistoricImporter() throws IOException {
        String nameImporter = menu.readString("Importer: ");
        String path="http://localhost:8080/importer/"+nameImporter+"/historic";
        consumeApiRest(path);
    }

    private void showHistoricManufacturer() throws IOException {
        String nameManufacturer = menu.readString("Manufacturer: ");
        String path="http://localhost:8080/manufacturer/"+ nameManufacturer +"/historic";
        consumeApiRest(path);
    }

    private void showManufacturer() throws IOException {
        String path = "http://localhost:8080/manufacturer";
        consumeApiRest(path);
    }




    private void consumeApiRest(String path) throws IOException{
        URL url = new URL(path);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer reply = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            reply.append(inputLine);
        }
        in.close();

        System.out.println(toPrettyFormat(reply.toString()));
    }


    public static String toPrettyFormat(String jsonString) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonParser jp = new JsonParser();
        JsonElement je = jp.parse(jsonString);
        String prettyJsonString = gson.toJson(je);
        return prettyJsonString;


    }

    private static String listOptions(List<String> options) {
        int maxSize = 0;
        StringBuilder opsString = new StringBuilder();
        for(int i = 0; i < options.size(); i++) {
            String option = options.get(i);
            opsString.append(i).append(") ").append(option).append("\n");
            maxSize = Math.max(option.length(), maxSize);
        }
        int n = String.valueOf(options.size()).length();
        String separator = MENU_SEPARATOR.repeat(maxSize + 2 + n) + "\n";
        return separator + opsString + separator;
    }


}
