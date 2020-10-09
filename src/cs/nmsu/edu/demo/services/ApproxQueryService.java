package cs.nmsu.edu.demo.services;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.neo4j.graphdb.Transaction;

import cs.nmsu.edu.demo.RstarTree.Data;
import cs.nmsu.edu.demo.methods.Result;
import cs.nmsu.edu.demo.utilities.constants;
import cs.nmsu.edu.demo.methods.ApproxMixedIndex;
import cs.nmsu.edu.demo.methods.ApproxRangeIndex;
import cs.nmsu.edu.demo.neo4jTools.connector;
import cs.nmsu.edu.demo.utilities.POIObject;
import cs.nmsu.edu.demo.utilities.QueryParameters;

@Path("/query")
public class ApproxQueryService {

	String homepath = System.getProperty("user.home");
	int t_distance = 40;

	@Path("/approxRangeIndexedById/{city}/{id}/{threshold}/{type:.*}/{num_bus_stop:.*}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response ApproxRangeIndexedQueryByIdQueryParameters(@PathParam("city") String city,
			@PathParam("id") int queryPlaceId, @PathParam("threshold") double distance_threshold,
			@PathParam("type") String type, @PathParam("num_bus_stop") int num_bus_stop) {

		System.out.println("\nCall the function approxRangeIndexedById ");

		QueryParameters qp = new QueryParameters();
		qp.setCity(city);
		qp.setNum_bus_stop(num_bus_stop);
		qp.setType(type);

		System.out.println(qp + " distance_threshold:" + distance_threshold);

		if (!constants.cityList.contains(qp.getCity())) {
			return null;
		}

		if ((type != null && !type.equals("")) && !constants.typeList.contains(type)) {
			return null;
		}

		ApproxRangeIndex approx_range_index = new ApproxRangeIndex(city, distance_threshold, qp);
		Data queryD = approx_range_index.getDataById(queryPlaceId);
		System.out.println(queryD);
		approx_range_index.baseline(queryD);

		ArrayList<ResultBean> result = new ArrayList<>();
		for (Result r : approx_range_index.skyPaths) {
			ResultBean rbean = new ResultBean(r);
			result.add(rbean);
		}

		updateBeansNodeLocationInformation(result, city, queryD, type);
		return Response.status(200).entity(result).header("Access-Control-Allow-Origin", "*").build();

	}

	@Path("/approxRangeIndexedByLocation/{city}/{lat}/{lng}/{threshold}/{type:.*}/{num_bus_stop:.*}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response ApproxRangeIndexedQueryByLocationQueryParameter(@PathParam("city") String city,
			@PathParam("lat") double lat, @PathParam("lng") double lng,
			@PathParam("threshold") double distance_threshold, @PathParam("type") String type,
			@PathParam("num_bus_stop") int num_bus_stop) {

		System.out.println("\nCall the function approxRangeIndexedByLocation ");

		if (!constants.cityList.contains(city)) {
			return null;
		}

		if ((type != null && !type.equals("")) && !constants.typeList.contains(type)) {
			return null;
		}

		int isAIOP = isAInterestingOfPoint(city, lat, lng, type);
		System.out.println("found   " + isAIOP + "  " + (isAIOP != -1));

		if (isAIOP != -1) {
			int id = isAIOP;
			return ApproxRangeIndexedQueryByIdQueryParameters(city, id, distance_threshold, type, num_bus_stop);
		} else {
			QueryParameters qp = new QueryParameters();
			qp.setCity(city);
			qp.setNum_bus_stop(num_bus_stop);
			qp.setType(type);
			System.out.println(qp + " distance_threshold:" + distance_threshold);

			ApproxRangeIndex approx_range_index = new ApproxRangeIndex(city, distance_threshold, qp);
			approx_range_index.baseline(lat, lng);
			ArrayList<ResultBean> result = new ArrayList<>();
			for (Result r : approx_range_index.skyPaths) {
				ResultBean rbean = new ResultBean(r);
				result.add(rbean);
			}
			Data queryData = new Data(3);
			queryData.setData(new float[] { -1, -1, -1 });
			updateBeansNodeLocationInformation(result, city, queryData, type);
			return Response.status(200).entity(result).header("Access-Control-Allow-Origin", "*").build();
		}
	}

