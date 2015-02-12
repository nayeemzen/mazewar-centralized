import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;


public class MazewarClientEventConsumer implements Runnable {
	
	private GUIClient guiClient;
	private LinkedBlockingQueue <MazewarPacket> eventQueue;
	private MazeImpl maze;
	private HashMap<String, RemoteClient> connectedRemoteClients;
	
	public MazewarClientEventConsumer(GUIClient guiClient,
			Maze maze, LinkedBlockingQueue eventQueue) {
		this.guiClient = guiClient;
		this.eventQueue = eventQueue;
		this.maze = (MazeImpl) maze;
		guiClient.isPlayable = false;
		connectedRemoteClients = new HashMap<String, RemoteClient>();
	}
	
	private void execute(MazewarPacket mazewarPacket) {
		System.out.println("Consuming event: " + mazewarPacket.eventType + " From: " + mazewarPacket.clientName);
		
		RemoteClient remoteClient;
		// TODO(Zen): Improve isPlayable logic
		if (mazewarPacket.eventType == MazewarPacket.BEGIN) {
			// Spawn new RemoteClient for opponents (ignore self)
			if(!mazewarPacket.clientName.equals(guiClient.getName())) {
				remoteClient = new RemoteClient(mazewarPacket.clientName);
				connectedRemoteClients.put(remoteClient.getName(), remoteClient);
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
		
		boolean isLocalClient = mazewarPacket.clientName.equals(guiClient.getName());
		
		if (!isLocalClient && !"server".equals(mazewarPacket.clientName) && !connectedRemoteClients.containsKey(mazewarPacket.clientName)) {
			return;
		} else {
			remoteClient = connectedRemoteClients.get(mazewarPacket.clientName);
		}
		
		
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
		case MazewarPacket.QUIT:
			assert(isLocalClient == false);
			maze.removeClient(remoteClient);
			connectedRemoteClients.remove(remoteClient.getName());
			if (connectedRemoteClients.size() == 0) {
				// No one else is playing anymore, quit the game.
				Mazewar.quit();
			}
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
