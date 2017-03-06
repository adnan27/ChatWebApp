package Users.modle;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
/**
 * class that represent a user 
 * @author Adnan Almog
 *
 */
public class User {
	/**
	 * holds user name
	 */
	private String userName;
	/**
	 * holds password
	 */
	private String password;
	/**
	 * holds user nickname
	 */
	private String nickname;
	/**
	 * holds user Description
	 */
	private String description;
	/**
	 * holds user photo
	 */
	private String photo;
	/**
	 * holds channels user subscribed to and last open time of each channel
	 */
    private  Map<String,Timestamp> channelsLastSeen = Collections.synchronizedMap(new HashMap<String,Timestamp>()); 

	public User(String username, String password, String nickname,String description,String photo) {
		this.userName = username;
		this.password = password;
		this.nickname = nickname;
		this.description = description;
		this.photo = photo;
		
	}
	public Map<String,Timestamp> getChannelUpdate(){
		return this.channelsLastSeen;
	}
	public void setChannel(String ch,Timestamp time) {
		
		channelsLastSeen.put(ch, time);
		
	}
	public  Timestamp getLastDate(String ch) {
		return channelsLastSeen.get(ch);
	}
	public  void DeleteChannels(String ch) {
		channelsLastSeen.remove(ch);
		
	}
	
	public  void updateLastEnterDate(String ch,Timestamp time) {
		channelsLastSeen.remove(ch);
		channelsLastSeen.put(ch, time);
	}
	public String getUserName() {
		return userName;
	}

	public String getPassword() {
		return password;
	}

	public String getNickname() {
		return nickname;
	}
	public String getPhoto() {
		return photo;
	}

	public String getDescription() {
		return description;
	}
	
}
