import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;


public class MazewarClientEventConsumer implements Runnable {
	
	private GUIClient guiClient;
	private LinkedBlockingQueue <MazewarPacket> eventQueue;
	private MazeImpl maze;
	private RemoteClient remoteClient;
	
	public MazewarClientEventConsumer(GUIClient guiClient,
			Maze maze, LinkedBlockingQueue eventQueue) {
		this.guiClient = guiClient;
		this.eventQueue = eventQueue;
		this.maze = (MazeImpl) maze;
		remoteClient = null;
		guiClient.isPlayable = false;
	}
	
	private void execute(MazewarPacket mazewarPacket) {
		System.out.println("Consuming event: " + mazewarPacket.eventType + " From: " + mazewarPacket.clientName);
		
		// TODO(Zen): Improve isPlayable logic
		if (mazewarPacket.eventType == MazewarPacket.BEGIN) {
			// Spawn new RemoteClient for opponents (ignore self)
			if(!mazewarPacket.clientName.equals(guiClient.getName())) {
				remoteClient = new RemoteClient(mazewarPacket.clientName);
	            maze.addClient(remoteClient);
			} else { 
				guiClient.isPlayable = true;
				maze.addClient(guiClient);
				synchronized(maze) {
					maze.notify();
				}
			}
			
			return;
		}
		
		if(!guiClient.isPlayable) return;
		
		assert(remoteClient != null);
		boolean isLocalClient = mazewarPacket.clientName.equals(guiClient.getName());
		
		// TODO(Zen): Add firing events
		switch(mazewarPacket.eventType) {
		case MazewarPacket.ACTION_MOVE_DOWN:
			if(isLocalClient)
				guiClient.backup();
			else
				remoteClient.backup();
			break;
		case MazewarPacket.ACTION_MOVE_UP:
			if(isLocalClient)
				guiClient.forward();
			else
				remoteClient.forward();
			break;
		case MazewarPacket.ACTION_TURN_LEFT:
			if(isLocalClient)
				guiClient.turnLeft();
			else
				remoteClient.turnLeft();
			break;
		case MazewarPacket.ACTION_TURN_RIGHT:
			if(isLocalClient)
				guiClient.turnRight();
			else
				remoteClient.turnRight();
			break;
		case MazewarPacket.ACTION_FIRE_PROJECTILE:
			if(isLocalClient) {
				System.out.println(guiClient.getName() + "fired");
				guiClient.fire();
			}
			else {
				System.out.println(remoteClient.getName() + "fired");
				remoteClient.fire();
			}
			break;
		case MazewarPacket.ACTION_MISSILE_TICK:
			maze.missileTick();
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
