package cs.nmsu.edu.demo.methods;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterable;
import org.neo4j.graphdb.Transaction;

import cs.nmsu.edu.demo.RstarTree.Data;
import cs.nmsu.edu.demo.neo4jTools.connector;
import cs.nmsu.edu.demo.methods.Index;
import cs.nmsu.edu.demo.utilities.QueryParameters;
import cs.nmsu.edu.demo.utilities.constants;

import java.io.*;
import java.util.*;

public class ExactMethodIndex {
	public double nn_dist; // the distance from the query hotel to the bus stop.
	public ArrayList<path> qqqq = new ArrayList<>();
	public ArrayList<Result> skyPaths = new ArrayList<>();
	public GraphDatabaseService graphdb;
	Random r = new Random(System.nanoTime());
	String treePath;
	String dataPath;
	int graph_size;
	String degree;
	String graphPath;
	long add_oper = 0;
	long check_add_oper = 0;
	long map_operation = 0;
	long checkEmpty = 0;
	long read_data = 0;
	// Todo: each hotel know the distance to the hotel than dominate it.
	HashMap<Integer, Double> dominated_checking = new HashMap<>(); //
	String home_folder = System.getProperty("user.home");
	private int hotels_num;
	private double range;
	private HashMap<Long, myNode> tmpStoreNodes = new HashMap();
	private ArrayList<Data> sNodes = new ArrayList<>();
	private ArrayList<Data> sky_hotel;
	private HashSet<Integer> finalDatas = new HashSet<>();
	private int checkedDataId = 9;
	private long add_counter; // how many times call the addtoResult function
	private long pro_add_result_counter; // how many path + hotel combination of the results are generated
	private long sky_add_result_counter; // how many results are taken the addtoskyline operation
	private Data queryD;
	private long d_list_num = 0;
	private String city = "";
	private QueryParameters qp;

	public ExactMethodIndex(String city, QueryParameters qp) {
		this.city = city;
		r = new Random(System.nanoTime());
//		this.graphPath = home_folder + "/neo4j334/testdb_" + city + "_Random/databases/graph.db";
		this.graphPath = constants.db_path + "/testdb_" + city + "_Gaussian/databases/graph.db";

		if (qp.getType().equals("") || qp.getType() == null) {
			this.treePath = constants.data_path + "/real_tree_" + city + ".rtr";
			this.dataPath = constants.data_path + "/staticNode_real_" + city + ".txt";
			this.hotels_num = getNumberOfHotels();
			System.out.println("There are " + this.hotels_num + " POI in the city " + city);
		} else {
			this.treePath = constants.data_path + "/real_tree_" + city + "_" + qp.getType() + ".rtr";
			this.dataPath = constants.data_path + "/staticNode_real_" + city + "_" + qp.getType() + ".txt";
			this.hotels_num = getNumberOfHotels();
			System.out.println("There are " + this.hotels_num + " POI in the city " + city + " are " + qp.getType());
		}

		this.qp = qp;

	}

