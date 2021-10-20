import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class client 
{
	private static Socket socket;
	
	private static DataOutputStream out;
	private static DataInputStream in;
	private static void upload(String fileName) throws Exception
	{
		String clientPath = client.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
		FileInputStream fis = new FileInputStream(clientPath + fileName);
		File file = new File(clientPath + fileName);
		out.writeLong(file.length());
		
		byte[] buffer = new byte[4096];
		int count = 0;
		while((count = fis.read(buffer)) > 0)
		{
			out.write(buffer, 0, count);
		}
		fis.close();
	}
	
	private static void download(String fileName, boolean isZip)
	{
		
	}
	
	public static void main(String[] args) throws Exception
	{
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
//		boolean isConnected = false;
//        while(!isConnected)
//        {
//            try
//            {
//                System.out.println("Entrer l'adresse IP du serveur: ");
//                String serverAddress = br.readLine();
//                System.out.println("Entrer le port du serveur: ");
//                int port = Integer.parseInt(br.readLine());
//                socket = new Socket(serverAddress, port);
//                isConnected = true;	
//            }
//            catch (Exception e) {
//                System.out.println("La connection avec le serveur a echouer. Veuillez-reessayer.");
//            }
//        }
        socket = new Socket("127.0.0.1", 5030);
        
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        System.out.println(in.readUTF());
        while(true)
        {
        	String message = br.readLine();
        	out.writeUTF(message);
        	
        	String[] s = message.split(" ");
        	if(s[0].equals("upload"))
        	{
        		upload(s[1]);
        	}
        	else if(s[0].equals("download"))
        	{
        		if(s.length > 2)
        		{
        			download(s[1], true);        			
        		}
        		else
        		{
        			download(s[1], false);
        		}
        	}
        	System.out.println(in.readUTF());
        }
	        
	}
}