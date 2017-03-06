package WebSocket;


import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import org.apache.tomcat.dbcp.dbcp.BasicDataSource;
import com.google.gson.Gson;
import Globals.AppConstants;
import Globals.Global;
import Messages.Message;
import Messages.OpenChatMsg;
import Messages.SocketMsg;
import Messages.UnseenMsg;
import Messages.UnsubscribeMsg;
import Messages.UpdateChannel;
import Rooms.modle.Rooms;
import Users.modle.*;
/**
 * Server-side WebSocket end-point that managed a chat between 
 * several client end-points
 * @author Adnan Almog
 */
@ServerEndpoint("/chat/{username}")
public class Chat{
	
	//tracks all active chat users
	//map between client session and client object at server end point 
    private static Map<Session,User> chatUsers = Collections.synchronizedMap(new HashMap<Session,User>()); 
	//map between client object at server end point and client session(same as above but in the opposite way)
    //both maps together implement a bidirectional map
    private static Map<User,Session> chatUsersInv = Collections.synchronizedMap(new HashMap<User,Session>()); 
    //map between channelName and Rooms 
    //Rooms holds a map of user 
    //this map hold and track all connected users that available on specific channel
    private static Map<String,Rooms> rooms = Collections.synchronizedMap(new HashMap<String,Rooms>()); 
    //this map hold and track all connected users to webSocket and that not open the channels they subscribe to 
    private static Map<String,Rooms> unconnectedUsersRooms = Collections.synchronizedMap(new HashMap<String,Rooms>());

    /**
     * Joins a new client to chat
     * enter all subscribed channel of the user to unconnectedUsersRooms 
     * if the channel not available create it
     * and thats how user can get notes about messages from other channel 
     * +note the client if there is any unseen messages in channels that the client subscribed to 
     * 
     * @param session 
     * 			client end point session
     * @throws IOException
     */
    @OnOpen
    public void joinChat(Session session, @PathParam("username") String username) throws IOException{
  	try{
		 Connection conn;

    	if (session.isOpen()) {
        //save username and proper client session in the maps
		User SessionUser=Global.USERS.get(username);
		chatUsers.put(session,SessionUser);
		chatUsersInv.put(SessionUser, session);
		 try {
         //inisilaize collection of unseenMsg which will hold the number of unseen messages and mention for each channel
		 Collection<UnseenMsg> channels=new ArrayList<UnseenMsg>() ;
		 //get a connection to database
		 Context context = new InitialContext();
		 BasicDataSource ds = (BasicDataSource)context.lookup("java:comp/env/jdbc/OTCDatasourceOpen");
	      conn = ds.getConnection();
	     //for each channel user subscribed to 
	     //get number of unseen messages and mentions from database 
	     for (Entry<String,Timestamp> entry : SessionUser.getChannelUpdate().entrySet()) {
		   String chanel = entry.getKey();
		   Timestamp date = entry.getValue();
		   //if room created add user into it
		   //otherwise create it and add user into it
			if(unconnectedUsersRooms.containsKey(chanel)){
			  unconnectedUsersRooms.get(chanel).addUser(SessionUser);
			  }else{
			   Rooms room2=new Rooms();
			   room2.addUser(SessionUser);
			   unconnectedUsersRooms.put(chanel,room2 );
			  }	
			
		  //get number of unseen message from database statement 
		  PreparedStatement stmt = conn.prepareStatement(AppConstants.SELECT_MSG_BY_CHANNEL_AND_DATESTMT);
		  stmt.setString(1, chanel);
		  stmt.setTimestamp(2, date);
		  ResultSet rs = stmt.executeQuery();
		  //if there is unseen message check if user mentioned in thos messages 
		  if(rs.next()){
			UnseenMsg updateChannel=new UnseenMsg(chanel,rs.getInt(1));  				   				
			PreparedStatement stmt2 = conn.prepareStatement(AppConstants.SELECT_NOTES_BY_USER_CHANNEL_AND_DATESTMT);
			stmt2.setString(1, SessionUser.getNickname());
			stmt2.setString(2, chanel);
			stmt2.setTimestamp(3, date);
			ResultSet rs2 = stmt2.executeQuery();
			//check if notes is not 0
			 if(rs2.next() && rs2.getInt(1)>0){
			  updateChannel.setnotes(rs2.getInt(1));
			 }
			 //add channel to collection
			 channels.add(updateChannel);
			 stmt2.close();
			 rs2.close();
			 }
		     rs.close();
		   	 stmt.close();				
			}
	        //send client channel collection with unseen messages 
	         Gson gson = new Gson();
	         String msgUpdateJsonResult = gson.toJson(channels, AppConstants.MSG_UPDATE_COLLECTION);
		     msgUpdateJsonResult="channelList "+msgUpdateJsonResult;//here
		     session.getBasicRemote().sendText(msgUpdateJsonResult);

	   		//close connection
	   	   	 conn.close();
	   	  				
				} catch (NamingException | SQLException e) {
					e.printStackTrace();

			  }
		     }
  	       }catch (IOException e) {
    		session.close();


    	}
      }

