package Globals;

import java.lang.reflect.Type;
import java.util.Collection;
import com.google.gson.reflect.TypeToken;
import Channels.modle.PublicChannel;
import Messages.Message;
import Messages.UnseenMsg;

/**
 * A simple place to hold global application constants
 */
public interface AppConstants {
	public final String CHANNEL = "channelname";
	public final String CHDESCRIPTION = "description";
	public final String NAME = "name";
	public final String USERNAME = "username";
	public final String PASSWORD = "password";
	public final String NICKNAME = "nickname";
	public final String REPLYTO = "replyto";
	public final String MSG = "msg";
	public final String DESCRIPTION = "description";
	public final String PHOTO = "photo";
	public final String SUBSCRIBE = "subscribe";
	public final String UNSUBSCRIBE = "unsubscribe";
	public final String CHANNELNAME = "channelname";
	public final String RANGE = "range";
	public final String PRIVATECHANNEL="creatPrivateChannel";
	public final Type CHANNEL_COLLECTION = new TypeToken<Collection<PublicChannel>>() {}.getType();
	public final Type MSG_COLLECTION = new TypeToken<Collection<Message>>() {}.getType();
	public final Type MSG_UPDATE_COLLECTION = new TypeToken<Collection<UnseenMsg>>() {}.getType();

	//derby constants
	public final String DB_NAME = "DB_NAME";
	public final String DB_DATASOURCE = "DB_DATASOURCE";
	public final String PROTOCOL = "jdbc:derby:"; 
	public final String OPEN = "Open";
	public final String SHUTDOWN = "Shutdown";
	
	//SQL STATMENT FOR NEW USER
	public final String CREATE_USERS_TABLE = "CREATE TABLE USERS(Username varchar(10),"
			+ "Password  varchar(8),"
			+"nickname varchar(20),"
			+"description varchar(50),"
			+"Photo varchar(255),"
			+"PRIMARY KEY(Username))";		
	public final String INSERT_USERS_STMT = "INSERT INTO USERS VALUES(?,?,?,?,?)";
	public final String SELECT_ALL_USERS_STMT = "SELECT * FROM USERS";
	public final String SELECT_USER_BY_NAME_STMT = "SELECT * FROM USERS "
			+ "WHERE Username=?";
	public final String SELECT_USER_BY_NICKNAME_STMT = "SELECT * FROM USERS "
			+ "WHERE nickname=?";
	//creat statment for channels 
	public final String CREATE_CHANNEL_TABLE = "CREATE TABLE CHANNELS(Channelname varchar(30),"
			+ "Description  varchar(500),"
			+"Creator varchar(50),"
			+ "PRIMARY KEY(Channelname))";		
	public final String INSERT_CHANNEL_STMT = "INSERT INTO CHANNELS VALUES(?,?,?)";
	public final String SELECT_ALL_CHANNELS_STMT = "SELECT * FROM CHANNELS";
	public final String SELECT_CHANNEL_BY_NAME_STMT = "SELECT * FROM CHANNELS "
			+ "WHERE Channelname=? ";
	public final String SELECT_CHANNEL_BY_CREATOR_STMT = "SELECT * FROM CHANNELS "
			+ "WHERE Creator=?";
	public  final String DELETE_CHANNEL = "DELETE FROM CHANNELS WHERE Channelname=? ";

	//CREAT STMNT FOR CHANEL USERS
	public final String CREATE_CHANNELUSERS_TABLE = "CREATE TABLE CHANNELUSERS(CHANNEL varchar(255),"
			+ "CHUSER varchar(255),"
			+ "lastSeen TIMESTAMP NOT NULL,"
			+"subscribeDate TIMESTAMP NOT NULL)";	
	public final String INSERT_CHANNELUSER_STMT = "INSERT INTO CHANNELUSERS VALUES(?,?,?,?)";
	public final String SELECT_ALL_CHANNELUSERS_STMT = "SELECT * FROM CHANNELUSERS";
	public final String SELECT_CHANNELUSERS_BY_CHANNELNAME_STMT = "SELECT * FROM CHANNELUSERS "
			+ "WHERE CHANNEL=?";
	public final String SELECT_CHANNELS_BY_CHANNELUSERS_STMT = "SELECT * FROM CHANNELUSERS "
			+ "WHERE CHUSER=?";
	public final String SELECT_CHANNELS_BY_CHANNELUSERS_AND_CHANNEL_STMT = "SELECT * FROM CHANNELUSERS "
			+ "WHERE CHUSER=? AND CHANNEL=?";
	public  final String DELETE_SUBSCRIPTON = "DELETE FROM CHANNELUSERS WHERE CHUSER=? AND CHANNEL=?";
	public  final String UPDATE_LASTSEEN_DATE = "UPDATE CHANNELUSERS SET lastSeen=? WHERE CHANNEL=? AND CHUSER=?";
	public final String SELECT_SUBSCRBTION_NUMBER = "SELECT COUNT(CHUSER) FROM CHANNELUSERS "
			+ "WHERE CHANNEL=?";
	//creat stmnt for msg 
	public final String CREATE_MESSAGES_TABLE = "CREATE TABLE MESSAGES(msgId INTEGER NOT NULL,"
			+ "sentFrom varchar(30),"
			+ "chName varchar(30),"
			+ "replyTo INTEGER ,"
			+ "messageTxt varchar(500) ,"
			+ "msgSendTime TIMESTAMP NOT NULL,"
	        + "lastReplyTime TIMESTAMP NOT NULL,"
	        + "PRIMARY KEY(msgId))";
	public final String INSERT_MSG_STMT = "INSERT INTO MESSAGES VALUES(?,?,?,?,?,?,?)";
	public final String SELECT_ALL_MSG_STMT = "SELECT * FROM MESSAGES ";
	public final String SELECT_LAST_MSG_ID = "SELECT MAX (msgId) FROM MESSAGES";
	public final String SELECT_MSG_BY_CHANNEL_AND_DATESTMT = "SELECT COUNT(msgId) FROM MESSAGES "
			+ "WHERE chName=? AND msgSendTime>?";
	public  final String SELECT_TEN_CHANNEL_MESSAGES = "SELECT * FROM MESSAGES WHERE chName=? AND lastReplyTime>? AND replyTo=-1  ORDER BY lastReplyTime DESC OFFSET ? ROWS FETCH NEXT 10 ROWS ONLY";
	public  final String SELECT_CHANNEL_MESSAGES_BY_REPLYTO = "SELECT * FROM MESSAGES WHERE replyTo=? ORDER BY msgSendTime DESC ";
	public  final String SELECT_CHANNEL_MESSAGES_BY_MSGID = "SELECT * FROM MESSAGES WHERE msgId=? ";
	public  final String UPDATE_LAST_REPLYTIME_DATE = "UPDATE MESSAGES SET lastReplyTime=? WHERE msgId=?";
	//creat stmnt for notify
	public final String CREATE_NOTES_TABLE = "CREATE TABLE NOTES(noteToMsgId INTEGER NOT NULL,"
			+ "userToNotify varchar(30),"
			+ "channelToNotify varchar(30),"
	        + "notifyTime TIMESTAMP NOT NULL)";
	public final String INSERT_NOTE_STMT = "INSERT INTO NOTES VALUES(?,?,?,?)";
	public final String SELECT_ALL_NOTIFY_STMT = "SELECT * FROM NOTES ";
	public final String SELECT_NOTES_BY_USER_CHANNEL_AND_DATESTMT = "SELECT COUNT(noteToMsgId) FROM NOTES "
			+ "WHERE userToNotify=? AND channelToNotify=? AND notifyTime>?";
}