	public ArrayList<Result> baselineIndex(Data queryD) {
		this.queryD = queryD;
		this.queryD = queryD;
		StringBuffer sb = new StringBuffer();
		sb.append(queryD.getPlaceId() + "|");

		Skyline sky = new Skyline(treePath);

		// find the skyline hotels of the whole dataset.
		sky.findSkyline();
		this.sky_hotel = new ArrayList<>(sky.sky_hotels); // must be in the result set

		long s_sum = System.currentTimeMillis();
		long index_s = 0;
		int sk_counter = 0; // the number of total candidate hotels of each bus station

		long r1 = System.currentTimeMillis();
		sky.BBS(queryD); // Find the hotels that aren't dominated by the query point
		long bbs_rt = System.currentTimeMillis() - r1;
		sNodes = sky.skylineStaticNodes;
		sb.append(this.sNodes.size() + " " + this.sky_hotel.size() + "|");

		// Lemma 1: Assumes the POIs that can be reached from the query point, included
		// the skyline POIs
		for (Data d : sNodes) {
			double[] c = new double[constants.path_dimension + 3];
			c[0] = d.distance_q;
			double[] d_attrs = d.getData();
			for (int i = 4; i < c.length; i++) {
				c[i] = d_attrs[i - 4];
			}
			Result r = new Result(queryD, d, c, null);
			addToSkyline(r);
		}

		// find the minimum distance from query point to the skyline hotel that dominate
		// non-skyline hotel cand_d
		for (Data cand_d : sNodes) {
			double h_to_h_dist = Double.MAX_VALUE;
			if (!sky_hotel.contains(cand_d)) {
				for (Data s_h : sky_hotel) {
					if (checkDominated(s_h.getData(), cand_d.getData())) {
						double tmep_dist = s_h.distance_q;
						if (tmep_dist < h_to_h_dist) {
							h_to_h_dist = tmep_dist;
						}
					}
				}
			}
			dominated_checking.put(cand_d.getPlaceId(), h_to_h_dist);
		}

//        System.out.println("==========" + this.skyPaths.size());

		long db_time = System.currentTimeMillis();
		connector.graphDB = null;
		connector n = new connector(graphPath);
		n.startDB();
		this.graphdb = n.getDBObject();

		long counter = 0; // queue operation counter
		long addResult_rt = 0;
		long expasion_rt = 0;
		long iteration_rt = System.nanoTime();

		try (Transaction tx = this.graphdb.beginTx()) {
			db_time = System.currentTimeMillis() - db_time;
			r1 = System.currentTimeMillis();

			long nn_rt = System.currentTimeMillis() - r1;

			long rt = System.currentTimeMillis();
			Node startNode = nearestNetworkNode(queryD);
			long numberofNodes = n.getNumberofNodes();

			while (startNode != null) {

				myNode s = new myNode(queryD, startNode.getId(), -1, n);

				myNodePriorityQueue mqueue = new myNodePriorityQueue();
				mqueue.add(s);

				this.tmpStoreNodes.put(s.id, s);

				while (!mqueue.isEmpty()) {

					myNode v = mqueue.pop();
					v.inqueue = false;

					counter++;

					for (int i = 0; i < v.skyPaths.size(); i++) {
						path p = v.skyPaths.get(i);
						if (!p.expaned) {
							p.expaned = true;

							long ee = System.nanoTime();
							ArrayList<path> new_paths = p.expand(n);
							expasion_rt += (System.nanoTime() - ee);
							for (path np : new_paths) {

								boolean num_bus_stop_query_flag = false;

								if (qp.num_bus_stop != -1) {
									if (np.rels.size() <= qp.num_bus_stop) {
										num_bus_stop_query_flag = true;
									}
								} else {
									num_bus_stop_query_flag = true;
								}

								if (num_bus_stop_query_flag) {
									if (!np.hasCycle()) {
										myNode next_n;
										if (this.tmpStoreNodes.containsKey(np.endNode)) {
											next_n = tmpStoreNodes.get(np.endNode);
										} else {
											next_n = new myNode(queryD, np.endNode, -1, n);
											this.tmpStoreNodes.put(next_n.id, next_n);
										}

										// Lemma 2
										if (!(this.tmpStoreNodes.get(np.startNode).distance_q > next_n.distance_q)) {
											if (next_n.addToSkyline(np) && !next_n.inqueue) {
												mqueue.add(next_n);
												next_n.inqueue = true;
											}
										}
									}
								}
							}
						}
					}
				}

				startNode = nearestNetworkNode(queryD);
			}

			long exploration_rt = System.currentTimeMillis() - rt; // time that is used to search on the graph
//            System.out.println("expansion finished " + exploration_rt);
			long tt_sl = 0;
//            hotels_scope = new HashMap<>();
			Index idx = new Index(qp.city, -1, qp.type);

			for (Map.Entry<Long, myNode> entry : tmpStoreNodes.entrySet()) {
				sk_counter += entry.getValue().skyPaths.size();
				myNode my_n = entry.getValue();

				long t_index_s = System.nanoTime();
				ArrayList<Data> d_list;
				d_list = idx.read_d_list_from_disk(my_n.id);

				index_s += (System.nanoTime() - t_index_s);
				for (path p : my_n.skyPaths) {
					if (!p.rels.isEmpty()) {
						long ats = System.nanoTime();
						boolean f = addToSkylineResult(p, d_list);
						addResult_rt += System.nanoTime() - ats;
					}
				}

			}

			// time that is used to find the candicated objects, find the nearest objects,
			sb.append(" running time(ms):" + bbs_rt + "," + nn_rt + "," + exploration_rt + "," + (index_s / 1000000)
					+ ",");
			tx.success();
		}

		long shut_db_time = System.currentTimeMillis();
		n.shutdownDB();
		shut_db_time = System.currentTimeMillis() - shut_db_time;

		s_sum = System.currentTimeMillis() - s_sum;
		sb.append((s_sum - db_time - shut_db_time - (index_s / 1000000)) + "| overall:" + (s_sum) + "   ");
		sb.append(addResult_rt / 1000000 + "(" + (this.add_oper / 1000000) + "+" + (this.check_add_oper / 1000000) + "+"
				+ (this.map_operation / 1000000) + "+" + (this.checkEmpty / 1000000) + "+" + (this.read_data / 1000000)
				+ "),");
		sb.append(expasion_rt / 1000000 + "|");
		sb.append("result size:" + this.skyPaths.size());

//        sb.append("\nadd_to_Skyline_result " + this.add_counter + "  " + this.pro_add_result_counter + "  " + this.sky_add_result_counter + " ");
//        sb.append((double) this.sky_add_result_counter / this.pro_add_result_counter);

		List<Result> sortedList = new ArrayList(this.skyPaths);
		Collections.sort(sortedList);

		HashSet<Long> final_bus_stops = new HashSet<>();

		for (Result r : sortedList) {
			this.finalDatas.add(r.end.getPlaceId());
			if (r.p != null) {
				for (Long nn : r.p.nodes) {
					final_bus_stops.add(nn);
				}
			}
		}

		sb.append(" " + finalDatas.size() + "|");

		int visited_bus_stop = this.tmpStoreNodes.size();
		int bus_stop_in_result = final_bus_stops.size();

		sb.append(visited_bus_stop + "," + bus_stop_in_result + "," + (double) bus_stop_in_result / visited_bus_stop
				+ "|" + this.sky_add_result_counter);

		sb.append("," + add_counter + "," + sk_counter + "," + counter);

		System.out.println(sb.toString());
		ArrayList<Result> fianl_result_set = new ArrayList<Result>(this.skyPaths);
		return fianl_result_set;

	}

