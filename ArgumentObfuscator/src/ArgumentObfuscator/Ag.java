package ArgumentObfuscator;
import java.util.List;

import android.content.Intent;
import com.google.android.gms.location.Geofence;

public class Ag {
	private int i[];
	private String s[];
	private double d[];
	private Intent intent[];
	private List<Geofence> ls;
	
	Ag(String str1, String str2){
		s = new String[2];
		s[0] = str1;
		s[1] = str2;
	}
	
	Ag(int int1){
		i = new int[1];
		i[0] = int1;
	}
	
	Ag(int int1, List<Geofence> lsg1){
		i = new int[1];
		i[0] = int1;
		ls = lsg1;
	}
	
	Ag(Intent intent1){
		intent = new Intent[1];
		intent[0] = intent1;
	}
	
	Ag(Intent intent1, int int1, int int2){
		i = new int[2];
		intent = new Intent[1];
		i[0] = int1;
		i[1] = int2;
		intent[0] = intent1;
	}
	
	Ag(double d1, double d2, double d3, double d4){
		d = new double[4];
		d[0] = d1;
		d[1] = d2;
		d[2] = d3;
		d[3] = d4;
	}
	
	int getArg(int x, int y){
		return i[y];
	}
	
	String getArg(String x, int y){
		return s[y];
	}
	
	double getArg(double x, int y){
		return d[y];
	}
	
	Intent getArg(Intent x, int y){
		return intent[y];
	}
	
	List<Geofence> getArg(List<Geofence> x, int y){
		return ls;
	}
}
