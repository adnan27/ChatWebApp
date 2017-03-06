package Servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Calendar;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.tomcat.dbcp.dbcp.BasicDataSource;

import com.google.gson.Gson;

import Channels.modle.PublicChannel;
import Globals.AppConstants;
import Globals.Global;

/**
 * Servlet implementation class creatPrivateChannel
 *  get HTTP request with uri="creatPrivateChannel/nickname1;
 * 	nickname is the nickname of the user in the private chat     	
 * its create a private channel and send back both clients the details  
 */

@WebServlet(
		description = "Servlet to add new user", 
		urlPatterns = { 
				"/creatPrivateChannel", 
				"/creatPrivateChannel/*"
		})
public class CreatPrivateChannel extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CreatPrivateChannel() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session=request.getSession();
    	String s=session.getAttribute("user").toString();
    	try {
			//Initialize a connection with database
			Context context = new InitialContext();
			BasicDataSource ds = (BasicDataSource)context.lookup(getServletContext().getInitParameter(AppConstants.DB_DATASOURCE) + AppConstants.OPEN);
			Connection conn = ds.getConnection();
			//Initialize string array that will hold users nick names
			String privateChannelUsersName[] = new String[2] ;
	    	//String urii="creatPrivateChannel/nickname1;
			//save relevant information in variables  
			String uri = request.getRequestURI();
	    	int privatChIndx=uri.indexOf(AppConstants.PRIVATECHANNEL);
			privateChannelUsersName[0]=uri.substring(privatChIndx+AppConstants.PRIVATECHANNEL.length() + 1);
			privateChannelUsersName[1]=Global.USERS.get(s).getNickname();
	    	//lexical sort nick names (we assume that nicknames are unique and so we choose private channel name to be the users nick name separated with @ and so its unique)
			Arrays.sort(privateChannelUsersName);
        	String privateChName="@";
            privateChName=privateChName.concat(privateChannelUsersName[0]);
        	privateChName=privateChName.concat("@");
        	privateChName=privateChName.concat(privateChannelUsersName[1]);
        	privateChName=privateChName.concat("@");
        	
        	//check if channel exist
            PreparedStatement stmt = conn.prepareStatement(AppConstants.SELECT_CHANNEL_BY_NAME_STMT);
			stmt.setString(1, privateChName);
			ResultSet rs = stmt.executeQuery();
            PrintWriter writer = response.getWriter();	
				if(rs.next()){
	 	        	writer.println("channel exist");
	 	        	writer.close();
				}
				else{
			    //add channel to DB
        	    stmt = conn.prepareStatement(AppConstants.INSERT_CHANNEL_STMT);
	            stmt.setString(1,privateChName);
				stmt.setString(2,"private chat");
				stmt.setString(3,privateChannelUsersName[0]);
				stmt.executeUpdate();
				conn.commit();
             	stmt.close();
             	
             	//get current time
             	Calendar calendar = Calendar.getInstance();
				java.util.Date now = calendar.getTime();	
				Timestamp currentTimestamp = new Timestamp(now.getTime());
             	//save subscribe details in DB
             	subscribeToChannel( conn, privateChName, privateChannelUsersName[0], currentTimestamp);
             	subscribeToChannel( conn, privateChName, privateChannelUsersName[1], currentTimestamp);
               
             	//update user channels 
			    Global.USERS.get(Global.usernamesNicknamesMap.get(privateChannelUsersName[0])).setChannel(privateChName, currentTimestamp);
		    	Global.USERS.get(Global.usernamesNicknamesMap.get(privateChannelUsersName[1])).setChannel(privateChName, currentTimestamp);
		    	
		    	//send client channel info
    			PublicChannel pc=new PublicChannel("private channel",privateChName,privateChannelUsersName[0]);
		    	writer = response.getWriter();
            	Gson gson = new Gson();
		    	String msgToSendNoteChannel = gson.toJson(pc,PublicChannel.class);
		    	writer.println(msgToSendNoteChannel);
		    	
		    	//close connection and output stream
		    	writer.close();
			    conn.close();
               }
        }catch (NamingException | SQLException e) {
    		e.printStackTrace();
    	}	
    	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
	/**
	 * subscribe to channel and save details in dataBase
	 * @conn
	 * database connection
	 * @privateChName
	 * the name of channel the client subscribe to
	 * @usersName
	 * user name of the client
	 * @currentTimestamp
	 * time of subscribtion 
	 */
    private void subscribeToChannel(Connection conn,String privateChName,String usersName,Timestamp currentTimestamp)throws SQLException{
    	PreparedStatement stmt = conn.prepareStatement(AppConstants.INSERT_CHANNELUSER_STMT);
        stmt.setString(1,privateChName);
		stmt.setString(2,usersName);
		stmt.setTimestamp(3, currentTimestamp);
		stmt.setTimestamp(4, currentTimestamp);
		stmt.executeUpdate();
		stmt.close();
		conn.commit();
    }
}
