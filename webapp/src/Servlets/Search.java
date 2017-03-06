package Servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.tomcat.dbcp.dbcp.BasicDataSource;
import com.google.gson.Gson;
import Channels.modle.PublicChannel;
import Globals.AppConstants;

/**
 * Servlet implementation class Search
 * the servlet search for specific its cover to ways of search one by nickname and the other is by channelname
 * HTTP request with	//String urii="search/channelname/chasss;
 * will search for channel that have the specific name that the client search for 
 * HTTP request with	//String urii="search/channelname/chasss;
 *  will return all the channels that such a user subscribed to
 */

@WebServlet(
		description = "Servlet to add new user", 
		urlPatterns = { 
				"/search", 
				"/search/*"
		})
public class Search extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Search() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			//Initialize collection which will hold channels
			Collection<PublicChannel> channels=new ArrayList<PublicChannel>() ;
	    	
			//Initialize a connection with database
			Context context = new InitialContext();
			BasicDataSource ds = (BasicDataSource)context.lookup(getServletContext().getInitParameter(AppConstants.DB_DATASOURCE) + AppConstants.OPEN);
			Connection conn = ds.getConnection();
			
			//String urii="search/channelname/chasss;
			//String urii="search/nickname/chasss;
			//save relevant information in variables  
			String uri = request.getRequestURI();
			int channel=uri.indexOf(AppConstants.CHANNELNAME);
			int nick=uri.indexOf(AppConstants.NICKNAME);
			//check if the search is by nickname or by channel name
            if(nick!=-1){
            //load searched channel from DB by user nickname
       		  String nickname=uri.substring(nick+AppConstants.NICKNAME.length() + 1);
       		  PreparedStatement stmt = conn.prepareStatement(AppConstants.SELECT_CHANNELS_BY_CHANNELUSERS_STMT);
		      stmt.setString(1, nickname);
		      ResultSet rs = stmt.executeQuery();
		      while(rs.next()){
				 //load channel info
                 PreparedStatement stmt2 = conn.prepareStatement(AppConstants.SELECT_CHANNEL_BY_NAME_STMT);
			     stmt2.setString(1, rs.getString(1));
				 ResultSet rs2 = stmt2.executeQuery();
				 //save in collection public channel and private channel of the user
				  while(rs2.next()){
					PublicChannel Pch=new PublicChannel(rs2.getString(2),rs2.getString(1),rs2.getString(3));
					if(Pch.getChannelName().contains("@")){
					}else{
						Pch.setparticipantNumber(selectParticipantNumber(rs.getString(1), conn));
						channels.add(Pch);
					}					
					}
				  //close stmt
					rs2.close();
					stmt2.close();
               }
				rs.close();
				stmt.close();	
               }
            
            //if the search is by channel name
               if(channel!=-1){
       			String channelname=uri.substring(channel+AppConstants.CHANNELNAME.length() + 1);
       			PreparedStatement stmt = conn.prepareStatement(AppConstants.SELECT_CHANNEL_BY_NAME_STMT);
				stmt.setString(1, channelname);
				ResultSet rs = stmt.executeQuery();
				//add channels to the collection
				while(rs.next()){
					if(!rs.getString(1).contains("@")){
					PublicChannel Pch=new PublicChannel(rs.getString(2),rs.getString(1),rs.getString(3));
					Pch.setparticipantNumber(selectParticipantNumber(rs.getString(1), conn));
					channels.add(Pch);
					}
               }
				rs.close();
				stmt.close();
               }
               
               
	       	//convert from channel collection to json
            Gson gson = new Gson();
           	String customerJsonResult = gson.toJson(channels, AppConstants.CHANNEL_COLLECTION);
           	PrintWriter writer = response.getWriter();
           	writer.println(customerJsonResult);
           	writer.close();
	    	//close connection
            conn.close();

			} catch (NamingException | SQLException e) {
		e.printStackTrace();
	}
	
	
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

	private int selectParticipantNumber(String channel, Connection conn)throws SQLException{
		PreparedStatement stmt = conn.prepareStatement(AppConstants.SELECT_SUBSCRBTION_NUMBER);
		stmt.setString(1, channel);
		ResultSet rs = stmt.executeQuery();
		if(rs.next()){
			int res=rs.getInt(1);
			rs.close();
			return res;
		}else{
			return 0;
		}
	}
}