    /**
     * messages sent from client to server there are 4 kind of message 
     * first-open/close channel for write 
     * second-Message delivery between chat participants at specific channel
     * third-update user channels (subscribe to a new channel)
     * fourth-update user channels (unsubscribe to a new channel)
     * 
     * @param session
     * 			client end point session
     * @param msg
     * 			message sent from client	
     * @throws IOException
     */
    @OnMessage
    public void deliverChatMessege(Session session, String msg) throws IOException{
    	try {
    	if (session.isOpen()) {
        
           	   String msgUpdateJsonResult = msg;
               int typeIndx=msgUpdateJsonResult.indexOf("type");
               String type=msgUpdateJsonResult.substring(typeIndx+6,typeIndx+7);
           	   User user = chatUsers.get(session);
           	    //convert message to openChatMsg object and send it to updateChannelOnlineUsers method
            	if(type.equals("1")){	
            		Gson gson1 = new Gson();
            		OpenChatMsg openMsg1= gson1.fromJson(msgUpdateJsonResult, OpenChatMsg.class); 		
            		updateChannelOnlineUsers(openMsg1,user);
            	}
           	    //convert message to socketMsg object and send it to SaveAndSendMsg method
            	if(type.equals("2")){
            		Gson gson1 = new Gson();
            		SocketMsg Msg1= gson1.fromJson(msgUpdateJsonResult, SocketMsg.class);
            		SaveAndSendMsg(Msg1,user);
            	}
           	    //convert message to updateChannel object and send it to UpdateUserChannel method
            	if(type.equals("3")){
            		Gson gson1 = new Gson();
            		UpdateChannel Msg1= gson1.fromJson(msgUpdateJsonResult, UpdateChannel.class);
            		//check if the channel is a private one 
            		//in this case update message need to send to the other user
            		//in order to notify him about the event 
            		if(Msg1.channelName().contains("@")){
            		String privateChatUserNames[]=	Msg1.channelName().split("@");
            		if(chatUsersInv.containsKey(Global.USERS.get(Global.usernamesNicknamesMap.get(privateChatUserNames[1])))&&chatUsersInv.containsKey(Global.USERS.get(Global.usernamesNicknamesMap.get(privateChatUserNames[2])))){
            			if(user.getNickname().equals(privateChatUserNames[1])){
            				chatUsersInv.get(Global.USERS.get(Global.usernamesNicknamesMap.get(privateChatUserNames[2]))).getBasicRemote().sendText("openPrivateChannel {\"channel\":"+"\""+Msg1.channelName()+"\"}");
            			}else{
            				chatUsersInv.get(Global.USERS.get(Global.usernamesNicknamesMap.get(privateChatUserNames[1]))).getBasicRemote().sendText("openPrivateChannel {\"channel\":"+"\""+Msg1.channelName()+"\"}");
            				}
            			UpdateUserChannel(Msg1,Global.USERS.get(Global.usernamesNicknamesMap.get(privateChatUserNames[1])));
            			UpdateUserChannel(Msg1,Global.USERS.get(Global.usernamesNicknamesMap.get(privateChatUserNames[2])));

            		}else{
                		UpdateUserChannel(Msg1,user);
                		}
            		}else{
            		UpdateUserChannel(Msg1,user);
            		}
            	}
           	    //convert message to unsubscribeMsg object and send it to removeChannel method
            	if(type.equals("4")){
            		Gson gson1 = new Gson();
            		UnsubscribeMsg Msg1= gson1.fromJson(msgUpdateJsonResult, UnsubscribeMsg.class); 		
            		removeChannel(chatUsers.get(session),Msg1.channelName());
    	}
    	}
        } catch (IOException e) {
                session.close();
        }
    	}
  /**
   * remove a client from chat if the connection lost or client close his session 
   * @param session
   * @param t
   * @throws IOException
   */
      
