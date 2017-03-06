package Messages;
/**
 * class that represent a message that sent from client in order to notify about user subscription to channel  
 * @author Adnan Almog
 *
 */
public class UpdateChannel {
	/**
	 * indicates if the the message sent from the client is a subscription update 
	 */
	private int type;
	/**
	 * holds channelname that the user subscribe to 
	 */
	private String channelName;
	
	public UpdateChannel(int Type,String channelName){
		this.channelName=channelName;
		this.type=Type;
	}
	
	public String channelName(){
		return channelName;
	}
	
	public int getType(){
		return type;
	}
}
