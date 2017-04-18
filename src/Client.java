import java.net.*;
import java.io.*;
import java.util.*;

/**
 * Created by Карен on 18.04.2017.
 */
public class Client  {

    private ObjectInputStream sInput;
    private ObjectOutputStream sOutput;
    private Socket socket;


    private String server, username;
    private int port;

    Client(String server, int port, String username) {
        this.server = server;
        this.port = port;
        this.username = username;
    }


    public boolean start() {
        try {
            socket = new Socket(server, port);
        }
        catch(Exception ec) {
            display(ec.toString());
            return false;
        }

        String msg = "Connection accepted " + socket.getInetAddress() + ":" + socket.getPort();
        display(msg);

        try
        {
            sInput  = new ObjectInputStream(socket.getInputStream());
            sOutput = new ObjectOutputStream(socket.getOutputStream());
        }
        catch (IOException eIO) {
            display(eIO.toString());
            return false;
        }

        new ListenFromServer().start();
        try
        {
            sOutput.writeObject(username);
        }
        catch (IOException eIO) {
            display(eIO.toString());
            disconnect();
            return false;
        }
        return true;
    }


    private void display(String msg) {
            System.out.println(msg);
    }


    void sendMessage(Message msg) {
        try {
            sOutput.writeObject(msg);
        }
        catch(IOException e) {
            display(e.toString());
        }
    }

    private void disconnect() {
        try {
            if(sInput != null) sInput.close();
        }
        catch(Exception e) {}
        try {
            if(sOutput != null) sOutput.close();
        }
        catch(Exception e) {}
        try{
            if(socket != null) socket.close();
        }
        catch(Exception e) {}
    }

    public static void main(String[] args) {
        String userName = args[0] != null ? args[0] : "Anonymous";

        Client client = new Client(Config.ADDRESS, Config.PORT, userName);
        if(!client.start())
            return;

        Scanner scan = new Scanner(System.in);
        while (true) {
            System.out.print("> ");
            String msg = scan.nextLine();
            if (msg.equalsIgnoreCase("AY"))
                client.sendMessage(new Message(Message.AY, ""));
            else
                client.sendMessage(new Message(Message.MESSAGE, msg));
        }
    }

    class ListenFromServer extends Thread {

        public void run() {
            while(true) {
                try {
                    String msg = (String) sInput.readObject();
                        System.out.println(msg);
                        System.out.print("> ");
                }
                catch(IOException e) {
                    display(e.toString());
                    break;
                }
                catch(ClassNotFoundException e) {
                    display(e.toString());
                    break;
                }
            }
        }
    }
}
