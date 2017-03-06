package Servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import Globals.AppConstants;
import Globals.Global;

/**
 * Servlet implementation class isSubscribed
 *  get HTTP request with uri="isSubscribed/nickname/chasss/channelname/ch;
 *  return true if client user subscribe to such a channel 
 *  false otherwise 
 */

@WebServlet(
		description = "Servlet to add new user", 
		urlPatterns = { 
				"/isSubscribed", 
				"/isSubscribed/*"
		})
public class IsSubscribed extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     * 
     */
    public IsSubscribed() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//save relevant information in variables  
		//String urii="isSubscribed/nickname/chasss/channelname/ch;
		String uri = request.getRequestURI();
		int channel=uri.indexOf(AppConstants.CHANNELNAME);
		int nick=uri.indexOf(AppConstants.NICKNAME);
		String nickname=uri.substring(nick+AppConstants.NICKNAME.length() + 1, channel-1);
		String CHNAME=uri.substring(channel+AppConstants.CHANNELNAME.length() + 1);
		//check if user subscribed to channel
		boolean Subscribed =Global.USERS.get(Global.usernamesNicknamesMap.get(nickname)).getChannelUpdate().containsKey(CHNAME);
		//send client answer
		PrintWriter writer = response.getWriter();
		writer.println(Subscribed);
		writer.close();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