	@Path("/approxMixedIndexedById/{city}/{id}/{threshold}/{type:.*}/{num_bus_stop:.*}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response ApproxMixedIndexedQueryByIdQueryParameters(@PathParam("city") String city,
			@PathParam("id") int queryPlaceId, @PathParam("threshold") double distance_threshold,
			@PathParam("type") String type, @PathParam("num_bus_stop") int num_bus_stop) {

		System.out.println("\nCall the function approxMixedIndexedById ");

		QueryParameters qp = new QueryParameters();
		qp.setCity(city);
		qp.setNum_bus_stop(num_bus_stop);
		qp.setType(type);

		System.out.println(qp + " distance_threshold:" + distance_threshold);

		if (!constants.cityList.contains(qp.getCity())) {
			return null;
		}

		if ((type != null && !type.equals("")) && !constants.typeList.contains(type)) {
			return null;
		}

		ApproxMixedIndex approx_mixed_index = new ApproxMixedIndex(city, distance_threshold, qp);
		Data queryD = approx_mixed_index.getDataById(queryPlaceId);
		System.out.println(queryD);
		approx_mixed_index.baseline(queryD);

		ArrayList<ResultBean> result = new ArrayList<>();
		for (Result r : approx_mixed_index.skyPaths) {
			ResultBean rbean = new ResultBean(r);
			result.add(rbean);
		}

		updateBeansNodeLocationInformation(result, city, queryD, type);
		return Response.status(200).entity(result).header("Access-Control-Allow-Origin", "*").build();

	}

	@Path("/approxMixedIndexedByLocation/{city}/{lat}/{lng}/{threshold}/{type:.*}/{num_bus_stop:.*}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response ApproxMixedIndexedQueryByLocationQueryParameter(@PathParam("city") String city,
			@PathParam("lat") double lat, @PathParam("lng") double lng,
			@PathParam("threshold") double distance_threshold, @PathParam("type") String type,
			@PathParam("num_bus_stop") int num_bus_stop) {

		System.out.println("\ncall the function approxMixedIndexedByLocation");

		if (!constants.cityList.contains(city)) {
			return null;
		}

		if ((type != null && !type.equals("")) && !constants.typeList.contains(type)) {
			return null;
		}

		int isAIOP = isAInterestingOfPoint(city, lat, lng, type);
		if (isAIOP != -1) {
			int id = isAIOP;
			return ApproxMixedIndexedQueryByIdQueryParameters(city, id, distance_threshold, type, num_bus_stop);
		} else {
			QueryParameters qp = new QueryParameters();
			qp.setCity(city);
			qp.setNum_bus_stop(num_bus_stop);
			qp.setType(type);
//			System.out.println("!~~~" + qp + " distance_threshold:" + distance_threshold);

			ApproxMixedIndex approx_mixed_index = new ApproxMixedIndex(city, distance_threshold, qp);
			approx_mixed_index.baseline(lat, lng);
			ArrayList<ResultBean> result = new ArrayList<>();
			for (Result r : approx_mixed_index.skyPaths) {
				ResultBean rbean = new ResultBean(r);
				result.add(rbean);
			}

			Data queryData = new Data(3);
			queryData.setData(new float[] { -1, -1, -1 });

			updateBeansNodeLocationInformation(result, city, queryData, type);
			return Response.status(200).entity(result).header("Access-Control-Allow-Origin", "*").build();
		}
	}

