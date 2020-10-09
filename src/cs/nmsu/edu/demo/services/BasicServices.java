package cs.nmsu.edu.demo.services;


import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONException;

import cs.nmsu.edu.demo.utilities.*;

@Path("/basic")
public class BasicServices {
	@Path("/getIOPInformationById/{city}/{id}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getIOPinformationById(@PathParam("id") int id, @PathParam("city") String city)
			throws JSONException {
		if(!constants.cityList.contains(city)) {
			return null;
		}
		
		POIObject iobj = new POIObject();
		String dataPath = constants.data_path + "/mydata/DemoProject/data/staticNode_real_" + city + ".txt";

		BufferedReader br = null;
		int linenumber = 0;

		try {
			br = new BufferedReader(new FileReader(dataPath));
			String line = null;
			while ((line = br.readLine()) != null) {
				if (linenumber == id) {
//                    System.out.println(line);
					String[] infos = line.split(",");
					Double lat = Double.parseDouble(infos[1]);
					Double log = Double.parseDouble(infos[2]);

					Float c1 = Float.parseFloat(infos[3]);
					Float c2 = Float.parseFloat(infos[4]);
					Float c3 = Float.parseFloat(infos[5]);

					iobj.setPlaceID(id);
					iobj.setLocations(new double[] { lat, log });
					iobj.setData(new float[] { c1, c2, c3 });

					iobj.setG_p_id(infos[6]);
					iobj.setG_p_name(infos[7]);

					break;
				} else {
					linenumber++;
				}
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Can not open the file, please check it. ");
		}
		return Response.status(200).entity(iobj).header("Access-Control-Allow-Origin", "*").build();
	}

	@Path("/getAllIOPs/{city}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllIOPsByCity(@PathParam("city") String city) throws JSONException {
		
		if(!constants.cityList.contains(city)) {
			return null;
		}
		
		String home_folder = System.getProperty("user.home");
		String dataPath = home_folder + "/mydata/DemoProject/data/staticNode_real_" + city + ".txt";

		ArrayList<POIObject > results = new ArrayList<POIObject>();

		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(dataPath));
			String line = null;
			while ((line = br.readLine()) != null) {
				POIObject iobj = new POIObject();
				String[] infos = line.split(",");

				int id = Integer.parseInt(infos[0]);

				Double lat = Double.parseDouble(infos[1]);
				Double log = Double.parseDouble(infos[2]);

				Float c1 = Float.parseFloat(infos[3]);
				Float c2 = Float.parseFloat(infos[4]);
				Float c3 = Float.parseFloat(infos[5]);

				iobj.setPlaceID(id);
				iobj.setLocations(new double[] { lat, log });
				iobj.setData(new float[] { c1, c2, c3 });

				iobj.setG_p_id(infos[6]);
				iobj.setG_p_name(infos[7]);
				
				results.add(iobj);
			}

			br.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Can not open the file, please check it. ");
		}

		return Response.status(200).entity(results).header("Access-Control-Allow-Origin", "*").build();

	}

}
