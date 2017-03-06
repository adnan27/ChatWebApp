package Messages;
/**
 * class that represent a message that we sent to client in order to notify about messages that not seen yet
 * and also the number of unseen messages that such user mentioned in 
 * @author Adnan Almog
 *
 */
public class UnseenMsg {
	/**
	 * holds channelname that the message relevant to 
	 */
private String channel;
    /**
     * holds the number of unseen messages
     */
private int unseenMsgNumber;
    /**
     * holds the number of message user mentioned into and have not been seen 
     */
private int notification;

public UnseenMsg(String channel,int unseenMsgNumber){
	this.unseenMsgNumber=unseenMsgNumber;
	this.channel=channel;
	this.notification=0;
}

public String getchannel(){
	return this.channel;
}

public int getunseenMsgNumber(){
	return this.unseenMsgNumber;
}

public void setnotes(int notification){
	this.notification=notification;
}

public int getNotification(){
	return this.notification;
}
}
