import java.io.BufferedReader;
import java.io.PrintStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;
import java.net.ServerSocket;

public class ChattyChatChatServer {
	private static ServerSocket serverSocket = null;
	private static Socket  clientSocket = null;
	private static final int maxClientsCount = 15;
	private static clientThread[] threads = new clientThread[maxClientsCount];
	
	public static void main(String args[])
	{
		int portNumber = 10122;
		if (args.length < 1)
		{
			System.out.println("<Using port number: " + portNumber + ">\n");
		}
		else
		{
			portNumber = Integer.valueOf(args[0]).intValue();
		}
		
		try {
			serverSocket = new ServerSocket(portNumber);
		} catch (IOException e) {
			System.out.println(e);
		}
		
		while (true) {
			try {
				clientSocket = serverSocket.accept();
				int i = 0;
				for (i = 0; i < maxClientsCount; i++) {
					if (threads[i] == null) {
						(threads[i] = new clientThread(clientSocket, threads)).start();
						break;
					}
				}
				if (i == maxClientsCount) {
					PrintStream output = new PrintStream(clientSocket.getOutputStream());
					output.println("<Server too busy, maximum 15 clients reached. Try again later.>");
					output.close();
					clientSocket.close();
				}
			} catch (IOException e) {
				System.out.println(e);
			}
		}
	}
}


class clientThread extends Thread{
	public String clientName = null;
	private BufferedReader input = null;
	private PrintStream output = null;
	private Socket clientSocket = null;
	private final clientThread[] threads;
	private int maxClientsCount;
	
	public clientThread(Socket clientSocket, clientThread[] threads) {
		this.clientSocket = clientSocket;
		this.threads = threads;
		maxClientsCount = threads.length;
	}
	
	public void run() {
		int maxClientsCount = this.maxClientsCount;
		int clientNum = -1;
		clientThread[] threads = this.threads;
		
		try {
			input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			output = new PrintStream(clientSocket.getOutputStream());
			String name = null;
			
			for (int i = 0; i < maxClientsCount; i++)
			{
				if (threads[i] != null && threads[i] == this) {
					clientNum = i;
					break;
				}
			}
			if (clientNum != -1) {
				clientName = "Client" + Integer.toString(clientNum);
				name = clientName;
			}
			//welcome client to chat and offer instructions
			output.println("<Welcome " + name + " to the ChattyChatChat.>\n<To leave, enter /quit in a new line.>\n<To set your nickname, enter /nick followed by the name you would like (a single word).>\n<To send a private message, enter /dm followed by the name of the recipient and then the message.>" );
			synchronized (this) {
				for (int i = 0; i < maxClientsCount; i++)
				{
					if (threads[i] != null && threads[i] != this) {
						threads[i].output.println("<A new user " + name + " has entered ChattyChatChat!>" );
					}
				}
			}
			//conversation starts here
			while (true) {
				String line = input.readLine();
				synchronized (this) {
					if (line.startsWith("/quit")) {
						for (int i = 0; i < maxClientsCount; i++)
						{
							if (threads[i]==this)
							{
								output.println("<Bye " + this.clientName +"!>");
								input.close();
								output.close();
								clientSocket.close();
								threads[i] = null;
								break;
							}
						}
						break;
					}
				}
				if (line.startsWith("/dm")) {
					//private message
					String[] arrayOfLine = line.split(" ", 3);
					if (!arrayOfLine[2].isEmpty()) {
						synchronized (this) {
							try {
								for (int i = 0; i < maxClientsCount; i++){
									if (threads[i] != null && threads[i].clientName.contentEquals(arrayOfLine[1])) {
										threads[i].output.println(name + ": " + arrayOfLine[2]);
									}
								}
							} catch (NullPointerException e)
							{
							}
						}
					}
				}
				else if (line.startsWith("/nick")) {
					//setting a nickname
					String[] arrayOfLine = line.split(" ", 3);
					if (this.clientName != null && this.clientName != arrayOfLine[1]) {
						this.clientName = arrayOfLine[1];
					}
					synchronized (this) {
						try {
							for (int i = 0; i < maxClientsCount; i++)
							{
								if (threads[i] != null && threads[i] != this);
								{
									threads[i].output.println("<Thread" + i + ": " + name + " has changed their nickname to: " + this.clientName + ">");
								}
							}
							name = this.clientName;
						} catch (NullPointerException e)
						{
						}
					}
				}
				else
				{
					//general message
					synchronized (this) {
						try {
							for (int i = 0; i < maxClientsCount; i++)
							{
								if (threads[i] != null && threads[i] != this);
								{
										threads[i].output.println(name + ": " + line);
								}
							}
						} catch (NullPointerException e)
						{
						}
					}
				}
			}
			synchronized (this) {
				for (int i = 0; i < maxClientsCount; i++)
				{
					if (threads[i] != null && threads[i] != this)
					{
						threads[i].output.println("<The user " + this.clientName + " has left the chat.>" );
					}
				}
			}
		/*	output.println("<Bye " + this.clientName +"!>");
			synchronized (this) {
				for (int i = 0; i < maxClientsCount; i++) {
					if (threads[i] != null && threads[i] == this){
						threads[i] = null;
						input.close();
						output.close();
						clientSocket.close();
						break;
					}
				}
			} */
			input.close();
			output.close();
			clientSocket.close();
		}catch (IOException e) {
			
		}
	}
	
}