import java.net.*; // for Socket and ServerSocket
import java.io.*; // for IOException
import java.util.Scanner;

class Client{
    protected String userName;
    protected InetAddress  ipAddress;
    protected int port;
    protected Boolean isConnected = true;
    protected int slot;
    
    protected InputStream input;
    protected OutputStream output;
    protected Socket clientSock;

    Client(Socket clientSock){
        this.clientSock = clientSock;
        this.ipAddress = clientSock.getInetAddress();
        this.port = clientSock.getPort();
        try{
            this.input = clientSock.getInputStream();
            this.output = clientSock.getOutputStream();
        }catch(IOException e){
            System.out.println("Couldnt create client: " + e.getMessage());
        }
        
    }

    public String getAddress(){
        return ipAddress + ":" + port;
    }
}

