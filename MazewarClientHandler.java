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
		
		readStream = new ObjectInputStream(clientSocket.getInputStream());
		writeStream = new ObjectOutputStream(clientSocket.getOutputStream());
		
		System.out.println("Created new thread to handle client connection");
	}
	
	/* Client request handler */
	public void handleReceivedPacket(MazewarPacket packetFromClient) {
		assert packetFromClient != null;
		
		if (packetFromClient.eventType == MazewarPacket.REGISTER) {
			/* Check if client is already registered */
			if (connectedClients.containsKey(clientName)) {
				handleError(MazewarPacket.ERROR_CLIENT_ALREADY_EXISTS);
				return;
			} else {
				clientName = packetFromClient.clientName;
				connectedClients.put(clientName, writeStream);
			}
		}
		
		/* @TODO(Zen): Wait for (at least) 2 clients to connect */
		if (connectedClients.size() < 2) {
			
		}
		
		/* Enqueue packet */		
		eventQueue.add(packetFromClient);
	}
	
	private void handleError(int errorCode) {
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
