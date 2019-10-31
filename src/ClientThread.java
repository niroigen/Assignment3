import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;

public class ClientThread extends Thread{

    //the ClientServiceThread class extends the Thread class and has the following parameters
    public int id;
    public String name; //client name
    public Socket connectionSocket; //client connection socket
    ArrayList<ClientThread> Clients; //list of all clients connected to the server

    //constructor function
    public ClientThread(int id, String name, Socket connectionSocket, ArrayList<ClientThread> Clients) {

        this.id = id;
        this.name = name;
        this.connectionSocket = connectionSocket;
        this.Clients = Clients;

    }

    //thread's run function
    public void run() {

        try {

            //create a buffer reader and connect it to the client's connection socket
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));;
            String clientSentence;
            DataOutputStream outToClient;

            //always read messages from client
            while (true) {

                clientSentence = inFromClient.readLine();

                //ckeck the start of the message

                if (clientSentence.startsWith("-Remove")) { //Remove Client

                    for (int i = 0; i < Clients.size(); i++) {

                        if (Clients.get(i).id == id) {
                            Clients.remove(i);
                        }

                    }

                } else if (clientSentence.startsWith("-Message")) { //compute the sum and send the result back

                    String []sentMessages = clientSentence.split(",");

                    String message = sentMessages[1];
                    String user = sentMessages[2];

                    if (sentMessages.length == 4) {
                        Server.messageTo = sentMessages[3];
                    }

                    Server.messageFrom = user;

                    if (Server.messageTo.equals("Everyone")) {
                        Server.messageTo = " ";
                    }

                    Server.message = "-NewMessage," + message + "," + user;

                    System.out.println("message is " + Server.message + " for " + Server.messageTo + " by " + Server.messageFrom);
                }
            }
        } catch(Exception ex) {

        }
    }
}