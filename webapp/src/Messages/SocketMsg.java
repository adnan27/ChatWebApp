package Messages;
/**
 * class that hold the info of the message sent from client through webSocket 
 * @author Adnan Almog
 *
 */
public class SocketMsg {
	/**
	 * indicates if the the message sent from the client is a text message  
	 */
	private int type;
	/**
	 * holds the channel name of the channel that the message sent to  
	 */
	private String channelName;
	/**
	 *  holds the text message as it wrote by the client 
	 */
	private String message;
	/**
	 *  holds the message id of the mainMessage if this message is a replay to the MainMessage 
	 *  -1 otherwise 
	 */
	private int replyTo;
	
	public SocketMsg(int Type,String channelName,int replyTo,String message){
		this.replyTo=replyTo;
		this.channelName=channelName;
		this.type=Type;
		this.message=message;
	}
	
	public String getMsg(){
		return message;
	}
	
	public int getReplyTo(){
		return replyTo;
	}
	
	public String getchannelName(){
		return channelName;
	}
	
	public int getType(){
		return type;
	}
}
