import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;

public class ReporterTask extends TimerTask {

	static String StationID = "KINFORTW142";
	static String StationKey = "";
	
	static int WindDir;
	static float Humidity;
	static float PrecipTotal;
	static float PrecipHour;
	static float Temp;
	static float WindGust;
	static float WindSpeed;
	static float Pressure;
	static float Dewptf;
	
	@Override
	public void run() {
		
		try {
			GetWeather();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			SendWeather();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public static void GetWeather() throws IOException
	{
		// Function connects to the Arduino on the local network, gets the HTML, and splits it up populating variables
		
		URL url = new URL("http://192.168.1.23");
		// Open connection to the Arduino
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		con.setConnectTimeout(5000);
		con.setReadTimeout(5000);
		
		int status = con.getResponseCode();
		
		if (status == 200)
		{
			System.out.println("Sensor data retrieved...");
		}
		else
		{
			System.out.println("Cannot connect to sensor network!");
		}
		
		// Buffer this hacky shit
		// Pull the entire input into a string
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer content = new StringBuffer();
		while ((inputLine = in.readLine()) != null) {
			content.append(inputLine);
		}
		// Disconnect from server
		in.close();	
		con.disconnect();
		
		// Split the input up based on HTML breaks
		String ContentString = content.toString();
		String[] Formatted = ContentString.split("<br />");
		
		// Fill in the variables
		Temp = Float.parseFloat(Formatted[0]);
		Humidity = Float.parseFloat(Formatted[1]);
		WindSpeed = Float.parseFloat(Formatted[2]);
		WindGust = Float.parseFloat(Formatted[4]);
		WindDir = Integer.parseInt(Formatted[3]);
		PrecipHour = Float.parseFloat(Formatted[5]);
		PrecipTotal = Float.parseFloat(Formatted[6]);
		Pressure = Float.parseFloat(Formatted[7]);
		Dewptf = Float.parseFloat(Formatted[8]);
		
		// Convert pressure from pa to inches
		Pressure = (float) (Pressure * 0.00029529983071445);
	}
	
	public static void SendWeather() throws IOException
	{
		// Function connects to the Weather Underground upload URL and formats a GET request to submit
		
		// Connect to the URL
		URL url = new URL("http://weatherstation.wunderground.com/weatherstation/updateweatherstation.php");
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		con.setConnectTimeout(5000);
		con.setReadTimeout(5000);

		// Use the helper function to build the URL with parameters
		Map<String, String> parameters = new HashMap<>();
		parameters.put("action", "updateraw");						// Tells WUnderground we're sending a raw update in the URL
		parameters.put("ID", StationID);							// Must send what station we're updating
		parameters.put("PASSWORD", StationKey);						// Must supply station key
		parameters.put("dateutc", "now");							// Tell that the update is whenever it arrives
		parameters.put("winddir", String.valueOf(WindDir));
		parameters.put("windspeedmph", String.valueOf(WindSpeed));
		parameters.put("windgustmph", String.valueOf(WindGust));
		parameters.put("humidity", String.valueOf(Humidity));
		parameters.put("tempf", String.valueOf(Temp));
		parameters.put("rainin", String.valueOf(PrecipHour));
		parameters.put("dailyrainin", String.valueOf(PrecipTotal));		
		parameters.put("baromin", String.valueOf(Pressure));
		parameters.put("dewptf", String.valueOf(Dewptf));
		 
		// Must set this flag to send anything
		con.setDoOutput(true);
		DataOutputStream out = new DataOutputStream(con.getOutputStream());
		out.writeBytes(ParameterStringBuilder.getParamsString(parameters));
		out.flush();
		out.close();
		
		// Success, print to the console
		if (con.getResponseCode() == 200)
		{
			System.out.print("Uploaded date to WU at ");
			System.out.println( java.util.Calendar.getInstance().getTime() );
			System.out.println( Temp + "° / " 
			+ Pressure + "in / " 
			+ Humidity + "% / " 
			+ WindSpeed + "mph / " 
			+ WindDir + "° / " 
			+ WindGust + "mphg / " 
			+ PrecipHour + "inh / " 
			+ PrecipTotal + " inday / " 
			+ Dewptf + " Dew");
		}
		
		con.disconnect();
	}

}
