import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Server {

    //Array of type ClientServiceThread, for all connected clients
    public static ArrayList<ClientThread> Clients = new ArrayList<ClientThread>();
    static int clientCount = 0;
    static String message = "";
    static String messageTo = "";
    static String messageFrom = "";
    static String clientListString = "";

    public static void main(String[] args) throws Exception {

        //Create the GUI frame and components
        JFrame frame = new JFrame ("Chatting Server");
        frame.setLayout(null);
        frame.setBounds(100, 100, 300, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        JLabel connectionStatusLabel = new JLabel("No Clients Connected");
        connectionStatusLabel.setBounds(80, 30, 200, 30);
        connectionStatusLabel.setForeground(Color.red);
        frame.getContentPane().add(connectionStatusLabel);

        //create the welcoming server's socket
        ServerSocket welcomeSocket = new ServerSocket(6789);

        //thread to always listen for new connections from clients
        new Thread (new Runnable(){ @Override
        public void run() {

            Socket connectionSocket;
            DataOutputStream outToClient;

            while (!welcomeSocket.isClosed()) {

                try {

                    //when a new client connect, accept this connection and assign it to a new connection socket
                    connectionSocket = welcomeSocket.accept();

                    //create a new output stream and send the message "You are connected" to the client
                    outToClient = new DataOutputStream(connectionSocket.getOutputStream());
                    outToClient.writeBytes("-Connected\n");

                    BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));;
                    String name = inFromClient.readLine().split(",")[1];

                    clientCount++;

                    //add the new client to the client's array
                    Clients.add(new ClientThread(clientCount, name, connectionSocket, Clients));
                    //start the new client's thread
                    Clients.get(Clients.size() - 1).start();

                }
                catch (Exception ex) {

                }

            }

        }}).start();



        //thread to always get the count of connected clients and update the label and send to clients
        new Thread (new Runnable(){ @Override
        public void run() {

            try {

                DataOutputStream outToClient;
                int currentCount = 0;

                while (true) {

                    if (Clients.size() > 0) //if there are one or more clients print their number
                    {
                        if (Clients.size() == 1)
                            connectionStatusLabel.setText("1 Client Connected");
                        else
                            connectionStatusLabel.setText(Clients.size() + " Clients Connected");

                        connectionStatusLabel.setForeground(Color.blue);
                    }
                    else { //if there are no clients connected, print "No Clients Connected"

                        connectionStatusLabel.setText("No Clients Connected");
                        connectionStatusLabel.setForeground(Color.red);

                        clientCount = 0;
                    }

                    if (currentCount < Clients.size()) {
                        for (int i = 0; i < Clients.size(); i++) {
                            outToClient = new DataOutputStream(Clients.get(i).connectionSocket.getOutputStream());
                            outToClient.writeBytes("-NewUser," + Clients.get(currentCount).name + "\n");
                        }

                        currentCount++;
                    } else if (currentCount > Clients.size()) {
                        currentCount -= 1;
                    }

                    for (int i = 0; i < Clients.size(); i++) {

                        outToClient = new DataOutputStream(Clients.get(i).connectionSocket.getOutputStream());
                        outToClient.writeBytes("-Count, " + Clients.size() + "\n");

                    }

                    Thread.sleep(1000);

                }

            } catch (Exception ex) {

            }

        }}).start();


        //thread to always get the date and send to clients
        new Thread (new Runnable(){ @Override
        public void run() {

            try {

                DataOutputStream outToClient;

                while (true) {

                    if (!message.equals("")) {
                        for (int i = 0; i < Clients.size(); i++) {
                            outToClient = new DataOutputStream(Clients.get(i).connectionSocket.getOutputStream());
                            String outputMessage = message + "," + messageTo + "," + messageFrom + "\n";

                            if (!messageTo.equals(" ") && !messageFrom.equals(" ")) {
                                if (messageTo.equals(Clients.get(i).name)) {
                                    outToClient.writeBytes(outputMessage);
                                } else if (messageFrom.equals(Clients.get(i).name)) {
                                    outToClient.writeBytes(outputMessage);
                                }
                            } else {
                                outToClient.writeBytes(outputMessage);
                            }
                        }

                        message = "";
                        messageTo = "";
                        messageFrom = "";
                    }

                    Thread.sleep(1000);

                }

            } catch (Exception ex) {

            }

        }}).start();

        //thread to always get the date and send to clients
        new Thread (new Runnable(){ @Override
        public void run() {

            try {

                DataOutputStream outToClient;

                while (true) {
                    if (Clients.size() > 0) {
                        String clientsList = "";

                        for (int i = 0; i < Clients.size() - 1; i++) {
                            clientsList += Clients.get(i).name + ",";
                        }

                        clientsList += Clients.get(Clients.size() - 1).name;

                        if (!clientListString.equals(clientsList)) {
                            for (int i = 0; i < Clients.size(); i++) {
                                outToClient = new DataOutputStream(Clients.get(i).connectionSocket.getOutputStream());
                                outToClient.writeBytes("-CurrentUsers" + "," + clientsList + "\n");
                            }

                            clientListString = clientsList;
                        }
                    }

                    Thread.sleep(1000);
                }

            } catch (Exception ex) {

            }

        }}).start();



        frame.setVisible(true);

    }

}