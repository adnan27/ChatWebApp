package Messages;
/**
 * class that hold the info of opening and closing channel 
 * @author Adnan Almog
 *
 */
public class OpenChatMsg {
	/**
	 * indicates if the the message sent from the client is for opening/closing channel 
	 */
private int type;
    /**
     * indicates the status of the channel 
     * true - channel open
     * false - channel close
     */
private boolean opened;
    /**
     * holds channelname of the channel that will be close/open
     */
private String channelName;

public OpenChatMsg(int Type,boolean status,String channelName){
	this.opened=status;
	this.channelName=channelName;
	this.type=Type;
}

public String getChannel(){
	return channelName;
}

public boolean isOpened(){
	return opened;
}

public int getType(){
	return type;
}
}
