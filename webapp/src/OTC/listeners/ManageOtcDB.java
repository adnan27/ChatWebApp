package OTC.listeners;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.apache.tomcat.dbcp.dbcp.BasicDataSource;
import Globals.AppConstants;
import Globals.Global;
import Users.modle.User;



/**
 * A listener that create derby data base tables and get prober data from database
 * such as our global Maps 
 */
@WebListener
public class ManageOtcDB implements ServletContextListener {

    /**
     * Default constructor. 
     */
    public ManageOtcDB() {
    }
    
    //utility that checks whether the customer tables already exists
    private boolean tableAlreadyExists(SQLException e) {
        boolean exists;
        if(e.getSQLState().equals("X0Y32")) {
            exists = true;
        } else {
            exists = false;
        }
        return exists;
    }

	/**
     * @see ServletContextListener#contextInitialized(ServletContextEvent)
     */
    public void contextInitialized(ServletContextEvent event)  { 
    	ServletContext cntx = event.getServletContext();
    	
    	try{
    		
    		//obtain OtcDB data source from Tomcat's context
    		Context context = new InitialContext();
    		BasicDataSource ds = (BasicDataSource)context.lookup(cntx.getInitParameter(AppConstants.DB_DATASOURCE) + AppConstants.OPEN);
    		Connection conn = ds.getConnection();
    		
            //creat tables
    		CreatTable(AppConstants.CREATE_USERS_TABLE,conn );
    		CreatTable(AppConstants.CREATE_CHANNEL_TABLE,conn );
    		CreatTable(AppConstants.CREATE_MESSAGES_TABLE,conn );
    		CreatTable(AppConstants.CREATE_CHANNELUSERS_TABLE,conn );
    		CreatTable(AppConstants.CREATE_NOTES_TABLE,conn );
    		
            //load users from DB
    		Global.USERS=FillUsers(conn);
    		
    		//close connection
    		conn.close();
    		
    	}catch (SQLException | NamingException ee){}
    
    }

	/**
     * @see ServletContextListener#contextDestroyed(ServletContextEvent)
     */
    public void contextDestroyed(ServletContextEvent event)  { 
   	 ServletContext cntx = event.getServletContext();
   	 
        //shut down database
   	 try {
    		//obtain CustomerDB data source from Tomcat's context and shutdown
    		Context context = new InitialContext();
    		BasicDataSource ds = (BasicDataSource)context.lookup(
    				cntx.getInitParameter(AppConstants.DB_DATASOURCE) + AppConstants.SHUTDOWN);
    		
    		ds.getConnection();
    		ds = null;
		} catch ( NamingException | SQLException e) {
			cntx.log("Error shutting down database",e);
		}

   }
 
    /**
     * a method that check if the table already exist in our derby database
     * if that didnt exist this method create the table 
     * @param table
     * table name
     * @param conn
     * connection to our derby database 
     * @throws SQLException
     */
    private void CreatTable(String table,Connection conn )throws SQLException{
		boolean Tablecreated = false;
		try{
			//create Users table
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(table);
			//commit update
    		conn.commit();
    		stmt.close();
		}catch (SQLException e){
			//check if exception thrown since table was already created (so we created the database already 
			//in the past
			Tablecreated = tableAlreadyExists(e);
			if (!Tablecreated){
				throw e;//re-throw the exception so it will be caught in the
				//external try..catch and recorded as error in the log
			}
		}
    }
    /**
     * get user info from data base and load it in a User object 
     * @param conn
     * connection to our derby database 
     * @return
     * a map between username and its proper Object 
     */
    private	Map<String,User>  FillUsers(Connection conn ){	
        //initilaize maps to fill with users info from DB
    	Map<String,User> US = Collections.synchronizedMap(new HashMap<String,User>());
        Map<String,String> USERNICKMAP = Collections.synchronizedMap(new HashMap<String,String>());

    	try {
		 PreparedStatement stmt = conn.prepareStatement(AppConstants.SELECT_ALL_USERS_STMT);
		 PreparedStatement stmt2 = conn.prepareStatement(AppConstants.SELECT_CHANNELS_BY_CHANNELUSERS_STMT);
		 PreparedStatement stmt3 = conn.prepareStatement(AppConstants.SELECT_LAST_MSG_ID);
			//get last msg id from DB 
			ResultSet rs3 = stmt3.executeQuery();
			if(rs3.next()){
				Global.msgIdInit(rs3.getInt(1));
				}else{
				Global.msgIdInit(0);
			}
			//get users info from DB 
			ResultSet rs = stmt.executeQuery();
			while(rs.next()){
		       User user=new User(rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(4),rs.getString(5));
		       stmt2.setString(1, user.getNickname());
		       ResultSet rs2 = stmt2.executeQuery();
		       while(rs2.next()){
		    	   user.setChannel(rs2.getString(1),rs2.getTimestamp(3));
		        }
		    US.put(user.getUserName(), user);
		    USERNICKMAP.put(user.getNickname(), user.getUserName());
	       }
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	Global.usernamesNicknamesMap=USERNICKMAP;
		return US;
    	}
}