	private void updateBeansNodeLocationInformation(ArrayList<ResultBean> result, String city, Data queryData,
			String type) {
		String home_folder = System.getProperty("user.home");
		String graphPath = constants.db_path+"/testdb_" + city + "_Gaussian/databases/graph.db";
		connector n = new connector(graphPath);
		n.startDB();
		try (Transaction tx = n.graphDB.beginTx()) {
			for (ResultBean rbean : result) {
				for (NodeBeans nbean : rbean.nodeIDs) {
					double[] locations = new double[2];
					locations[0] = (double) n.graphDB.getNodeById(nbean.getId()).getProperty("lat");
					locations[1] = (double) n.graphDB.getNodeById(nbean.getId()).getProperty("log");
					nbean.setLat(locations[0]);
					nbean.setLng(locations[1]);
				}
				rbean.end_name = getLocationNameByID(rbean.end, city, type);
				double[] querycosts = queryData.getData();
//				System.out.println(rbean.end_name);
//				System.arraycopy(querycosts, 0, rbean.querycosts, 4, querycosts.length);
				rbean.querycosts[4] = 5 - querycosts[0];
				rbean.querycosts[5] = querycosts[1];
				rbean.querycosts[6] = 10 - querycosts[2];
			}
			tx.success();
		}

		n.shutdownDB();
	}

	private String getLocationNameByID(long id, String city, String type) {
		String name = null;
		POIObject iobj = new POIObject();
		String home_folder = System.getProperty("user.home");
		String dataPath = "";
		if (type.equals("all") || type == null || type.equals("")) {
			dataPath = constants.data_path+"/staticNode_real_" + city + ".txt";
		} else {
			dataPath = constants.data_path+"/staticNode_real_" + city + "_" + type + ".txt";
		}

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

					iobj.setPlaceID((int) id);
					iobj.setLocations(new double[] { lat, log });
					iobj.setData(new float[] { c1, c2, c3 });

					iobj.setG_p_id(infos[6]);
					iobj.setG_p_name(infos[7]);

					name = iobj.g_p_name;

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

		return name;
	}

	// check whether the given location is a IOP in the data set.
	private int isAInterestingOfPoint(String city, double lat, double lng, String type) {
		String datapath;
		int isIOP = -1;
		double min_distance = Double.MAX_VALUE;
		if (!type.equals("") && type != null) {
			datapath = constants.data_path+"/staticNode_real_" + city + "_" + type + ".txt";
		} else {
			datapath = constants.data_path+"/staticNode_real_" + city + ".txt";
		}

		System.out.println("find the location from the file " + datapath);
		double[] targetLocations = new double[2];

		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(datapath));
			String line = reader.readLine();
			while (line != null) {
				double lat2 = Double.parseDouble(line.split(",")[1]);
				double lng2 = Double.parseDouble(line.split(",")[2]);
				double distance = constants.distanceInMeters(lat, lng, lat2, lng2);
				if (distance < min_distance) {
					min_distance = distance;
					if (distance < t_distance) {
						isIOP = Integer.parseInt(line.split(",")[0]);
						targetLocations[0] = lat2;
						targetLocations[1] = lng2;
					}
				}
				line = reader.readLine();
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println(isIOP + " -->> distance to query location is " + (long) min_distance + "m   " + lat + " "
				+ lng + "     " + targetLocations[0] + " " + targetLocations[1]);
		return isIOP;
	}

	public static void main(String args[]) {
		String city = "SF";
		double lat = 37.794597;
		double lng = -122.395181;
		long distance_threshold = 500;
		String type = "lodging";
		int num_bus_stop = 10;
		ApproxQueryService aqs = new ApproxQueryService();
		aqs.ApproxMixedIndexedQueryByLocationQueryParameter(city, lat, lng, distance_threshold, type, num_bus_stop);
//		aqs.ApproxRangeIndexedQueryByIdQueryParameters(city, 1233, distance_threshold, type, num_bus_stop);
//		aqs.ApproxMixedIndexedQueryByIdQueryParameters(city, 123, distance_threshold, type, num_bus_stop);
	}

}