	public ArrayList<Result> baselineWihtIndex(double lat, double lng) {
		System.out.println("call baselineWihtIndex function");
		StringBuffer sb = new StringBuffer();
		sb.append("[" + lat + "," + lng + "]" + " ");

		Skyline sky = new Skyline(treePath);

		// find the skyline hotels of the whole dataset.
		sky.findSkyline(lat, lng);
		this.sky_hotel = new ArrayList<>(sky.sky_hotels);

		long s_sum = System.currentTimeMillis();
		long index_s = 0;
		int sk_counter = 0; // the number of total candidate hotels of each bus station

		long r1 = System.currentTimeMillis();
//		 Find the hotels that aren't dominated by the query point
		sky.allDatas(lat, lng);
		long bbs_rt = System.currentTimeMillis() - r1;
		sNodes = sky.allNodes;

		for (Data d : sNodes) {
			double[] c = new double[constants.path_dimension + 3];
			c[0] = d.distance_q;
			double[] d_attrs = d.getData();
			for (int i = 4; i < c.length; i++) {
				c[i] = d_attrs[i - 4];
			}
			Result r = new Result(lat, lng, d, c, null);
			addToSkyline(r);
		}
		sb.append(this.sNodes.size() + " " + this.sky_hotel.size() + " #ofSkyline:" + this.skyPaths.size() + " ");
		// find the minimum distance from query point to the skyline hotel that dominate
		// non-skyline hotel cand_d
		for (Data cand_d : sNodes) {
			double h_to_h_dist = Double.MAX_VALUE;
			if (!sky_hotel.contains(cand_d)) {
				for (Data s_h : sky_hotel) {
					if (checkDominated(s_h.getData(), cand_d.getData())) {
						double tmep_dist = s_h.distance_q;
						if (tmep_dist < h_to_h_dist) {
							h_to_h_dist = tmep_dist;
						}
					}
				}
			}
			dominated_checking.put(cand_d.getPlaceId(), h_to_h_dist);
		}

		long db_time = System.currentTimeMillis();
		connector n = new connector(graphPath);
		n.startDB();
		this.graphdb = n.getDBObject();

		long counter = 0;
		long addResult_rt = 0;
		long expasion_rt = 0;

		long iteration_rt = System.nanoTime();

		try (Transaction tx = this.graphdb.beginTx()) {
			db_time = System.currentTimeMillis() - db_time;
			r1 = System.currentTimeMillis();

			long nn_rt = System.currentTimeMillis() - r1;

			long rt = System.currentTimeMillis();
			Node startNode = nearestNetworkNode(lat, lng);
			long numberofNodes = n.getNumberofNodes();

			while (startNode != null) {
				long last_iter_rt = System.nanoTime() - iteration_rt;
				iteration_rt = System.nanoTime();
//				System.out.println(startNode.getId()+"   ----->   "+ this.tmpStoreNodes.size() + "/"+numberofNodes+"   Last Iteration Runing time:"+(last_iter_rt/1000000)+"ms");
				myNode s = new myNode(lat, lng, startNode.getId(), -1, n);

				myNodePriorityQueue mqueue = new myNodePriorityQueue();
				mqueue.add(s);
				this.tmpStoreNodes.put(s.id, s);

				while (!mqueue.isEmpty()) {
					myNode v = mqueue.pop();
					v.inqueue = false;
					counter++;

					for (int i = 0; i < v.skyPaths.size(); i++) {
						path p = v.skyPaths.get(i);
						if (!p.expaned) {
							p.expaned = true;
							long ee = System.nanoTime();
							ArrayList<path> new_paths = p.expand(n);
							expasion_rt += (System.nanoTime() - ee);
							for (path np : new_paths) {

								boolean num_bus_stop_query_flag = false;

								if (qp.num_bus_stop != -1) {
									if (np.rels.size() <= qp.num_bus_stop) {
										num_bus_stop_query_flag = true;
									}
								} else {
									num_bus_stop_query_flag = true;
								}

								if (num_bus_stop_query_flag) {
									if (!np.hasCycle()) {
										myNode next_n;
										if (this.tmpStoreNodes.containsKey(np.endNode)) {
											next_n = tmpStoreNodes.get(np.endNode);
										} else {
											next_n = new myNode(lat, lng, np.endNode, -1, n);
											this.tmpStoreNodes.put(next_n.id, next_n);
										}

										// lemma 2
										if (!(this.tmpStoreNodes.get(np.startNode).distance_q > next_n.distance_q)) {
											if (next_n.addToSkyline(np) && !next_n.inqueue) {
												mqueue.add(next_n);
												next_n.inqueue = true;
											}
										}
									}
								}
							}
						}
					}
				}

				startNode = nearestNetworkNode(lat, lng);
			}

			System.out.println("---------------Graph Traversal are finished----------------");

			long exploration_rt = System.currentTimeMillis() - rt;
//            System.out.println("expansion finished " + exploration_rt);

			long graph_process_rt = System.currentTimeMillis() - r1;

			long tt_sl = 0;

//            hotels_scope = new HashMap<>();
			int addtocounter = 0;
			Index idx = new Index(qp.city, -1, qp.type);
			for (Map.Entry<Long, myNode> entry : tmpStoreNodes.entrySet()) {
				long one_iter_rt = System.currentTimeMillis();
				addtocounter++;
				sk_counter += entry.getValue().skyPaths.size();
				myNode my_n = entry.getValue();

				long t_index_s = System.nanoTime();

				index_s += (System.nanoTime() - t_index_s);
				ArrayList<Data> d_list = idx.read_d_list_from_disk(my_n.id);
				if (d_list != null) {
					for (path p : my_n.skyPaths) {
						if (!p.rels.isEmpty()) {
							long ats = System.nanoTime();
							boolean f = addToSkylineResultByLocation(lat, lng, p, d_list);
							addResult_rt += System.nanoTime() - ats;
						}
					}
				}
				one_iter_rt = System.currentTimeMillis() - one_iter_rt;
//				System.out.println("size of skyline of Node "+ entry.getKey()+" is "+ entry.getValue().skyPaths.size()+" used "+ one_iter_rt+ "ms #### "+d_list.size()+" ######"+addtocounter+"............................................");
				one_iter_rt = System.currentTimeMillis();
			}

			// time that is used to find the candidate objects, find the nearest objects,
			sb.append(" running time(ms):" + bbs_rt + "," + nn_rt + "," + exploration_rt + "," + (index_s / 1000000)
					+ ",");
			tx.success();
		}

		long shut_db_time = System.currentTimeMillis();
		n.shutdownDB();
		shut_db_time = System.currentTimeMillis() - shut_db_time;

		s_sum = System.currentTimeMillis() - s_sum;
		sb.append((s_sum - db_time - shut_db_time - (index_s / 1000000)) + "| overall:" + (s_sum) + "   ");
		sb.append(addResult_rt / 1000000 + "(" + (this.add_oper / 1000000) + "+" + (this.check_add_oper / 1000000) + "+"
				+ (this.map_operation / 1000000) + "+" + (this.checkEmpty / 1000000) + "+" + (this.read_data / 1000000)
				+ "),");
		sb.append(expasion_rt / 1000000 + "|");
		sb.append("result size:" + this.skyPaths.size());

		List<Result> sortedList = new ArrayList(this.skyPaths);
		Collections.sort(sortedList);

		HashSet<Long> final_bus_stops = new HashSet<>();

		for (Result r : sortedList) {
			this.finalDatas.add(r.end.getPlaceId());
			if (r.p != null) {
				for (Long nn : r.p.nodes) {
					final_bus_stops.add(nn);
				}
			}
		}

		sb.append(" " + finalDatas.size() + "|");

		int visited_bus_stop = this.tmpStoreNodes.size();
		int bus_stop_in_result = final_bus_stops.size();

		sb.append(visited_bus_stop + "," + bus_stop_in_result + "," + (double) bus_stop_in_result / visited_bus_stop
				+ "|" + this.sky_add_result_counter);

		sb.append("," + add_counter + "," + sk_counter + "," + counter);

		System.out.println(sb.toString());
		ArrayList<Result> fianl_result_set = new ArrayList<Result>(this.skyPaths);
		return fianl_result_set;

	}

