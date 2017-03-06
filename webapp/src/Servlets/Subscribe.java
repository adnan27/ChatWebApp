package Servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
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
import Globals.AppConstants;
import Globals.Global;

/**
 * Servlet implementation class Subscribe
 * get HTTP request with uri="subscribe/channelname/chhhhh;
 * save subscription of the user to the specific channel in database and send ack
 * 
 */
@WebServlet(
		description = "Servlet to add new user", 
		urlPatterns = { 
				"/subscribe", 
				"/subscribe/*"
		})
public class Subscribe extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Subscribe() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//get username from session
		HttpSession session=request.getSession();
    	String s=session.getAttribute("user").toString();
    	String nick=Global.USERS.get(s).getNickname();
    	
    	try {
			//Initialize a connection with database
			Context context = new InitialContext();
			BasicDataSource ds = (BasicDataSource)context.lookup(getServletContext().getInitParameter(AppConstants.DB_DATASOURCE) + AppConstants.OPEN);
			Connection conn = ds.getConnection();
			
	    	//String urii="subscribe/channelname/chhhhh;
			//save relevant information in variables  
			String uri = request.getRequestURI();
			int descindx=uri.indexOf(AppConstants.SUBSCRIBE);
			String CH=uri.substring(descindx+AppConstants.SUBSCRIBE.length() + 1+12);
			
			//check if user already subscribed to channel
			PreparedStatement stmt1 = conn.prepareStatement(AppConstants.SELECT_CHANNELS_BY_CHANNELUSERS_AND_CHANNEL_STMT);
			stmt1.setString(1, nick);
			stmt1.setString(2, CH);
			ResultSet rs1 = stmt1.executeQuery();
			//add new subscribe statement to DB
			if(!rs1.next()){
		      PreparedStatement stmt = conn.prepareStatement(AppConstants.INSERT_CHANNELUSER_STMT);
              stmt.setString(1,CH);
		   	  stmt.setString(2,nick);
			  Calendar calendar = Calendar.getInstance();
			  java.util.Date now = calendar.getTime();	
			  Timestamp currentTimestamp = new Timestamp(now.getTime());
			  stmt.setTimestamp(3, currentTimestamp);
			  stmt.setTimestamp(4, currentTimestamp);
			  stmt.executeUpdate();
			  conn.commit();
			
			  //send client ack
    		  PrintWriter writer = response.getWriter();
	    	  writer.println("success");

			  //update user channels in globals
			  Global.USERS.get(s).setChannel(CH,currentTimestamp);
			  
    		  //close connection
			  stmt.close();
    		  conn.close();
    		  writer.close();
			}else{
				PrintWriter writer = response.getWriter();
	    		writer.println("user already subscribed to this channel");
	    		writer.close();
			}
			stmt1.close();
			rs1.close();
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

}
