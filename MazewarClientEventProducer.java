import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.concurrent.LinkedBlockingQueue;


public class MazewarClientEventProducer implements Runnable {
	
	private final ObjectInputStream readStream;
	private final LinkedBlockingQueue eventQueue;
	
	MazewarClientEventProducer (ObjectInputStream readStream, LinkedBlockingQueue eventQueue) {
		this.readStream = readStream;
		this.eventQueue = eventQueue;
	}
	
	public void run() {
		try {
			MazewarPacket packetFromClient = new MazewarPacket();
			while ((packetFromClient = (MazewarPacket) readStream.readObject()) != null) {
				eventQueue.add(packetFromClient);
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
}
