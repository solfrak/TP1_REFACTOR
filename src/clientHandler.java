import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.Socket;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class clientHandler extends Thread
{
	private Socket socket;
	private int clientID; 
	
	private File file;
	DataOutputStream out;
	DataInputStream in;
	private String clientPath;
	public clientHandler(Socket socket, int clientID) throws URISyntaxException
	{
		this.socket = socket;
		this.clientID = clientID;
		
		System.out.println("New connection with client #" + this.clientID + " at " + this.socket);
		
		clientPath = server.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
		clientPath += "ServerRoot/";
		
		file = new File(clientPath);
	}
	
	private void ls() throws Exception
	{
		String[] directory = file.list();
		String ans = "";
		for(String i: directory)
		{
			File temp = new File(clientPath + i);
			if(temp.isDirectory())
			{
				ans += "\t[folder]" + i + "\n";
			}
			else
			{
				ans += "\t" + i + "\n";
			}
		}
		out.writeUTF(ans);
	}
	
	private void cd(String folder) throws Exception
	{
		if(folder.equals(".."))
		{
			String temp = clientPath.substring(0, clientPath.lastIndexOf("/"));
			int index = temp.lastIndexOf("/") + 1;
			String lastFolder = temp.substring(index, temp.lastIndexOf(""));
			if(!lastFolder.equals("ServerRoot"))
			{
				temp = temp.substring(0, temp.lastIndexOf("/"));
				clientPath = temp + "/";
				file = new File(clientPath);
			}
			
			out.writeUTF("CD effectué");
		}
		else 
		{
			File temp = new File(clientPath + folder);
			if(temp.exists())
			{
				if(temp.isDirectory())
				{
					file = temp;
					clientPath += folder + "/";
					out.writeUTF("CD effectué");					
				}
				else
				{
					out.writeUTF("impossible de cd dans un fichier");
				}
			}
			else
			{
				out.writeUTF("Le dossier spécifier n'existe pas");
			}
		}
	}
	
	private void mkdir(String folder) throws Exception
	{
		File temp = new File(clientPath + folder);
		if(temp.mkdir())
		{
			out.writeUTF("mkdir effectué");
		}
		else
		{
			out.writeUTF("le dossier existe déjà");
		}
	}
	
	private void delete(String folder) throws Exception
	{
		File temp = new File(clientPath + folder);
		if(temp.delete())
		{
			out.writeUTF("delete effectué");
		}
		else out.writeUTF("impossible de supprimer le dossier/fichier");
	}
	
	private void upload(String fileName) throws Exception
	{
		int size = (int) in.readLong();
		FileOutputStream fos = new FileOutputStream(clientPath + fileName);
		byte[] buffer = new byte[4096];
		int read = 0;
		int remaining = size;
		
		while((read = in.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
			remaining -= read;
			fos.write(buffer, 0, read);
		}
		fos.close();
		out.writeUTF("upload received");
	}
	
	private void download(String fileName, boolean isZip) throws Exception
	{
		if(isZip)
		{
			//Create zip folder
			FileOutputStream fos = new FileOutputStream("compressed.zip");
			ZipOutputStream zipOut = new ZipOutputStream(fos);
			
			File tempFile = new File(clientPath + fileName);
			FileInputStream fis = new FileInputStream(clientPath + fileName);
			
			ZipEntry zipEntry = new ZipEntry(tempFile.getName());
			zipOut.putNextEntry(zipEntry);
			
			byte[] buffer = new byte[4096];
			int lenght;
			
			while((lenght = fis.read(buffer)) > 0)
			{
				zipOut.write(buffer, 0, lenght);
			}
			zipOut.close();
			fis.close();
			fos.close();
		
			File zipFile = new File("compressed.zip");
			FileInputStream fis2 = new FileInputStream(zipFile);
			
			out.writeLong(zipFile.length());
			byte[] buffer2 = new byte[4096];
			int count;
			while((count = fis2.read(buffer2)) > 0)
			{
				out.write(buffer2, 0, count);
			}
			fis2.close();
			
		}
		else
		{
			File f = new File(clientPath + fileName);
			FileInputStream fis = new FileInputStream(f);
			
			out.writeLong(f.length());
			byte[] buffer = new byte[4096];
			int count;
			while((count = fis.read(buffer)) > 0)
			{
				out.write(buffer, 0, count);
			}
			fis.close();
		}
		out.writeUTF("download received");
	}
	
	
	private void processMessage(String message) throws Exception
	{
		String[] s = message.split(" ");
		
		switch (s[0])
		{
			case "ls": 
				ls();
				break;
			case "cd":
				if(s.length > 1)
				{
					cd(s[1]);					
				}
				else out.writeUTF("Commande impossible");
				break;
			case "mkdir":
				if(s.length > 1)
				{
					mkdir(s[1]);					
				}
				else out.writeUTF("Commande impossible");
				break;
			case "delete":
				if(s.length > 1)
				{
					delete(s[1]);					
				}
				else out.writeUTF("Commande impossible");
				break;
			case "upload":
				if(s.length > 1)
				{
					upload(s[1]);					
				}
				else out.writeUTF("Commande impossible");
				break;
			case "download":
				if(s.length > 1)
				{
					if(s.length > 2)
					{
						download(s[1], true);
					}
					else {
						download(s[1], false);
					}
				}
				else out.writeUTF("Commande impossible");
				break;
		}
	}
	
	private void losMessage(String message)
	{
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-DD @ HH:mm:ss");
		String date = LocalDateTime.now().format(dtf).toString();
		String logMessage = "[" + socket.getInetAddress().toString().replaceAll("/", "") +":" + socket.getPort() + "//" + date + "] ";
		logMessage += message;
		System.out.println(logMessage);
	}
	
	public void run()
	{
		try 
		{
			out = new DataOutputStream(socket.getOutputStream());
			in = new DataInputStream(socket.getInputStream());
			
			out.writeUTF("La connection avec le serveur est établie");
			
			while(true)
			{
				String messageReceived = in.readUTF();
				losMessage(messageReceived);
				processMessage(messageReceived);
			}
		}
		catch(Exception e)
		{
			System.out.println(e);
		}
	}
}