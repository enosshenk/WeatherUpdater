import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


public class WeatherReader {

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
	
	static Timer ReportTimer = new Timer("Time");
	static ReporterTask Task = new ReporterTask();
	
	public static void main(String[] args) {
		ReportTimer.schedule(Task, (long)0.0, (long)300000.0);

	}
}


