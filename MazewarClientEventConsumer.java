import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;


public class MazewarClientEventConsumer implements Runnable {
	
	private boolean isPlayable;
	private GUIClient guiClient;
	private LinkedBlockingQueue <MazewarPacket> eventQueue;
	private Maze maze;
	
	public MazewarClientEventConsumer(GUIClient guiClient,
			Maze maze, LinkedBlockingQueue eventQueue) {
		this.guiClient = guiClient;
		this.eventQueue = eventQueue;
		this.maze = maze;
		guiClient.isPlayable = false;
	}
	
	private void execute(MazewarPacket mazewarPacket) {
		System.out.println("Consuming event: " + mazewarPacket);
		
		// TODO(Zen): Improve isPlayable logic
		if (mazewarPacket.eventType == MazewarPacket.RESUME) {
			guiClient.isPlayable = true;
			// Spawn new RemoteClient for opponents (ignore self)
			if(!mazewarPacket.clientName.equals(guiClient.getName())) {
				RemoteClient opponent = new RemoteClient(mazewarPacket.clientName);
	            maze.addClient(opponent);
			}
			
			return;
		}
		
		if(!guiClient.isPlayable) return;
		
		switch(mazewarPacket.eventType) {
		case MazewarPacket.ACTION_MOVE_DOWN:
			guiClient.backup();
			break;
		case MazewarPacket.ACTION_MOVE_UP:
			guiClient.forward();
			break;
		case MazewarPacket.ACTION_TURN_LEFT:
			guiClient.turnLeft();
			break;
		case MazewarPacket.ACTION_TURN_RIGHT:
			guiClient.turnRight();
			break;
		default:
			System.err.println("Undefined event!");
		}
		
	}
	
	public void run() {
		/* Listen and execute on GUIClient indefinitely */
		while(true) {
			try {
				/* eventQueue.take() blocks if queue is empty */
				execute(eventQueue.take());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