    @OnError
    public void leaveChat(Session session, Throwable t) throws IOException{
    	removeUserFromRooms(session);
    	}
    /**
     * Removes a client from the chat
     * @param session
     * 			client end point session
     * @throws IOException
     */
    @OnClose
    public void close(Session session) throws IOException{
    	removeUserFromRooms(session);
    	}

   /**
    * if client want to open a channel 
    * add it to channel room at Rooms Map and remove him from unconnectedrooms for that specific channel he want to open
    * if client want to close a channel
    * remove him from rooms(open channels) for that specific channel he want to close
    * @param openMsg1
    * holds open/close channel for write client message
    * @param user
    * client server side object representation
    * @throws IOException
    */
    private void updateChannelOnlineUsers(OpenChatMsg openMsg1,User user) throws IOException{
    	//if the message got is for opening channel for write
    	if(openMsg1.isOpened()){
    		//check if the room exist if its exist add the user to the room
    	     if(rooms.containsKey(openMsg1.getChannel())){
    		    rooms.get(openMsg1.getChannel()).addUser(user);
             	}
    	     //create new room object and add the user into it
           	else{
    		Rooms room=new Rooms();
    		room.addUser(user);
    		rooms.put(openMsg1.getChannel(),room );
    		}
    	//remove user from unconnected rooms for that specific channel 
    	//requested to open for write
    	unconnectedUsersRooms.get(openMsg1.getChannel()).removeUser(user);
    	
    	//if the message got is for closing channel for write
    	}else{		
			try {
				Calendar calendar = Calendar.getInstance();
	    		java.util.Date now = calendar.getTime();	
	    		Timestamp currentTimestamp = new Timestamp(now.getTime());
	    		//update last seen channel time
	    		Global.USERS.get(user.getUserName()).updateLastEnterDate(openMsg1.getChannel(), currentTimestamp);
	    		//get a connection to database
	    		Context context = new InitialContext();
				BasicDataSource ds = (BasicDataSource)context.lookup("java:comp/env/jdbc/OTCDatasourceOpen");
				Connection conn = ds.getConnection();
	    		//update last seen channel time in database
				PreparedStatement stmt = conn.prepareStatement(AppConstants.UPDATE_LASTSEEN_DATE);	
					stmt.setTimestamp(1, currentTimestamp);
					stmt.setString(2, openMsg1.getChannel());
					stmt.setString(3, user.getNickname());
					stmt.executeUpdate();
					stmt.close();
					conn.close();
				//remove from rooms 
				//add to unconnected rooms	
				rooms.get(openMsg1.getChannel()).removeUser(user);
		    	unconnectedUsersRooms.get(openMsg1.getChannel()).addUser(user);
		    	//check if room is empty and remove it from map if its the case	
		    	if(rooms.get(openMsg1.getChannel()).getChannelUserNumber()==0){
		    	 rooms.remove(openMsg1.getChannel());
		    	}
			} catch (NamingException | SQLException e) {
				e.printStackTrace();
			}	
    	}
    }
    /**
     * save message in database 
     * saves mentions of users in database 
     * save unseen messages in database
     * send online messages to connected users 
     * for every user subscribed to the channel
     * @param Msg1
     * @param user
     * @throws IOException
     */
    private void SaveAndSendMsg(SocketMsg Msg1,User user) throws IOException{
	 Context context;
	try {
		//hold messages to send to channel users
		Collection<Message> msgsToSend=new ArrayList<Message>() ;
		//used for message sort and rearrange
		Deque<Message> stackOfMsg = new ArrayDeque<Message>();
        
		//get database connection
		context = new InitialContext();
		BasicDataSource ds = (BasicDataSource)context.lookup("java:comp/env/jdbc/OTCDatasourceOpen");
		Connection conn = ds.getConnection();
		
		//save message in database
		PreparedStatement stmt = conn.prepareStatement(AppConstants.INSERT_MSG_STMT);
		Calendar calendar = Calendar.getInstance();
		java.util.Date now = calendar.getTime();	
		Timestamp currentTimestamp = new Timestamp(now.getTime());
		int id=Global.getMsgId();
		String timeInMyFormat = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy ").format(currentTimestamp);

		Message msg=new  Message(id, user.getNickname(), Msg1.getchannelName(),Msg1.getReplyTo(),Msg1.getMsg(),timeInMyFormat);
	     	stmt.setInt(1,id);
			stmt.setString(2,user.getNickname());
			stmt.setString(3,Msg1.getchannelName());
			stmt.setInt(4,Msg1.getReplyTo());
			stmt.setString(5,Msg1.getMsg());
			stmt.setTimestamp(6,currentTimestamp);
			stmt.setTimestamp(7,currentTimestamp);
			stmt.executeUpdate();
			conn.commit();
			stmt.close();
			
		   int i;
		   //holds usernames mentioned in the message
           Map<String,Integer> userToNotify = Collections.synchronizedMap(new HashMap<String,Integer>()); 
		   
           //check and save users that have been mentioned in the message
			String words[]=msg.getText().split(" ");
			for(i=0;i<words.length;i++){
				if(!words[i].equals("")&&words[i].charAt(0)=='@'){
					words[i]=words[i].replace("@","");
					//save mention statement in database
					PreparedStatement stmt2 = conn.prepareStatement(AppConstants.INSERT_NOTE_STMT);
					stmt2.setInt(1,id);
					stmt2.setString(2,words[i]);
					stmt2.setString(3,Msg1.getchannelName());
					stmt2.setTimestamp(4,currentTimestamp);
					stmt2.executeUpdate();
					conn.commit();
					stmt2.close();
					userToNotify.put(words[i], i);
				}
			}
			//get all message sequence from database if the message is a replay to other message
			int replyTomsgID=Msg1.getReplyTo();
			int rootMsgId=0;
			if(replyTomsgID!=-1){
			while(replyTomsgID!=-1){
              PreparedStatement stmt2 = conn.prepareStatement(AppConstants.SELECT_CHANNEL_MESSAGES_BY_MSGID);
				stmt2.setInt(1,replyTomsgID);
				ResultSet rs2 = stmt2.executeQuery();
				if(rs2.next()){
					String timeInMyFormat2 = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy ").format(rs2.getTimestamp(6));
					Message rootMsg=new	 Message (rs2.getInt(1), rs2.getString(2), rs2.getString(3),rs2.getInt(4),rs2.getString(5),timeInMyFormat2);
					replyTomsgID=rootMsg.getReplyTo();
					if(replyTomsgID==-1){
						rootMsgId=rootMsg.getid();
						rootMsg.setPicUrl(Global.USERS.get(Global.usernamesNicknamesMap.get(rootMsg.getFromUser())).getPhoto());
						stackOfMsg.push(rootMsg);

					}
				}
				stmt2.close();
				rs2.close();
				}
			///UPDATE ORIGINAL MSG LASTREPLYTO DATE
			 PreparedStatement stmt2 = conn.prepareStatement(AppConstants.UPDATE_LAST_REPLYTIME_DATE);
			 stmt2.setTimestamp(1, currentTimestamp);
				stmt2.setInt(2,rootMsgId);
				stmt2.executeUpdate();
				conn.commit();
				stmt2.close();
				//GET ALL REPLAYS OF THE ROOT MSG SORTED
				while(!stackOfMsg.isEmpty()){
					int nextMsgId=stackOfMsg.peek().getid();
					
					msgsToSend.add(stackOfMsg.pop());
					/////get replay msgs from db
					 PreparedStatement stmt3 = conn.prepareStatement(AppConstants.SELECT_CHANNEL_MESSAGES_BY_REPLYTO);
						stmt3.setInt(1,nextMsgId);
						ResultSet rs3 = stmt3.executeQuery();
						while(rs3.next()){
							String timeInMyFormat3 = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy ").format(rs3.getTimestamp(6));
				            Message replyMsg=new Message (rs3.getInt(1), rs3.getString(2), rs3.getString(3),rs3.getInt(4),rs3.getString(5), timeInMyFormat3);
				            replyMsg.setPicUrl(Global.USERS.get(Global.usernamesNicknamesMap.get(replyMsg.getFromUser())).getPhoto());	
				            stackOfMsg.push(replyMsg);
			       			}
						rs3.close();
						stmt3.close();
						
				}
			}
			else{
				msg.setPicUrl(Global.USERS.get(Global.usernamesNicknamesMap.get(msg.getFromUser())).getPhoto());
				msgsToSend.add(msg);
			}
			conn.close();
			notifyRoom( msgsToSend,rooms.get(msg.getChannelName()),id);
			updateUnseenMsgs(unconnectedUsersRooms.get(msg.getChannelName()),msg.getChannelName(),userToNotify);
	} catch (NamingException | SQLException e) {
		e.printStackTrace();
	}
		
    }
    /**
     * send message to all connected users 
     * @param msgs
     * collection of messages to send to online users 
     * @param room
     * hold online users that open for write the channel that the message sent to
     * @param id
     * 
     * @throws IOException
     */
    private void notifyRoom(Collection<Message> msgs, Rooms room,int id)throws IOException{
		  try {
			Gson gson = new Gson();
			String msgToSend = gson.toJson(msgs,AppConstants.MSG_COLLECTION);
			for (Entry<String,User> entry : room.getAllOnlineUsers().entrySet()) {
			   if(chatUsersInv.get(entry.getValue()).isOpen()){
				if(!(msgToSend.startsWith("chatMsg")))
				 msgToSend="chatMsg"+id+" "+msgToSend;//here
			     chatUsersInv.get(entry.getValue()).getBasicRemote().sendText(msgToSend);
				  }
			  }
		} catch (IOException e) {
			e.printStackTrace();
		} 
	  }
    /**
     * send online users that subscribed to channel update for unseen message
     * @param room
     * online users that subscribe to channel but didnt open it 
     * @param channelName
     * channel name to notify users in
     * @param mentionedUsers
     * a map of users that have been mentioned in the message
     * @throws IOException
     */
    private void updateUnseenMsgs( Rooms room,String channelName, Map<String,Integer> mentionedUsers)throws IOException{
	  try {
		  Gson gson = new Gson();
		  UnseenMsg updateMsgChannel=new UnseenMsg(channelName,1);
		  UnseenMsg updateMsgChannelAndUser=new UnseenMsg(channelName,1);
		  updateMsgChannelAndUser.setnotes(1);
		  String msgToSendNoteChannel = gson.toJson(updateMsgChannel,UnseenMsg.class);
		  String msgToSendNoteChannelAndUser = gson.toJson(updateMsgChannelAndUser,UnseenMsg.class);
		  msgToSendNoteChannel="channelUnseenMsgUpdate "+msgToSendNoteChannel;
		  msgToSendNoteChannelAndUser="channelUnseenMsgUpdate "+msgToSendNoteChannelAndUser;
		  for (Entry<String,User> entry : room.getAllOnlineUsers().entrySet()) {
			  if(chatUsersInv.get(entry.getValue()).isOpen()){
				if(  mentionedUsers.containsKey(entry.getValue().getNickname())){
					chatUsersInv.get(entry.getValue()).getBasicRemote().sendText(msgToSendNoteChannelAndUser);
				}else{
					chatUsersInv.get(entry.getValue()).getBasicRemote().sendText(msgToSendNoteChannel);
				}
			  }
		  }
	} catch (IOException e) {
		e.printStackTrace();
	} 
  }
    /**
     * update the list of channels user subscribe to
     * @param Msg
     * hold channels update
     * @param user
     * not updated user object
     * @throws IOException
     */
    private void UpdateUserChannel(UpdateChannel Msg,User user)throws IOException{
    	User updatedUser=Global.USERS.get(user.getUserName());
    	Session prevSession=chatUsersInv.get(user);

    	chatUsers.remove(user);
    	chatUsers.put(prevSession, updatedUser);

    	chatUsersInv.remove(prevSession);
    	chatUsersInv.put(updatedUser, prevSession);

    	if(unconnectedUsersRooms.containsKey(Msg.channelName())){
    		unconnectedUsersRooms.get((Msg.channelName())).addUser(user);
    	}else{
    		Rooms room=new Rooms();
    		room.addUser(updatedUser);
    		unconnectedUsersRooms.put(Msg.channelName(),room );
    	}
    }
    /**
     * remove channel from user channel list
     * use for unsubscribe update
     * @param user
     * @param channel
     * @throws IOException
     */
    