	private boolean addToSkylineResult(path np, ArrayList<Data> d_list) {

//    private boolean addToSkylineResult(path np, Data d) {
		this.add_counter++;
		long r2a = System.nanoTime();

//		if (np.rels.isEmpty()) {
//			return false;
//		}
		if (np.isDummyPath()) {
			return false;
		}

		this.checkEmpty += System.nanoTime() - r2a;

		long rr = System.nanoTime();
		myNode my_endNode = this.tmpStoreNodes.get(np.endNode);
		this.map_operation += System.nanoTime() - rr;

		long dsad = System.nanoTime();
		long d1 = 0, d2 = 0;
		boolean flag = false;

		for (Data d : d_list) {
			if (!this.dominated_checking.containsKey(d.getPlaceId()) || d.getPlaceId() == queryD.getPlaceId()) {
				continue;
			}

			this.pro_add_result_counter++;
			long rrr = System.nanoTime();

			if (d.getPlaceId() == queryD.getPlaceId()) {
				continue;
			}

			double[] final_costs = new double[np.costs.length + 3];
			System.arraycopy(np.costs, 0, final_costs, 0, np.costs.length);
			d.distance_q = constants.distanceInMeters(d.location[0], d.location[1], queryD.location[0],
					queryD.location[1]);

			double end_distance = constants.distanceInMeters(my_endNode.locations[0], my_endNode.locations[1],
					d.location[0], d.location[1]);

			final_costs[0] += end_distance;
			// Lemma 3 & Lemma 4
			if (final_costs[0] < d.distance_q && final_costs[0] < this.dominated_checking.get(d.getPlaceId())) {

				double[] d_attrs = d.getData();
				for (int i = 4; i < final_costs.length; i++) {
					final_costs[i] = d_attrs[i - 4];
				}

				Result r = new Result(this.queryD, d, final_costs, np);

				this.check_add_oper += System.nanoTime() - rrr;
				d1 += System.nanoTime() - rrr;
				long rrrr = System.nanoTime();
				this.sky_add_result_counter++;
				boolean t = addToSkyline(r);

				this.add_oper += System.nanoTime() - rrrr;
				d2 += System.nanoTime() - rrrr;

				if (!flag && t) {
					flag = true;
				}
			}
		}

		this.read_data += (System.nanoTime() - d1 - d2 - dsad);
		return flag;
	}

