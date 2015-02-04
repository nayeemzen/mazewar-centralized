import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;


public class MazewarClientEventConsumer implements Runnable {
	
	private boolean isPlayable;
	private GUIClient guiClient;
	private LinkedBlockingQueue <MazewarPacket> eventQueue;
	private Mazewar maze;
	private Maze mazeImpl;
	
	public MazewarClientEventConsumer(GUIClient guiClient,
			Mazewar maze, Maze mazeImpl, LinkedBlockingQueue eventQueue) {
		this.guiClient = guiClient;
		this.eventQueue = eventQueue;
		this.maze = maze;
		this.mazeImpl = mazeImpl;
		guiClient.isPlayable = false;
	}
	
	private void execute(MazewarPacket mazewarPacket) {
		System.out.println("Consuming event: " + mazewarPacket);
		
		// TODO(Zen): Improve isPlayable logic
		if (mazewarPacket.eventType == MazewarPacket.RESUME) {
			guiClient.isPlayable = true;
			// Spawn new GUIClient for opponents (ignore self)
			if(!mazewarPacket.clientName.equals(guiClient.getName())) {
				GUIClient opponent = new GUIClient(mazewarPacket.clientName);
	            mazeImpl.addClient(opponent);
	            maze.addKeyListener(opponent);
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