    private void removeChannel(User user,String channel)throws IOException{
    	if(channel.contains("@")){
    		String privateChatUserNames[]=	channel.split("@");
    		if(user.getNickname().equals(privateChatUserNames[1])){
    			if(chatUsersInv.get(Global.USERS.get(Global.usernamesNicknamesMap.get(privateChatUserNames[2]))) != null)
				   chatUsersInv.get(Global.USERS.get(Global.usernamesNicknamesMap.get(privateChatUserNames[2]))).getBasicRemote().sendText("removePrivateChannel {\"channel\":"+"\""+channel+"\"}");
    }else{
    	if(chatUsersInv.get(Global.USERS.get(Global.usernamesNicknamesMap.get(privateChatUserNames[1])))!=null)
		      chatUsersInv.get(Global.USERS.get(Global.usernamesNicknamesMap.get(privateChatUserNames[1]))).getBasicRemote().sendText("removePrivateChannel {\"channel\":"+"\""+channel+"\"}");

    }
}
}
    /**
     * when the connection with client closed/lost
     * remove client from all channels and maps 
     * @param session
     * @throws IOException
     */
    private void removeUserFromRooms(Session session)throws IOException{
    	if(chatUsers.containsKey(session)){
			User user = chatUsers.remove(session);
    	for (Entry<String,Timestamp> entry : user.getChannelUpdate().entrySet()) {
			String chanel = entry.getKey();
			if(rooms.containsKey(chanel)&&rooms.get(chanel).userExist(user)){
				OpenChatMsg openMsg1=new OpenChatMsg(1,false,chanel);
				updateChannelOnlineUsers( openMsg1, user);
				break;
			}
    		}
		chatUsersInv.remove(user);
		User updatedUser=Global.USERS.get(user.getUserName());

		for (Entry<String,Timestamp> entry : user.getChannelUpdate().entrySet()) {
			String chanel = entry.getKey();
			if(unconnectedUsersRooms.containsKey(chanel)){
				unconnectedUsersRooms.get(chanel).removeUser(updatedUser);
			}
    		}
		}
    }
}
