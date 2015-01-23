import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class MazewarServer {
	
	public ServerSocket serverSocket;
	/* Use LinkedBlockingQueue as it is an unbounded producer-consumer queue */
	private LinkedBlockingQueue <MazewarPacket> eventQueue;
	private ConcurrentHashMap <String, ObjectOutputStream> connectedClients;
	
	MazewarServer(int port) throws IOException {
		serverSocket = new ServerSocket(port);
		eventQueue = new LinkedBlockingQueue <MazewarPacket>();
		connectedClients = new ConcurrentHashMap <String, ObjectOutputStream>();
	}
	
	/* Start server */
	public void run () throws IOException {
		assert serverSocket != null;
		
		/* Spawn thread for listening to changes in the event queue */
		(new Thread (new EventQueueListener(eventQueue, connectedClients))).start();
		
		/* Indefinitely accept new clients */
		while(true) {
			Socket clientSocket = null;
			try {
				clientSocket = serverSocket.accept();
			} catch (IOException e) {
				System.err.println("Error: error accepting client connection");
				e.printStackTrace();
			}
			
			/* Spawn thread for handling the new client connection */
			(new Thread (new MazewarClientHandler(clientSocket, connectedClients, eventQueue))).start();
		}
	}
	
	public static void main (String [] args) throws IOException {
		
		if (args.length != 1) {
			System.err.println("Error: expected <port> as argument");
			System.exit(-1);
		}
		
		int port = Integer.parseInt(args[0]);
		(new MazewarServer(port)).run();
	}
}
