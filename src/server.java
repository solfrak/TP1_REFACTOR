import java.net.InetAddress;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.io.File;
public class server {

    public static void main(String[] args) throws Exception {
        int clientNumber = 0;
        
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in)); 

        System.out.println("Entrer l'adresse IP du serveur: ");
        String serverAddress = br.readLine();
        System.out.println("Entrer le port du serveur: ");
        int serverPort = Integer.parseInt(br.readLine());
//        String serverAddress = "127.0.0.1";
//        int serverPort = 5030;

        InetAddress serverIP = InetAddress.getByName(serverAddress);
        ServerSocket listener = new ServerSocket();
        listener.setReuseAddress(true);
        listener.bind(new InetSocketAddress(serverIP, serverPort));

        System.out.format("The server is running on %s:%d%n", serverAddress, serverPort);
        
        //Create root folder if not already there
        String jarPath = server.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
        jarPath += "ServerRoot";
        File file = new File(jarPath);
        file.mkdir();
        
        try {
            while (true) {
                // Create a new clientHandler in a new thread
                new clientHandler(listener.accept(), clientNumber++).start();
            }
        } finally {
            listener.close();
        }
    }

}
