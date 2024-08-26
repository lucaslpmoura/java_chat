import java.net.*; // for Socket and ServerSocket
import java.io.*; // for IOException
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Arrays;


class ChatServer {
    private ServerSocket servSock;

    //Used for username
    public static final int BUFFERSIZE = 32;

    private static final int maxClients = 3;
    private int numOfClients = 0;
    
    private Thread connectionThread;

    private Client[] clients = new Client[maxClients];
    private ReadThread[] readThreads = new ReadThread[maxClients];

    private Message recievedMessage;

    public ChatServer(int servPort){
        recievedMessage = null;


        try{
            this.servSock = new ServerSocket(servPort);
        }catch(IOException e){
            System.out.println(e.getMessage());
        }

        System.out.println("Started server at port " + servPort);

        connectionThread = new Thread() {
            public void run () {
                for(;;){
                    Socket clientSock = accept();
                    Client newClient = new Client(clientSock);

                    //Reads the username of the client
                    try{
                        byte[] buffer = new byte[BUFFERSIZE];
                        newClient.input.read(buffer);
                        newClient.userName = new String(buffer);
                    }catch (IOException e){
                        System.out.println("Couldn't read username: " +  e.getMessage());
                    }
                    
                    addClient(newClient);
                }
            }
        };
        connectionThread.start();
    }

    public Socket accept(){
        try{
               return servSock.accept();
            }catch (IOException e){
                System.out.println(e.getMessage());
                return null;
        }
        
    }

    private void addClient(Client newClient){
        if(numOfClients <= maxClients ) {
            int slot = numOfClients;
            for (int i = 0; i < clients.length; i++){
                if(clients[i] == null){
                    slot = i;
                }
            }
            newClient.slot = slot;
            clients[slot] = newClient;
            ReadThread readThread = new ReadThread(slot, newClient);
            readThreads[slot] = readThread;
            
            System.out.println("Created Read-Thread for client at " + newClient.getAddress() + " Username: " + newClient.userName);
            readThread.start();
            
            numOfClients++;
        }
    }

    private void removeClient(Client clientToRemove){
        try{
            clientToRemove.isConnected = false;
            clients[clientToRemove.slot] = null;
            System.out.println("Disconected client at " + clientToRemove.getAddress());
            return;
        }catch(Exception e){
            System.out.println("Couldn't remove client: " + e.getMessage());
        }
    }

    private Boolean needToPrint = true;

    public void startDispatching() {
        Client clientToRemove = null;
        
        for (ReadThread readThread : readThreads){
            if(readThread != null){
                Message msg = readThread.readMessage();
                if (msg != null){
                    recievedMessage = msg;
                    if(recievedMessage.content != null){
                        System.out.println(recievedMessage.content);
                    }
                    break;
                }
            }
        }
        for (Client client : clients){
            if(recievedMessage != null ){
                if(recievedMessage.content != " "){
                    if(client != null){
                        System.out.println("Sending message to client: " + client.getAddress());
                    try{    
                        client.output.write((recievedMessage.originator.userName + ": " + recievedMessage.content).getBytes());
                    }catch(IOException e){
                        System.out.println("Error writing to client: " + e.getMessage() + ", disconecting client");
                        clientToRemove = client;
                        recievedMessage = null;
                        break;
                    }
                    }
                }
                
            }
        }
        recievedMessage = null;
        if(clientToRemove != null){
            removeClient(clientToRemove);
        }
    }



    public static void main(String args[]) throws Exception{
        int serverPort = 7600;
        ChatServer server = new ChatServer(serverPort);
        for (;;){ 
            server.startDispatching();
        }
        

    }
}

class ReadThread extends Thread{
    public static final int BUFFERSIZE = 512;
    protected int id;
    protected Client client;
    private byte[] buffer;
    Message message;

    ReadThread(int id, Client client){
        this.id = id;
        this.client = client;
        this.message = new Message(client, null);
    }

    //@override
    public void run(){
        while(client.isConnected){
            
            buffer = new byte[BUFFERSIZE];
            InputStream in = client.input;
            try{
                in.read(buffer);
            }catch(IOException e){
                System.out.println("Error reading input buffer: " + e.getMessage());
            }
            message.content = new String(buffer);
        }
    }

    public Message readMessage(){
        if(message.content != null){
            Message msg = new Message(client, message.content);
            message.content = null;
            return msg;
        }
        return null;
    }
    
}


class Message{
    protected Client originator;
    protected String content;
    protected String timestamp;

    Message(Client client, String content){
        this.originator = client;
        this.content = content;
    }
}

