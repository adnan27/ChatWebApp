package Messages;
/**
 * class that represent message 
 * @author Adnan Almog
 *
 */
public class Message {
	/**
	 * holds message id as it been saved in DB
	 */
	private int id;
	/**
	 * holds the username of the user that sent the message 
	 */
	private String fromUser;
	/**
	 * holds the channelname of the channel that the message sent to 
	 */
	private String channelName;
	/**
	 * holds -1 if the message is not a reply for other message
	 * holds msgId of the MainMessage if the message is a replay to that MainMessage
	 */
	private int replyTo;
	/**
	 *holds the content of the message 
	 */
	private String text;
	/**
	 *holds post time of the message 
	 */
	private String time;
	/**
	 * holds a picture url of the message author 
	 */
	private String picture;
	
	public Message (int id, String fromUser, String channelName, int replyTo,String text, String time) {
		this.id = id;
		this.fromUser = fromUser;
		this.channelName = channelName;
		this.replyTo = replyTo;
		this.text = text;
		this.time = time;
	}
	
    public void setPicUrl(String pic){
    	this.picture=pic;
    }
    
	public int getid () {
		return this.id;
	}
	
	public int getReplyTo () {
		return this.replyTo;
	}
	
	public String getFromUser () {
		return this.fromUser;
	}
	
	public String getChannelName () {
		return this.channelName;
	}
	
	public String getText () {
		return this.text;
	}
	
	public String getTime () {
		return this.time;
	}
	
	public String getPic () {
		return this.picture;
	}
}
