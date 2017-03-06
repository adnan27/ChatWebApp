package Messages;
/**
 * class that represent a message that sent from client in order to notify about user unsubscription to channel  
 * @author Adnan Almog
 *
 */
public class UnsubscribeMsg {
	/**
	 * indicates if the the message sent from the client is a unsubscription update 
	 */
	private int type;
	/**
	 * holds channelname that the user unsubscribe to 
	 */
	private String channelName;
	
	public UnsubscribeMsg(int Type,String channelName){
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
