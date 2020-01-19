
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Menu {
    private Scanner in;

    public Menu() {
        in = new Scanner(System.in);
    }

    public List<String> getInitialMenuOptions() {
        return new ArrayList<>(Arrays.asList("Login", "Register", "Quit"));
    }


    public List<String> getMainMenuImporterOptions(){
        return new ArrayList<>(Arrays.asList("Make offer","Show manufacturer","Negotiation in course","Show historic manufacturer","Show historic importer", "Subscribe", "Unsubscribe" ,"Logout"));
    }

    public List<String> getMainMenuManufacterOptions(){
        return new ArrayList<>(Arrays.asList("Make order","Show manufacturer","Negotiation in course","Show historic manufacturer","Show historic importer","Logout"));
    }


    public String readString(String msg) {
        System.out.print(msg);
        return in.next();
    }

    public int readInt(String msg) {
        int num;

        try {
            System.out.print(msg);
            num = Integer.parseInt(in.next());
        } catch (NumberFormatException e) {
            System.out.println("\n> The value introduced is not valid\n");
            num = readInt(msg);
        }

        return num;
    }

    public float readFloat(String msg) {
        float num;

        try {
            System.out.print(msg);
            num = Float.parseFloat(in.next());
        } catch (NumberFormatException e) {
            System.out.println("\n> The value introduced is not valid\n");
            num = readFloat(msg);
        }

        return num;
    }

}
