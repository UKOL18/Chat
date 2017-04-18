import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Карен on 18.04.2017.
 */
public class Server {
    private static int uniqueId;
    private ArrayList<ClientThread> allClients;

    private SimpleDateFormat sdf;
    private int port;
    private boolean keepGoing;

    public Server(int port) {
        this.port = port;
        sdf = new SimpleDateFormat("HH:mm:ss");
        allClients = new ArrayList<ClientThread>();
    }


    public void start() {
        keepGoing = true;
        try
        {
            ServerSocket serverSocket = new ServerSocket(port);
            while(keepGoing)
            {
                display("Server successfully started on port " + port);
                Socket socket = serverSocket.accept();
                if(!keepGoing)
                    break;
                ClientThread t = new ClientThread(socket);
                allClients.add(t);
                t.start();
            }
            try {
                serverSocket.close();
                for(int i = 0; i < allClients.size(); ++i) {
                    ClientThread tc = allClients.get(i);
                    try {
                        tc.sInput.close();
                        tc.sOutput.close();
                        tc.socket.close();
                    }
                    catch(IOException ioE) {
                        display(ioE.toString());
                    }
                }
            }
            catch(Exception e) {
                display(e.toString());
            }
        }
        catch (IOException e) {
            display(e.toString());
        }
    }

    private void display(String msg) {
        String time = sdf.format(new Date()) + " " + msg;
        System.out.println(time);
    }

    private synchronized void broadcast(String message) {
        String time = sdf.format(new Date());
        String messageLf = time + " " + message + "\n";
        System.out.print(messageLf);

        for(int i = allClients.size(); --i >= 0;) {
            ClientThread ct = allClients.get(i);
            if(!ct.writeMsg(messageLf)) {
                allClients.remove(i);
                display("Disconnected Client " + ct.username);
            }
        }
    }

    synchronized void remove(int id) {
        for(int i = 0; i < allClients.size(); ++i) {
            ClientThread ct = allClients.get(i);
            if(ct.id == id) {
                allClients.remove(i);
                return;
            }
        }
    }

    public static void main(String[] args) {
        Server server = new Server(Config.PORT);
        server.start();
    }

    class ClientThread extends Thread {
        Socket socket;
        ObjectInputStream sInput;
        ObjectOutputStream sOutput;
        int id;
        String username;
        Message cm;
        String date;

        ClientThread(Socket socket) {
            id = ++uniqueId;
            this.socket = socket;
            try
            {
                sOutput = new ObjectOutputStream(socket.getOutputStream());
                sInput  = new ObjectInputStream(socket.getInputStream());
                username = (String) sInput.readObject();
                display(username + " connected.");
            }
            catch (IOException e) {
                display(e.getStackTrace().toString());
                return;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            date = new Date().toString() + "\n";
        }

        public void run() {
            boolean keepGoing = true;
            while(keepGoing) {
                try {
                    cm = (Message) sInput.readObject();
                }
                catch (IOException e) {
                    display(username + " Stream: " + e);
                    break;
                }
                catch(ClassNotFoundException e2) {
                    break;
                }
                String message = cm.getMessage();

                switch(cm.getType()) {
                    case Message.MESSAGE:
                        broadcast(username + ": " + message);
                        break;
                    case Message.AY:
                        writeMsg("List of users connected at " + sdf.format(new Date()) + "\n");
                        for(int i = 0; i < allClients.size(); ++i) {
                            ClientThread ct = allClients.get(i);
                            writeMsg((i+1) + ") " + ct.username + " since " + ct.date);
                        }
                        break;
                }
            }
            remove(id);
            close();
        }

        private void close() {
            try {
                if(sOutput != null) sOutput.close();
            }
            catch(Exception e) {}
            try {
                if(sInput != null) sInput.close();
            }
            catch(Exception e) {};
            try {
                if(socket != null) socket.close();
            }
            catch (Exception e) {}
        }

        private boolean writeMsg(String msg) {
            if(!socket.isConnected()) {
                close();
                return false;
            }
            try {
                sOutput.writeObject(msg);
            }
            catch(IOException e) {
                display("Error sending message to " + username);
                display(e.toString());
            }
            return true;
        }
    }
}

