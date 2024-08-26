
import java.net.*; // for Socket
import java.io.*; // for IOException and Input/OutputStream
import java.util.Scanner;

public class ChatClient {

    private static final int BUFFERSIZE = 1024;

    private static String userName;

    private static Boolean isRunning = true;

    public static void main(String[] args) throws IOException {
        String server = "localhost";        
        int servPort = 7600;

        Scanner sc = new Scanner(System.in);

        System.out.print("Type your username: ");
        userName = sc.next();


        Socket socket = new Socket(server, servPort);
        System.out.println("Connected to server at " + new String(socket.getInetAddress().getHostAddress()) + 
        ":" + (socket.getPort()) + " as " + userName);


        InputStream in = socket.getInputStream();
        OutputStream out = socket.getOutputStream();
        
        //Sends username to server
        out.write(userName.getBytes());


        Thread readThread = new Thread() {
            public void run(){
                while(!socket.isClosed()){
                    byte[] byteBuffer = new byte[BUFFERSIZE];
                    try{
                        in.read(byteBuffer);
                        System.out.print("\b\b\b\b\b\b\b\b\b\b\b\b\b"); // Deletes "Message: " from the screen
                        System.out.println(new String(byteBuffer));
                        System.out.print("Message: ");
                        
                    }catch(Exception e){
                        System.out.println("Couldn't read from server: " +  e.getMessage());

                    }                    
                    
                    //Thread.sleep(500);
                }   
            }
        };
        readThread.start();

        while (!socket.isClosed()){
            
            System.out.print("Message: ");
            String msg = sc.next();
            if(msg.contains("close")){
                
                socket.close();
            
                return;
            }
            
            out.write(msg.getBytes());
        }
    }
}
