package Servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
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
import Globals.AppConstants;
import Globals.Global;
import Messages.Message;

/**
 * Servlet implementation class lastTenMsgs
 * get HTTP request with uri="lastTenMsgs/channelname/chhhhh/range/0;
 * channelname is the channel needed to fetch from last 10 message
 * range is the range of the message fetched 
 * 0- last 10 messages
 * 10- last 20 messages
 * 20-last 30 messages
 * etc..
 * sorted by date and time
 * the servlet return back to client a collection of message object 
 */
@WebServlet("/lastTenMsgs/*")
public class LastTenMsgs extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public LastTenMsgs() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			//Initialize a connection with database
			Context context = new InitialContext();
			BasicDataSource ds = (BasicDataSource)context.lookup(getServletContext().getInitParameter(AppConstants.DB_DATASOURCE) + AppConstants.OPEN);
			Connection conn = ds.getConnection();
			
			//Initialize array witch will hold msgs
			Collection<Message> lastTenMsgs=new ArrayList<Message>() ;
			//Initialize stack witch will be used to sort relevant messges 
			Deque<Message> stackOfMsg = new ArrayDeque<Message>();

	    	//String urii="lastTenMsgs/channelname/chhhhh/range/0;
			//save relevant information in variables  
			String uri = request.getRequestURI();
			int nameindx=uri.indexOf(AppConstants.CHANNEL);
			int Rangeindx=uri.indexOf(AppConstants.RANGE);
			String CHNAME=uri.substring(nameindx+AppConstants.CHANNEL.length() + 1, Rangeindx-1);
			String Rang=uri.substring(Rangeindx+AppConstants.RANGE.length() + 1);
			
            //get username saved in session
			HttpSession session=request.getSession();
	    	String s=session.getAttribute("user").toString();
	    	String nick=Global.USERS.get(s).getNickname();
	    	
	    	//load subscribe time from DB
			PreparedStatement stmt1 = conn.prepareStatement(AppConstants.SELECT_CHANNELS_BY_CHANNELUSERS_AND_CHANNEL_STMT);
			stmt1.setString(1,nick);
			stmt1.setString(2,CHNAME);
			ResultSet rs1 = stmt1.executeQuery();
			Timestamp subscribeTime=null;
			if(rs1.next()){
				 subscribeTime=rs1.getTimestamp(4);
			}
			
			//load last ten messeges at specific channel
            PreparedStatement stmt = conn.prepareStatement(AppConstants.SELECT_TEN_CHANNEL_MESSAGES);
			stmt.setString(1,CHNAME);
			stmt.setTimestamp(2, subscribeTime);
			stmt.setInt(3,Integer.parseInt(Rang));
			ResultSet rs = stmt.executeQuery();
			
			//load last 10 main messages and push them to stack
			while(rs.next()){
				String timeInMyFormat = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy ").format(rs.getTimestamp(6));
				Message msg=new	 Message (rs.getInt(1), rs.getString(2), rs.getString(3),rs.getInt(4),rs.getString(5),timeInMyFormat);
				stackOfMsg.push(msg);
				}
			
			//load all replays for each main message 
			while(!stackOfMsg.isEmpty()){
				//load all replays for the top message in the stack from DB
				PreparedStatement stmt2 = conn.prepareStatement(AppConstants.SELECT_CHANNEL_MESSAGES_BY_REPLYTO);
				stmt2.setInt(1,stackOfMsg.peek().getid());
				ResultSet rs2 = stmt2.executeQuery();
				
				//save message in array and pop it from stack
				stackOfMsg.peek().setPicUrl(Global.USERS.get(Global.usernamesNicknamesMap.get(stackOfMsg.peek().getFromUser())).getPhoto());
				lastTenMsgs.add(stackOfMsg.pop());
				
				//push all the replys in the stack 
				while(rs2.next()){
					String timeInMyFormat = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy ").format(rs2.getTimestamp(6));
					Message msg=new	 Message (rs2.getInt(1), rs2.getString(2), rs2.getString(3),rs2.getInt(4),rs2.getString(5), timeInMyFormat);
					stackOfMsg.push(msg);
					}
				
				//close statment
				 stmt2.close();
		         rs2.close();     
			}
		
		    Gson gson = new Gson();   
	        //convert from message collection to json
	        String customerJsonResult = gson.toJson(lastTenMsgs, AppConstants.MSG_COLLECTION);
	        PrintWriter writer = response.getWriter();
	        writer.println(customerJsonResult);
	        
	        //close connection and statments 
	      	writer.close();
            conn.close();
            stmt.close();
            rs.close();
           
		}catch (SQLException | NamingException e) {
			getServletContext().log("Error while querying for customers", e);
    		response.sendError(500);//internal server error
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
