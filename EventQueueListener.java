import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class EventQueueListener implements Runnable {
	
	private LinkedBlockingQueue <MazewarPacket> eventQueue;
	private ConcurrentHashMap <String, ObjectOutputStream> connectedClients;
	private AtomicInteger sequenceNumber;
	private final int MIN_CLIENTS = 4;
	
	EventQueueListener(LinkedBlockingQueue eventQueue, ConcurrentHashMap connectedClients) {
		assert (eventQueue != null) && (connectedClients != null);
		this.eventQueue = eventQueue;
		this.connectedClients = connectedClients;
		sequenceNumber = new AtomicInteger(0);
	}
	
	public void broadcast(MazewarPacket packet) throws IOException {
		 /* Tag packet with sequence number */
		packet.sequenceNumber = sequenceNumber.incrementAndGet();
		System.out.println("dequeing: " + packet.eventType + ", "
				+ "sequence num: " + packet.sequenceNumber 
				+ "from: " + packet.clientName);
		
		/* Iterate through HashMap of connected clients and broadcast to each */
		for(ObjectOutputStream writeStream : connectedClients.values()) {
			/* Synchronized write to the writeStream */
			synchronized(writeStream) {
				writeStream.writeObject(packet);
			}
		}
	}
	
	@Override
	public void run() {
		/* Listen and broadcast indefinitely */
		while(true) {
			try {
				/* eventQueue.take() blocks if queue is empty */
				if (connectedClients.size() < MIN_CLIENTS) 
					continue;
				broadcast(eventQueue.take());
			} catch (InterruptedException e){
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
