package org.alphapone.dbhm;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.json.JSONObject;


public class Server {	
	public static void main(String []args) 
		throws Exception
	{
		int port = O.getServerPort();
		HttpServer server = HttpServer.create(new InetSocketAddress(port),0);
		server.createContext("/dbhm", new MyHandler());
		server.setExecutor(null);
		System.out.println("");
		System.out.println("Dbhm server started on the port " + port + "...");
		System.out.println("     .... serving resource http://localhost:" + port + "/dbhm");
		System.out.println("   Press ^C or send -9 signal to this process to stop the server");
		server.start();
	}
	static class MyHandler implements HttpHandler {
		@Override 
		public void handle(HttpExchange t) 
			throws IOException
		{
			StringBuilder result = new StringBuilder();
			int resultCode = 200;
			try {
				InputStream input = t.getRequestBody();
				InputStreamReader isr = new InputStreamReader(input);
				BufferedReader br = new BufferedReader(isr);
				StringBuilder isb = new StringBuilder();
				br.lines().forEach(
					(String s)->
					{
						isb.append(s);
						isb.append('\n');
					}
				);
				JSONObject o = new JSONObject(isb.toString());
				Object key = o.opt("key");
				Object value = o.opt("value");
				Object command = o.opt("command");
				if (key!=null) {
					if (value!=null) {
						Dbhm dbhm = Dbhm.getInstance();
						dbhm.putObject(String.valueOf(key),String.valueOf(value));
					} else {
						Dbhm dbhm = Dbhm.getInstance();
						Object fo = dbhm.getObject(String.valueOf(key));						
						result.append(String.valueOf(fo));
						if ("remove".equals(command)) {
							dbhm.removeKey(String.valueOf(key));
						}
					}
				}
			} catch (Exception ex) {
				System.out.println(ex.toString());
				result.append(ex.toString());
				resultCode = 500;
			}
			byte []a=result.toString().getBytes();
			t.sendResponseHeaders(resultCode,a.length);
			OutputStream os = t.getResponseBody();
			os.write(a);
			os.close();
		}
	}
}
