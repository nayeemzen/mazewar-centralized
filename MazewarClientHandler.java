import java.net.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.io.*;

public class MazewarClientHandler implements Runnable {
	
	private final Socket clientSocket;
	private final LinkedBlockingQueue <MazewarPacket> eventQueue;
	private final ConcurrentHashMap <String, ObjectOutputStream> connectedClients;
	private final ObjectInputStream readStream;
	private final ObjectOutputStream writeStream;
	private String clientName;
	
	MazewarClientHandler(Socket clientSocket, ConcurrentHashMap connectedClients, 
			LinkedBlockingQueue eventQueue) throws IOException {
		assert (clientSocket != null) && (connectedClients != null) && (eventQueue != null);
		this.clientSocket = clientSocket;
		this.connectedClients = connectedClients;
		this.eventQueue = eventQueue;
		
		clientName = null;
		readStream = new ObjectInputStream(clientSocket.getInputStream());
		writeStream = new ObjectOutputStream(clientSocket.getOutputStream());
		
		System.out.println("Created new thread to handle client connection");
	}
	
	private void registerClient (MazewarPacket packetFromClient) {
		
			System.out.println("REGISTERING");
			/* Check if client is already registered */
			if (connectedClients.containsKey(packetFromClient.clientName)) {
				handleError(MazewarPacket.ERROR_CLIENT_ALREADY_EXISTS);
				return;
			} else {
				clientName = packetFromClient.clientName;
				connectedClients.put(clientName, writeStream);
			}
	}
	
	/* Client request handler */
	public void handleReceivedPacket(MazewarPacket packetFromClient) throws IOException {
		assert packetFromClient != null;
		System.out.println("Handling Received Packet");
		
		if (packetFromClient.eventType == MazewarPacket.REGISTER) {
			registerClient(packetFromClient);
		}
			
		/* Ensure client has been registered with server */
		assert(connectedClients.containsKey(packetFromClient.clientName));
		
		/* Construct packet */
		MazewarPacket packetToClient = new MazewarPacket();
		packetToClient.clientName = clientName;
		packetToClient.errorCode = 0;
		packetToClient.coord_x = packetFromClient.coord_x;
		packetToClient.coord_y = packetFromClient.coord_y;
		
		if (packetFromClient.eventType == MazewarPacket.REGISTER) {
			packetToClient.eventType = MazewarPacket.BEGIN;
		} else {
			packetToClient.eventType = packetFromClient.eventType;
		}
			
		/* Enqueue packet for the EventQueueListener to broadcast */
		System.out.println("enqueing: " + packetToClient.eventType);
		eventQueue.add(packetToClient);
	}
	
	private void handleError(int errorCode) {
		System.err.println("Error: " + errorCode);
		MazewarPacket packet = new MazewarPacket();
		packet.clientName = clientName;
		packet.eventType = MazewarPacket.ERROR;
		/* TODO(Zen): Finish */
	}

	@Override
	public void run() {
		try {
			MazewarPacket packetFromClient = new MazewarPacket();
			while ((packetFromClient = (MazewarPacket) readStream.readObject()) != null) {
				// TODO println for debug purposes only, remove later
				System.out.println(packetFromClient.clientName);
				handleReceivedPacket(packetFromClient);
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				clientSocket.close();
				readStream.close();
				writeStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
}
