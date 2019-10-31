import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class Client {

    static Socket clientSocket;
    static JTextArea receivedTextArea;
    static JComboBox usersComboBox;
    static JTextField clientTextField;
//    static JLabel countLabel;
//    static JLabel dateLabel;

    public static void main(String[] args) throws Exception {

        //Create the GUI frame and components
        JFrame frame = new JFrame ("Chatting Client");
        frame.setLayout(null);
        frame.setBounds(100, 100, 500, 550);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel clientLabel = new JLabel("Client Name");
        clientLabel.setBounds(20, 40, 150, 30);
        frame.getContentPane().add(clientLabel);

        clientTextField = new JTextField();
        clientTextField.setBounds(100, 40, 200, 30);
        frame.getContentPane().add(clientTextField);

        JButton connectButton = new JButton("Connect");
        connectButton.setBounds(300, 40, 100, 30);
        frame.getContentPane().add(connectButton);

//        countLabel = new JLabel("");
//        countLabel.setBounds(20, 100, 300, 30);
//        frame.getContentPane().add(countLabel);
//        countLabel.setVisible(false);

//        dateLabel = new JLabel("Server's Date");
//        dateLabel.setBounds(20, 140, 400, 30);
//        frame.getContentPane().add(dateLabel);
//        dateLabel.setVisible(false);

        JLabel sendLabel = new JLabel("Send To");
        sendLabel.setBounds(20, 400, 100, 30);
        frame.getContentPane().add(sendLabel);
        sendLabel.setVisible(false);

        JTextArea sendTextArea = new JTextArea();
        sendTextArea.setBounds(20, 425, 400, 75);
        frame.getContentPane().add(sendTextArea);
        sendTextArea.setVisible(false);

        JButton sendButton = new JButton("Send");
        sendButton.setBounds(420, 450, 80, 30);
        frame.getContentPane().add(sendButton);
        sendButton.setVisible(false);

        receivedTextArea = new JTextArea();
        receivedTextArea.setBounds(20, 100, 460, 300);
        receivedTextArea.setEditable(false);
        frame.getContentPane().add(receivedTextArea);

        usersComboBox = new JComboBox();
        usersComboBox.setBounds(100, 400, 200, 30);
        frame.getContentPane().add(usersComboBox);
        usersComboBox.setVisible(false);

        JScrollPane receivedTextAreaScroll = new JScrollPane(receivedTextArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        receivedTextAreaScroll.setBounds(20,  100,  460,  300);
        frame.getContentPane().add(receivedTextAreaScroll);

        //Action listener when connect button is pressed
        connectButton.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                if (!clientTextField.getText().equals("")) {
                    try {
                        if (connectButton.getText().equals("Connect")) { //if pressed to connect

                            //create a new socket to connect with the server application
                            clientSocket = new Socket("localhost", 6789);

                            //call function StartThread
                            StartThread();

                            //make the GUI components visible, so the client can send and receive messages
                            sendButton.setVisible(true);
                            sendLabel.setVisible(true);
                            sendTextArea.setVisible(true);
                            usersComboBox.setVisible(true);
                            clientTextField.setEnabled(false);
//                        dateLabel.setVisible(true);
//                        countLabel.setVisible(true);
                            receivedTextArea.setVisible(true);
                            receivedTextAreaScroll.setVisible(true);

                            //change the Connect button text to disconnect
                            connectButton.setText("Disconnect");

                        } else { //if pressed to disconnect

                            //create an output stream and send a Remove message to disconnect from the server
                            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
                            outToServer.writeBytes("-Remove\n");

                            //close the client's socket
                            clientSocket.close();

                            //make the GUI components invisible
                            sendButton.setVisible(false);
                            sendLabel.setVisible(false);
                            sendTextArea.setVisible(false);
                            usersComboBox.setVisible(false);
                            clientTextField.setEnabled(true);
//                        dateLabel.setVisible(false);
//                        countLabel.setVisible(false);
                            receivedTextArea.setText("");
                            sendTextArea.setText("");
                            receivedTextArea.setVisible(false);
                            receivedTextAreaScroll.setVisible(false);

                            //change the Connect button text to connect
                            connectButton.setText("Connect");

                        }

                    } catch (Exception ex) {
                        System.out.println(ex.toString());
                    }
                }
            }});

        //Action listener when send button is pressed
        sendButton.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                try {
                    //create an output stream
                    DataOutputStream outToServer = new DataOutputStream (clientSocket.getOutputStream());

                    Object selectedItem = usersComboBox.getSelectedItem();

                    String text = sendTextArea.getText();
                    String sendingSentence = "-Message," + text + "," + clientTextField.getText();

                    if (selectedItem != null) {
                        sendingSentence += "," + selectedItem;
                    }

                    sendingSentence += "\n";

                    outToServer.writeBytes(sendingSentence);
                } catch (Exception ex) {
                    System.out.println(ex.toString());
                }
            }});

        //Disconnect on close
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {

                try {

                    //create an output stream and send a Remove message to disconnect from the server
                    DataOutputStream outToServer = new DataOutputStream (clientSocket.getOutputStream());
                    outToServer.writeBytes("-Remove\n");

                    //close the client's socket
                    clientSocket.close();

                    //make the GUI components invisible
                    sendButton.setVisible(false);
                    sendLabel.setVisible(false);
                    sendTextArea.setVisible(false);
//                    dateLabel.setVisible(false);
//                    countLabel.setVisible(false);
                    receivedTextArea.setVisible(false);
                    receivedTextAreaScroll.setVisible(false);

                    //change the Connect button text to connect
                    connectButton.setText("Connect");

                    System.exit(0);

                } catch (Exception ex) {
                    System.out.println(ex.toString());
                }


            }
        });

        frame.setVisible(true);

    }

    //Thread to always read messages from the server and print them in the textArea
    private static void StartThread() {

        new Thread (new Runnable(){ @Override
        public void run() {

            try {

                //create a buffer reader and connect it to the socket's input stream
                BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String receivedSentence;

                //always read received messages and append them to the textArea
                while (true) {

                    receivedSentence = inFromServer.readLine();
                    //System.out.println(receivedSentence);

                    if (receivedSentence.startsWith("-Connected")) {
                        DataOutputStream outToServer = new DataOutputStream (clientSocket.getOutputStream());

                        String sendingSentence = "-Name," + clientTextField.getText() + "\n";
                        outToServer.writeBytes(sendingSentence);
                    } else if (receivedSentence.startsWith("-NewUser")) {

                        String []strings = receivedSentence.split(",");
                        String newUser = strings[1];
                        if (newUser.equals(clientTextField.getText())) {
                            receivedTextArea.append("You are Connected\n");
                        } else {
                            receivedTextArea.append(newUser + " is connected\n");
                        }
//                        dateLabel.setText("Server's Date: " + strings[1]);

                    } else if (receivedSentence.startsWith("-NewMessage")) {
                        String []strings = receivedSentence.split(",");
                        String message = strings[1];
                        String user = strings[2];
                        String messageTo = strings[3];
                        String messageFrom = strings[4];

                        String receivedMessage = "";

                        if (messageTo.equals(clientTextField.getText())){
                            receivedMessage += messageFrom + ": ";
                        } else if (messageFrom.equals(clientTextField.getText())) {
                            if (messageTo.equals(" ")) {
                                receivedMessage += "You: ";
                            } else {
                                receivedMessage += "You to " + messageTo + ": ";
                            }
                        } else {
                            receivedMessage += user + ": ";
                        }

                        receivedMessage += message + "\n";
                        receivedTextArea.append(receivedMessage);
                    } else if (receivedSentence.startsWith("-CurrentUsers")) {

                        usersComboBox.removeAllItems();

                        String []strings = receivedSentence.split(",");

                        usersComboBox.addItem("Everyone");

                        for (int i = 1; i < strings.length; i++) {
                            if (!strings[i].equals(clientTextField.getText()))
                                usersComboBox.addItem(strings[i]);
                        }
//                        countLabel.setText("Number of connected clients to the server: " + strings[1] + "\n");

                    }
                }

            }
            catch(Exception ex) {

            }


        }}).start();

    }

}