	private boolean addToSkylineResultByLocation(double lat, double lng, path np, ArrayList<Data> d_list) {
		this.add_counter++;
		long r2a = System.nanoTime();

//		if (np.rels.isEmpty()) {
//			return false;
//		}

		if (np.isDummyPath()) {
			return false;
		}

		this.checkEmpty += System.nanoTime() - r2a;

		long rr = System.nanoTime();
		myNode my_endNode = this.tmpStoreNodes.get(np.endNode);
		this.map_operation += System.nanoTime() - rr;

		long dsad = System.nanoTime();
		long d1 = 0, d2 = 0;
		boolean flag = false;

		for (Data d : d_list) {
			if (!this.dominated_checking.containsKey(d.getPlaceId())) {
				continue;
			}

			this.pro_add_result_counter++;
			long rrr = System.nanoTime();

			double[] final_costs = new double[np.costs.length + 3];
			System.arraycopy(np.costs, 0, final_costs, 0, np.costs.length);

			d.distance_q = constants.distanceInMeters(d.location[0], d.location[1], lat, lng);
//	        
			double end_distance = constants.distanceInMeters(my_endNode.locations[0], my_endNode.locations[1],
					d.location[0], d.location[1]);

			final_costs[0] += end_distance;
			// lemma3
			// double d3 = Math.sqrt(Math.pow(d.location[0] - queryD.location[0], 2) +
			// Math.pow(d.location[1] - queryD.location[1], 2));

			if (final_costs[0] < d.distance_q && final_costs[0] < this.dominated_checking.get(d.getPlaceId())) {
				double[] d_attrs = d.getData();
				for (int i = 4; i < final_costs.length; i++) {
					final_costs[i] = d_attrs[i - 4];
				}

				Result r = new Result(lat, lng, d, final_costs, np);

				this.check_add_oper += System.nanoTime() - rrr;
				d1 += System.nanoTime() - rrr;
				long rrrr = System.nanoTime();
				this.sky_add_result_counter++;
				boolean t = addToSkyline(r);

				this.add_oper += System.nanoTime() - rrrr;
				d2 += System.nanoTime() - rrrr;

				if (!flag && t) {
					flag = true;
				}
			}
		}

		this.read_data += (System.nanoTime() - d1 - d2 - dsad);
		return flag;
	}

