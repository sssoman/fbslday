import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.Reader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.servlet.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

public class Main extends AbstractHandler {
	private static final long serialVersionUID = 1L;
	private String code="";
	/* Request parameters*/
    private static final String TOKEN = "token";
    private static final String TEAM_ID = "team_id";
    private static final String TEAM_DOMAIN = "team_domain";
    private static final String CHANNEL_ID = "channel_id";
    private static final String CHANNEL_NAME = "channel_name";
    private static final String USER_ID = "user_id";
    private static final String USER_NAME = "user_name";
    private static final String COMMAND = "command";
    private static final String TEXT = "text";
    private static final String RESPONSE_URL = "response_url";

    /* Response parameters*/
    private static final String RESPONSE_TYPE = "response_type";
    
    private static final String SLACK_CHANNEL_URL = "https://slack.com/api/channels.info";
    private static final String SLACK_USER_URL = "https://slack.com/api/users.info";

	@Override
    public void handle( String target,
                        Request baseRequest,
                        HttpServletRequest req,
                        HttpServletResponse res ) throws IOException,
                                                      ServletException
    {
		SlackRequest sRequest = parseRequest(req);
		String text = sRequest.getText();
		String op = = null;
		if(text != null && !text.isEmpty()){
		final String[] tokens = text.split(" ");
		op = tokens[1];
		}
		if(op.equals("search") || op == null || op.isEmpty()){
		FBGraph fbGraph = new FBGraph("access_token=EAAZAirncyLTwBAHUW6tGXxJ3YhlhkzmnExHp7irya5kw8Fu7ZBJzumiXoq0aZCa7UAk5GDziwJbZCGGnvkAVx6hZAlQFkSKqALZAbqd1KujNKtXUgWlZAfK9ZCUmTcGxWnpdpv6R1zytzcF97rugFONX84tsDS5EqkIHgvdLi58aVmjKxPGqrAww&expires_in=6429");
		String graph = fbGraph.getFBGraph();
		Map<String, String> fbProfileData = fbGraph.getGraphData(graph);
		StringBuilder sb = new StringBuilder();
        sb.append(fbProfileData.get("post0"));
        sb.append("\n");
        sb.append(fbProfileData.get("post1"));
        sb.append("\n");
        SlackResponse sResp = new SlackResponse(sb.toString(), ResponseType.IN_CHANNEL.getValue());
        if (sResp != null) {
			try {
				createResponse(sResp, res);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		}
		else if(op.equals("post")){
			String postId = tokens[2];
			String commentText = tokens[3];
			String u = "https://graph.facebook.com/"+ postId + "/comments?" + "EAAZAirncyLTwBAHUW6tGXxJ3YhlhkzmnExHp7irya5kw8Fu7ZBJzumiXoq0aZCa7UAk5GDziwJbZCGGnvkAVx6hZAlQFkSKqALZAbqd1KujNKtXUgWlZAfK9ZCUmTcGxWnpdpv6R1zytzcF97rugFONX84tsDS5EqkIHgvdLi58aVmjKxPGqrAww&expires_in=6429";
			URL url = new URL(u);
			StringBuilder postData = new StringBuilder();
			Map<String,Object> params = new LinkedHashMap<>();
	        params.put("message", commentText);
	        for (Map.Entry<String,Object> param : params.entrySet()) {
	            if (postData.length() != 0) postData.append('&');
	            postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
	            postData.append('=');
	            postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
	        }
	        byte[] postDataBytes = postData.toString().getBytes("UTF-8");
	        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
	        conn.setRequestMethod("POST");
	        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
	        conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
	        conn.setDoOutput(true);
	        conn.getOutputStream().write(postDataBytes);
	        Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
	        StringBuilder sb = new StringBuilder();
	        for (int c; (c = in.read()) >= 0;)
	            sb.append((char)c);
	        String response = sb.toString();
	        System.out.println("Comment resp" + response);;
		}
	baseRequest.setHandled(true);
		}
		/*// Declare response encoding and types
		res.setContentType("text/html; charset=utf-8");

        // Declare response status code
        res.setStatus(HttpServletResponse.SC_OK);

        // Write back response
        res.getWriter().println("<h1>Hello World</h1>");

        // Inform jetty that this request has now been handled
        baseRequest.setHandled(true);

    }*/

	public static void main( String[] args ) throws Exception
    {
		Server server = new Server(Integer.valueOf(System.getenv("PORT")));
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(new Main());
        server.start();
        server.join();
    }

    /*
	 * Request of the form 
	 * token=gIkuvaNzQIHg97ATvDxqgjtO
       team_id=T0001
       team_domain=example
       channel_id=C2147483705
       channel_name=test
       user_id=U2147483697
       user_name=Steve
       command=/weather
       text=94070
       response_url=https://hooks.slack.com/commands/1234/5678
	 */
	SlackRequest parseRequest(HttpServletRequest request){
		final SlackRequest slackRequest = new SlackRequest();
		if (request != null) {
			slackRequest.setToken(request.getParameter(TOKEN));
			slackRequest.setTeamId(request.getParameter(TEAM_ID));
			slackRequest.setTeamDomain(request.getParameter(TEAM_DOMAIN));
			slackRequest.setChannelId(request.getParameter(CHANNEL_ID));
			slackRequest.setChannelName(request.getParameter(CHANNEL_NAME));
			slackRequest.setUserId(request.getParameter(USER_ID));
			slackRequest.setUserName(request.getParameter(USER_NAME));
			slackRequest.setCommand(request.getParameter(COMMAND));
			slackRequest.setText(request.getParameter(TEXT));
			slackRequest.setResponseUrl(request.getParameter(RESPONSE_URL));
		}
		return slackRequest;
	}
	
	void createResponse(SlackResponse slackResponse, HttpServletResponse response) throws JSONException{
               JSONObject jResp = new JSONObject();
               jResp.put(RESPONSE_TYPE, slackResponse.getResponseType());
               jResp.put(TEXT, slackResponse.getText());
               response.setContentType("application/json");
               response.setStatus(HttpStatus.OK_200);
		try {
			response.getWriter().print(jResp.toString());
		} catch (IOException e) {
			//new SlackResponse(
			//		SlackErrors.BAD_REQUEST.getValue(),
			//		ResponseType.EPHEMERAL.getValue());
		}
	}
}
