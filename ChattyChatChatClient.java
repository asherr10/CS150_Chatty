import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class ChattyChatChatClient implements Runnable{
	private static Socket clientSocket = null;
	private static PrintStream output = null;
	private static BufferedReader input = null;
	private static BufferedReader inputLine = null;
	private static boolean closed = false;
	
	public static void main(String args[])
	{
		//default host
		String host = "localhost";
		//default port
		int port = 10122;
		
		if (args.length < 2)
		{
			System.out.println("Port being used: " + port + "\nHost being used: " + host);
		}
		else
		{
			host = args[0];
			port = Integer.valueOf(args[1]).intValue();
		}
		
		//create new socket using the host and port
		try
		{
			clientSocket = new Socket(host, port);
			inputLine = new BufferedReader(new InputStreamReader(System.in));
			output = new PrintStream(clientSocket.getOutputStream());
			input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		}
		catch (UnknownHostException e) {
			System.err.println("Host: " + host + " could not be found.\n");
		}
		catch (IOException e) {
			System.err.println("I/O could not be found for host: " + host + "\n");
		}
		
		if (clientSocket != null && input != null && output != null)
		{
			try {
				new Thread(new ChattyChatChatClient()).start();
				while (!closed)
				{
					output.println(inputLine.readLine().trim());
				}
				output.close();
				input.close();
				clientSocket.close();
			} catch (IOException e) {
				System.err.println("IOException: " + e);
			}
		}
		
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		String responseLine;
		try {
			while ((responseLine = input.readLine()) != null) {
				System.out.println(responseLine);
				if (responseLine.startsWith("<Bye"))
				{
					output.close();
					input.close();
					clientSocket.close();
					System.exit(0);
					closed = true;
				}
			}
		} catch (IOException e) {
		}
		
	}
	
}
