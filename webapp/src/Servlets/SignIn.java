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
import javax.servlet.http.HttpSession;
import org.apache.tomcat.dbcp.dbcp.BasicDataSource;
import Globals.AppConstants;
import Users.modle.*;

/**
 * Servlet implementation class SignIn
 * get HTTP request with uri="signin/username/adnan/password/123;
 * return ack if the user exist and the pass is true
 * otherwise note client about the problem
 */
@WebServlet(
		description = "Servlet to add new user", 
		urlPatterns = { 
				"/signin", 
				"/signin/*"
		})
public class SignIn extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SignIn() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			//get output stream
	    	PrintWriter writer= response.getWriter();

			//Initialize a connection with database
			 Context context = new InitialContext();
			BasicDataSource ds = (BasicDataSource)context.lookup(getServletContext().getInitParameter(AppConstants.DB_DATASOURCE) + AppConstants.OPEN);
			Connection conn = ds.getConnection();
			
			//String urii="signin/username/adnan/password/123;
			//save relevant information in variables  
			String uri = request.getRequestURI();
			int userindx=uri.indexOf(AppConstants.USERNAME);
			int passindx=uri.indexOf(AppConstants.PASSWORD);
			String username=uri.substring(userindx+AppConstants.USERNAME.length() + 1, passindx-1);
			String pass=uri.substring(passindx+AppConstants.PASSWORD.length() + 1);
			pass.replace("/","");
			
		    //get user information from DB
		    PreparedStatement stmt = conn.prepareStatement(AppConstants.SELECT_USER_BY_NAME_STMT);
		    stmt.setString(1, username);
			ResultSet rs = stmt.executeQuery();
			//if user exist
		  if(rs.next()){
			 User user=new User(rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(4),rs.getString(5));
	        if(user.getPassword().equals(pass)){
	           //add user to session 
	           HttpSession session=request.getSession();
	           session.setAttribute("user", user.getUserName());
	           //send ack to client 
	           writer.print("sucess");
	          }else{
		       //pass not match
	           writer.print("worng password");
		                        }
			}else{
					///user not found
					writer.print("user not exist");
				}
		        //close connection
				rs.close();
				stmt.close();
				writer.close();
	    		conn.close();
			} catch (NamingException | SQLException e) {
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
