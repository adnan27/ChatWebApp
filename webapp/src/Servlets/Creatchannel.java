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
import Channels.modle.PublicChannel;
import Globals.AppConstants;
import Globals.Global;

/**
 * Servlet implementation class Creatchannel
 * get HTTP request with uri="creatchannel/channelname/ch/description/dasdasd sda
 * add channel to DB and notify user if the channel added succesfuly
 */

@WebServlet(
		description = "Servlet to add new user", 
		urlPatterns = { 
				"/creatchannel", 
				"/creatchannel/*"
		})
public class Creatchannel extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Creatchannel() {
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
			BasicDataSource ds = (BasicDataSource)context.lookup(
					getServletContext().getInitParameter(AppConstants.DB_DATASOURCE) + AppConstants.OPEN);
			Connection conn = ds.getConnection();
	    	//String uri="creatchannel/channelname/chhhhh/description/dasdasd sda;
			//save relevant information in variables  
			String uri = request.getRequestURI();
			int nameindx=uri.indexOf(AppConstants.CHANNEL);
			int descindx=uri.indexOf(AppConstants.CHDESCRIPTION);
			String CHNAME=uri.substring(nameindx+AppConstants.CHANNEL.length() + 1, descindx-1);
			String CHdes=uri.substring(descindx+AppConstants.CHDESCRIPTION.length() + 1);
			CHdes=CHdes.replaceAll("%20", " ");
			PublicChannel ch= new PublicChannel(CHdes,CHNAME,s);
			
			try {     
				PrintWriter writer = response.getWriter();
				//check if channel exist already 
                PreparedStatement stmt = conn.prepareStatement(AppConstants.SELECT_CHANNEL_BY_NAME_STMT);
				stmt.setString(1, CHNAME);
				ResultSet rs = stmt.executeQuery();
				if(rs.next()){
					System.out.println("channel exist");
	 	        	writer.println("channel exist");
				}else{   
				//insert channel to DB
                PreparedStatement   stmt2 = conn.prepareStatement(AppConstants.INSERT_CHANNEL_STMT);
	            stmt2.setString(1,ch.getChannelName());
				stmt2.setString(2,ch.getDescription());
				stmt2.setString(3,ch.getcreator());
				stmt2.executeUpdate();
				conn.commit();
				stmt2.close();
				
				//the user that add the channel subscribed automatically to the channel 
				//insert subscribe info to DB
				PreparedStatement	stmt3 = conn.prepareStatement(AppConstants.INSERT_CHANNELUSER_STMT);
	            stmt3.setString(1,ch.getChannelName());
				stmt3.setString(2,Global.USERS.get(ch.getcreator()).getNickname());
				Calendar calendar = Calendar.getInstance();
				java.util.Date now = calendar.getTime();	
				Timestamp currentTimestamp = new Timestamp(now.getTime());
				stmt3.setTimestamp(3, currentTimestamp);
				stmt3.setTimestamp(4, currentTimestamp);
				stmt3.executeUpdate();
				conn.commit();
				stmt3.close();
				
				//update user channels at globals map
				Global.USERS.get(ch.getcreator()).setChannel(ch.getChannelName(),currentTimestamp);
				
				//SEND CLIENT SUCSSUCE
	        	writer.print("channel added successfully");
	     
				rs.close();
				stmt.close();
				writer.close();
	    		//close connection
	    		conn.close();
				}
			}catch (SQLException e){
				getServletContext().log("Error while querying for user", e);
	    		response.sendError(500);//internal server error
			}	
         	}catch ( NamingException |SQLException e){
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
