package Rooms.modle;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import Users.modle.User;
/**
 * class that holds connected users to some channel
 * @author Adnan Almog
 *
 */
public class Rooms {
	/**
	 * a map between username and his proper User object
	 * its hold all the connected Users to some channel
	 */
    private  Map<String,User> roomUsers = Collections.synchronizedMap(new HashMap<String,User>()); 
    
    public Rooms(){}
    
    public void addUser(User user1){
    	roomUsers.put(user1.getNickname(), user1);
    }
    
    public void removeUser(User user1){
    	if(roomUsers.containsKey(user1.getNickname())){
        	roomUsers.remove(user1.getNickname());
    	}
    }
    
    public boolean userExist(User user1){
    	if(roomUsers.containsKey(user1.getNickname())){
    		return true;
    	}else{
    		return false;
    	}
    }
    
    public int getChannelUserNumber(){
    	return roomUsers.size();
    }
    
    public Map<String,User> getAllOnlineUsers(){
    	return roomUsers;
    }
}
