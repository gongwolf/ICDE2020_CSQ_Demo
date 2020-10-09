package cs.nmsu.edu.demo.utilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class constants {
	public static final int path_dimension = 4; // 1(edu_dis)+3(road net work attrs);
	
	public static String home_folder = System.getProperty("user.home");
//    public static final String data_path = home_folder + "/shared_git/ICDE_ConstrainSkylineQuery/Data";
//    public static final String data_path = home_folder + "/shared_git/ICDE_ConstrainSkylineQuery/Data";
//    public static final String db_path = data_path + "/Neo4jDB_files";
//    public static final String index_path = data_path + "/index";
    
    public static final String data_path = home_folder + "/eclipse-workspace/ICDE2020CSQDemo/Data";
    public static final String db_path = data_path + "/Neo4jDB_files";
    public static final String index_path = data_path + "/index";


	
	public static HashSet<String> cityList = new HashSet<>(Arrays.asList("SF", "LA", "NY"));
	public static HashSet<String> typeList = new HashSet<>(Arrays.asList("food", "lodging", "restaurant"));

	public static void print(double[] costs) {
		System.out.print("[");
		for (double c : costs) {
			System.out.print(c + " ");
		}
		System.out.println("]");
	}

	public static double distanceInMeters(double lat1, double long1, double lat2, double long2) {
		long R = 6371000;
		double d;
		
		//method 4
	    double dLat = lat2 * Math.PI / 180 - lat1 * Math.PI / 180;
	    double dLon = long2 * Math.PI / 180 - long1 * Math.PI / 180;
	    double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
	    Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
	    Math.sin(dLon/2) * Math.sin(dLon/2);
	    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
	    d = R * c;
	    return d;
	}

}
