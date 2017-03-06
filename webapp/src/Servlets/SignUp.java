package Servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.tomcat.dbcp.dbcp.BasicDataSource;
import Globals.AppConstants;
import Globals.Global;
import Users.modle.*;
/**
 * Servlet implementation class SignUp
 * get HTTP request with uri="signup/username/adnan/password/123/nickname/ahbl/description/here you go/photo/mypic.gif;
 * if username or nickname exist send client note
 * otherwise create and save new user in database 
 */
@WebServlet(
		description = "Servlet to add new user", 
		urlPatterns = { 
				"/signup", 
				"/signup/*"
		})
public class SignUp extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SignUp() {
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
			
			//String urii="signup/username/adnan/password/123/nickname/ahbl/description/here you go/photo/mypic.gif";
			//save relevant information in variables  
			String uri = request.getRequestURI();
			int userindx=uri.indexOf(AppConstants.USERNAME);
			int passindx=uri.indexOf(AppConstants.PASSWORD);
			int nickindx=uri.indexOf(AppConstants.NICKNAME);
         	int descriptionindx=uri.indexOf(AppConstants.DESCRIPTION);
         	int photoindx=uri.indexOf(AppConstants.PHOTO);
			String username=uri.substring(userindx+AppConstants.USERNAME.length() + 1, passindx-1);
			String pass=uri.substring(passindx+ AppConstants.PASSWORD.length() + 1, nickindx-1);
			String nick=uri.substring(nickindx+ AppConstants.NICKNAME.length() + 1, descriptionindx-1);
			String description=uri.substring(descriptionindx+ AppConstants.DESCRIPTION.length() + 1, photoindx-1);
			description.replaceAll("%20", " ");
			String photo=uri.substring(photoindx+AppConstants.PHOTO.length() + 1);
			//creat new user
            User user=new User(username,pass,nick,description,photo);
            
            try {
				PrintWriter writer = response.getWriter();

            	//check if username exist
                PreparedStatement stmt = conn.prepareStatement(AppConstants.SELECT_USER_BY_NAME_STMT);
				stmt.setString(1, user.getUserName());
				ResultSet rs = stmt.executeQuery();
				if(rs.next()){
	 	        	writer.print("user already exist");
				}
				else {
				//check if nickname exist
				PreparedStatement stmt2 = conn.prepareStatement(AppConstants.SELECT_USER_BY_NICKNAME_STMT);
				stmt2.setString(1, user.getNickname());
				ResultSet rs2 = stmt2.executeQuery();
				if(rs2.next()){
					writer.print("nickname already exist");	
				}else{
					//add new user to DB
		            stmt = conn.prepareStatement(AppConstants.INSERT_USERS_STMT);
		            stmt.setString(1,user.getUserName());
	 				stmt.setString(2,user.getPassword());
	 				stmt.setString(3,user.getNickname());
	 				stmt.setString(4,user.getDescription());
	 				stmt.setString(5,user.getPhoto());
	 				stmt.executeUpdate();
	 				conn.commit();
	 				
	 				//add user to globals
	 				//System.out.println(user.getUserName()+"///"+user.getNickname());
	 				
	 				Global.USERS.put(user.getUserName(), user);
	 				Global.usernamesNicknamesMap.put(user.getNickname(), user.getUserName());
	 				
	 				//SEND CLIENT SUCSSUCE
	 		      	writer.print("user added successfuly");		
				}
				rs2.close();
				stmt2.close();
				}
						
	    		//close connection
				rs.close();
				stmt.close();
				writer.close();
	    		conn.close();

			}  catch (SQLException e) {
				getServletContext().log("Error while querying for customers", e);
	    		response.sendError(500);//internal server error
			}
	     	} catch (NamingException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
