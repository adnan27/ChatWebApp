package Globals;

import java.util.Map;
import Users.modle.User;

public class Global {
	/**
	 * Map that initialized when the server is up 
	 * its maps each userName to his own User class object witch holds the User details 
	 */
	public static Map<String,User> USERS =null;
	/**
	 * Map that initialized when the server is up 
	 * its maps each userName to the proper nickName
	 */
	public static Map<String,String> usernamesNicknamesMap =null;
	/**
	 *  initialized when the server is up 
	 * its hold the last msgId that already saved on DB
	 */
	private static int MSGID=0;
	private static Object op=new Object();
	/**
	 * a method that return the next msgId 
	 * the method is synchronized so every msg have a unique id
	 */
	public static int getMsgId(){
		 synchronized(op) {
			 MSGID+=1;
		        return MSGID;
		    }
	}
	/**
	 * initialized msgId 
	 * @param msgId 
	 * holds the Number that msgId initialized to
	 */
	public static void msgIdInit(int msgId){
		MSGID=msgId;
	}
	
}
