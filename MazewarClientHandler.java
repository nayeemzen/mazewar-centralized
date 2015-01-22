import java.net.*;
import java.io.*;

public class MazewarClientHandler implements Runnable {
	
	private final Socket clientSocket;
	private final ObjectInputStream readStream;
	private final ObjectOutputStream writeStream;
	
	MazewarClientHandler(Socket clientSocket) throws IOException {
		assert clientSocket != null;
		this.clientSocket = clientSocket;
		readStream = new ObjectInputStream(clientSocket.getInputStream());
		writeStream = new ObjectOutputStream(clientSocket.getOutputStream());
		System.out.println("Created new thread to handle client connection");
	}
	
	/* Client request handler */
	public void handleReceivedPacket(MazewarPacket packetFromClient) {
		// @TODO(Zen): Enqueue received action
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