	public Data generateQueryData() {
		Data d = new Data(3);
		d.setPlaceId(9999999);
		float latitude = randomFloatInRange(0f, 180f);
		float longitude = randomFloatInRange(0f, 180f);
		d.setLocation(new double[] { latitude, longitude });

		float priceLevel = randomFloatInRange(0f, 5f);
		float Rating = randomFloatInRange(0f, 5f);
		float other = randomFloatInRange(0f, 5f);
		d.setData(new float[] { priceLevel, Rating, other });
		return d;
	}

	public float randomFloatInRange(float min, float max) {
		float random = min + r.nextFloat() * (max - min);
		return random;
	}

	public Node nearestNetworkNode(Data queryD) {
		Node nn_node = null;
		double distz = Float.MAX_VALUE;
		try (Transaction tx = this.graphdb.beginTx()) {
			ResourceIterable<Node> iter = this.graphdb.getAllNodes();
			for (Node n : iter) {
				double lat = (double) n.getProperty("lat");
				double log = (double) n.getProperty("log");

				double temp_distz = constants.distanceInMeters(lat, log, queryD.location[0], queryD.location[1]);
				if (distz > temp_distz && !this.tmpStoreNodes.containsKey(n.getId())) {
					nn_node = n;
					distz = temp_distz;
					this.nn_dist = distz;
				}
			}
			tx.success();
		}

		this.nn_dist = distz;
		return nn_node;
	}

