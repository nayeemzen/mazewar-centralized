import java.io.Serializable;

public class MazewarPacket implements Serializable {
	
	/* Client action events */
	public static final int ACTION_TURN_LEFT  		= 101;
	public static final int ACTION_TURN_RIGHT 		= 102;
	public static final int ACTION_MOVE_UP    		= 103;
	public static final int ACTION_MOVE_DOWN  		= 104;
	public static final int ACTION_FIRE_PROJECTILE	= 105;
	
	/* Event to sync projectile movements between clients */
	public static final int SYNC_PROJECTILE_MOVEMENTS = 200;
	
	/* Register/quit events */
	public static final int REGISTER = 300;
	public static final int QUIT	 = 301;
	
	/* Error codes */
	public static final int ERROR_TIMEOUT = -100;
	
	/* Packet information variables */
	public int syncNumber;
	public int eventType;
	public int errorCode;
	
	/* Client information variables */
	public String clientName;
	public int score;
	public Point coordinates;
	
}
