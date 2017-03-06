package Servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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
 * Servlet implementation class Unsubscribe
 * get HTTP request with uri="unsubscribe/channelname/chhhhh;
 * remove user subscription to specific channel from database
 */
@WebServlet(
		description = "Servlet to add new user", 
		urlPatterns = { 
				"/unsubscribe", 
				"/unsubscribe/*"
		})

public class Unsubscribe extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Unsubscribe() {
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
			
	    	//String urii="unsubscribe/channelname/chhhhh;
			//save relevant information in variables  
			String uri = request.getRequestURI();
			int descindx=uri.indexOf(AppConstants.UNSUBSCRIBE);
			String CH=uri.substring(descindx+AppConstants.UNSUBSCRIBE.length() + 1+12);
			
			//delete subscription from DB
			PreparedStatement stmt = conn.prepareStatement(AppConstants.DELETE_SUBSCRIPTON);
            stmt.setString(1,nick);
			stmt.setString(2,CH);
			stmt.executeUpdate();
			conn.commit();
			stmt.close();

            //update user channels
			Global.USERS.get(s).DeleteChannels(CH);
			
			//check if the channel is a private one and delete subscription of the other user if its the case
			String nick2="";
			if(CH.contains("@")){
				String users[]=CH.split("@");
				if(users[1].equals(nick)){
					 nick2=users[2];
				}else{
					 nick2=users[1];
				}
				//delete subscription of the other user from DB
				PreparedStatement stmt1 = conn.prepareStatement(AppConstants.DELETE_SUBSCRIPTON);
	            stmt1.setString(1,nick2);
				stmt1.setString(2,CH);
				stmt1.executeUpdate();
				conn.commit();
				stmt1.close();
	            //update user channels
				Global.USERS.get(Global.usernamesNicknamesMap.get(nick2)).DeleteChannels(CH);
                //delete channel
				PreparedStatement stmt2 = conn.prepareStatement(AppConstants.DELETE_CHANNEL);
	            stmt2.setString(1,CH);
				stmt2.executeUpdate();
				conn.commit();
				stmt2.close();
			}
			
    		//send ack to client
    		PrintWriter writer = response.getWriter();
    		writer.println("success");
    		writer.close();
    		//close connection
    		conn.close();
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