	public Node nearestNetworkNode(double q_lat, double q_lng) {
		Node nn_node = null;
		double distz = Float.MAX_VALUE;
		try (Transaction tx = this.graphdb.beginTx()) {
			ResourceIterable<Node> iter = this.graphdb.getAllNodes();
			for (Node n : iter) {
				double lat = (double) n.getProperty("lat");
				double log = (double) n.getProperty("log");

				double temp_distz = constants.distanceInMeters(lat, log, q_lat, q_lng);
				if (distz > temp_distz && !this.tmpStoreNodes.containsKey(n.getId())) {
					nn_node = n;
					distz = temp_distz;
					this.nn_dist = distz;
				}
			}
			tx.success();
		}

		this.nn_dist = distz;
//        nn_dist = (int) Math.ceil(distz);
		return nn_node;
	}

	public boolean addToSkyline(Result r) {
		int i = 0;
//        if (r.end.getPlaceId() == checkedDataId) {
//            System.out.println(r);
//        }
		if (skyPaths.isEmpty()) {
			this.skyPaths.add(r);
		} else {
			boolean can_insert_np = true;
			for (; i < skyPaths.size();) {
				if (checkDominated(skyPaths.get(i).costs, r.costs)) {
					can_insert_np = false;
					break;
				} else {
					if (checkDominated(r.costs, skyPaths.get(i).costs)) {
						this.skyPaths.remove(i);
					} else {
						i++;
					}
				}
			}

			if (can_insert_np) {
				this.skyPaths.add(r);
				return true;
			}
		}

		return false;
	}

	private boolean checkDominated(double[] costs, double[] estimatedCosts) {
		for (int i = 0; i < costs.length; i++) {
			if (costs[i] * (1.0) > estimatedCosts[i]) {
				return false;
			}
		}
		return true;
	}

	public Data getDataById(int placeId) {
		BufferedReader br = null;
		int linenumber = 0;

		Data queryD = new Data(3);

		try {
			br = new BufferedReader(new FileReader(this.dataPath));
			String line = null;
			while ((line = br.readLine()) != null) {
				if (linenumber == placeId) {
//                    System.out.println(line);
					String[] infos = line.split(",");
					Double lat = Double.parseDouble(infos[1]);
					Double log = Double.parseDouble(infos[2]);

					Float c1 = Float.parseFloat(infos[3]);
					Float c2 = Float.parseFloat(infos[4]);
					Float c3 = Float.parseFloat(infos[5]);

					queryD.setPlaceId(placeId);
					queryD.setLocation(new double[] { lat, log });
					queryD.setData(new float[] { c1, c2, c3 });
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
		return queryD;
	}

	public int getRandomNumberInRange_int(int min, int max) {

		if (min >= max) {
			throw new IllegalArgumentException("max must be greater than min");
		}

		Random r = new Random();
		return r.nextInt((max - min) + 1) + min;
	}

	public int getNumberOfHotels() {
		int result = 0;
		File f = new File(this.dataPath);
		BufferedReader b = null;
		try {
			b = new BufferedReader(new FileReader(f));
			String readLine = "";

			while (((readLine = b.readLine()) != null)) {
				result++;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return result;

	}
}
