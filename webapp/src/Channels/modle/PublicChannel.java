package Channels.modle;
/**
 * class that represent channel 
 * @author Adnan Almog
 *
 */
public class PublicChannel {
	/**
	 * holds channel creator userName 
	 */
	private String creator;
	/**
	 * holds channel Description 
	 */
	private String description;
	/**
	 * holds channel name 
	 */
	private String channelName;
	/**
	 * holds the number of participant at the channel   
	 */
	private int participantNumber=0;
	
	public PublicChannel(String Description1,String ChannelName1,String creator1) {
		channelName=ChannelName1;
		description = Description1;
		creator=creator1;
	}
	public int getparticipantNumber(){
		return participantNumber;
	}
	public void setparticipantNumber(int PN){
		this.participantNumber=PN;
	}
	public String getDescription(){
		return description;
	}
	public String getcreator(){
		return creator;
	}
	public String getChannelName(){
		return channelName;
	}
	public void setChannelName(String ch){
		 this.channelName=ch;
	}
}
