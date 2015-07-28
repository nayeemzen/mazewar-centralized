import java.util.concurrent.LinkedBlockingQueue;

public class MissileTick implements Runnable {
	
	private LinkedBlockingQueue <MazewarPacket> eventQueue;
	
	MissileTick(LinkedBlockingQueue <MazewarPacket> eventQueue) {
		this.eventQueue = eventQueue;
	}
	
	public void run() {
		while (true) {
			MazewarPacket tick = new MazewarPacket();
			tick.eventType = MazewarPacket.ACTION_MISSILE_TICK;
			tick.clientName = "server";
			eventQueue.add(tick);
			try {
				Thread.sleep(200);
	        } catch(Exception e) {
	            // shouldn't happen
	        }
		}
	}
}
