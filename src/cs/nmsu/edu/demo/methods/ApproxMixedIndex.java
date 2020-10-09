package cs.nmsu.edu.demo.methods;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterable;
import org.neo4j.graphdb.Transaction;

import cs.nmsu.edu.demo.RstarTree.*;
import cs.nmsu.edu.demo.methods.*;
import cs.nmsu.edu.demo.neo4jTools.connector;
import cs.nmsu.edu.demo.methods.Index;
import cs.nmsu.edu.demo.utilities.QueryParameters;
import cs.nmsu.edu.demo.utilities.constants;

public class ApproxMixedIndex {
	private final String graphPath;
	public ArrayList<Result> skyPaths = new ArrayList<>();
	public ArrayList<Data> sky_hotel;
	Random r;
	String treePath;
	String dataPath;
	int graph_size;
	String degree;
	long add_oper = 0;
	long check_add_oper = 0;
	long map_operation = 0;
	long checkEmpty = 0;
	long read_data = 0;
	// Todo: each hotel know the distance to the hotel than dominate it.
	HashMap<Integer, Double> dominated_checking = new HashMap<>(); //
	// int distance_threshold = Integer.MAX_VALUE;
	double distance_threshold = 0.001;
	String home_folder = System.getProperty("user.home");
	private int hotels_num;
	private GraphDatabaseService graphdb;
	private HashMap<Long, myNode> tmpStoreNodes = new HashMap();
	private ArrayList<Data> sNodes = new ArrayList<>();
	private HashSet<Integer> finalDatas = new HashSet<Integer>();
	private int checkedDataId = 9;
	private long add_counter; // how many times call the addtoResult function
	private long pro_add_result_counter; // how many path + hotel combination of the results are generated
	private long sky_add_result_counter; // how many results are taken the addtoskyline operation
	private Data queryD;
	private int idx_num;
	private String city;
	private QueryParameters qp = new QueryParameters();

	public ApproxMixedIndex(String city, double distance_threshold, QueryParameters qp) {
		r = new Random();
		this.city = city;
		this.distance_threshold = distance_threshold;
		this.qp = new QueryParameters(qp);

		if (qp.type == null || qp.type.equals("")) {
			this.treePath = constants.data_path + "/real_tree_" + city + ".rtr";
			this.dataPath = constants.data_path + "/staticNode_real_" + city + ".txt";
		} else {
			this.treePath = constants.data_path + "/real_tree_" + city + "_" + qp.getType() + ".rtr";
			this.dataPath = constants.data_path + "/staticNode_real_" + city + "_" + qp.getType() + ".txt";
		}

		this.graphPath = constants.db_path + "/testdb_" + city + "_Gaussian/databases/graph.db";
	}

	public void baseline(Data queryD) {
		clearTempResult();
		System.out.println(this.qp);
		long r1 = System.currentTimeMillis();

		long s_sum = System.currentTimeMillis();
		long db_time = System.currentTimeMillis();
		connector n = new connector(graphPath);
		n.startDB();
		this.graphdb = n.getDBObject();

		long counter = 0;
		long addResult_rt = 0;
		long expasion_rt = 0;
		int sk_counter = 0; // the number of total candidate hotels of each bus station

		db_time = System.currentTimeMillis() - db_time;
		r1 = System.currentTimeMillis();
		long nn_rt = System.currentTimeMillis() - r1;

		this.queryD = queryD;
		StringBuffer sb = new StringBuffer();
		sb.append(queryD.getPlaceId() + "|");

		Skyline sky = new Skyline(treePath);
		// find the skyline hotels of the whole dataset.
		sky.findSkyline(queryD);

		this.sky_hotel = new ArrayList<>(sky.sky_hotels);

		long index_s = 0;

		r1 = System.currentTimeMillis();
		// Find the hotels that aren't dominated by the query point
		sky.BBS(queryD);
		long bbs_rt = System.currentTimeMillis() - r1;
		sNodes = sky.skylineStaticNodes;

		sb.append(this.sNodes.size() + " " + this.sky_hotel.size() + "|");

		for (Data d : sNodes) {
			double[] c = new double[constants.path_dimension + 3];
			c[0] = d.distance_q;

			if (c[0] <= this.distance_threshold) {
				double[] d_attrs = d.getData();
				for (int i = 4; i < c.length; i++) {
					c[i] = d_attrs[i - 4];
				}
				Result r = new Result(queryD, d, c, null);
				addToSkyline(r);
			}
		}

		sb.append(" #init_skyline_paths:" + this.skyPaths.size() + " ");

		// find the minimum distance from query point to the skyline hotel that dominate
		// non-skyline hotel cand_d
		for (Data cand_d : sNodes) {
			double h_to_h_dist = Double.MAX_VALUE;
			if (!this.sky_hotel.contains(cand_d)) {
				for (Data s_h : this.sky_hotel) {
					if (checkDominated(s_h.getData(), cand_d.getData())) {
						double tmep_dist = s_h.distance_q;
						if (tmep_dist < h_to_h_dist) {
							h_to_h_dist = tmep_dist;
						}
					}
				}
			} else {
				h_to_h_dist = Double.MAX_VALUE;
			}
			dominated_checking.put(cand_d.getPlaceId(), h_to_h_dist);
		}

		int visited_bus_stop = 0;
		try (Transaction tx = n.graphDB.beginTx()) {

			long rt = System.currentTimeMillis();

			myNodePriorityQueue mqueue = new myNodePriorityQueue();
			HashSet<Long> nodesInRange = nearestNetworkNodeInRange(queryD, n);
			for (long sid : nodesInRange) {
				myNode s = new myNode(queryD, sid, this.distance_threshold, n);
				mqueue.add(s);
				this.tmpStoreNodes.put(s.id, s);
			}

			while (!mqueue.isEmpty()) {

				myNode v = mqueue.pop();
				v.inqueue = false;
				counter++;

				path[] needToProcess = new path[constants.path_dimension];
				for (path p : v.skyPaths) {
//                    if (!p.expaned) {
					if (p.isDummyPath()) {
						needToProcess[0] = p;
					} else if (needToProcess[1] == null) {
						for (int i = 1; i < needToProcess.length; i++) {
							needToProcess[i] = p;
						}
					} else {
						for (int i = 1; i < needToProcess.length; i++) {
							if (needToProcess[i].costs[i] > p.costs[i]) {
								needToProcess[i] = p;
							}
						}
					}
//                    }
				}

				for (int i = 0; i < needToProcess.length; i++) {
					path p = needToProcess[i];
					if (p != null && !p.expaned) {
						p.expaned = true;

						long ee = System.nanoTime();
						ArrayList<path> new_paths = p.expand(n);
						expasion_rt += (System.nanoTime() - ee);

						for (path np : new_paths) {
							boolean num_bus_stop_query_flag = false;

							if (this.qp.getNum_bus_stop() != -1) {
								if (np.rels.size() <= this.qp.getNum_bus_stop()) {
									num_bus_stop_query_flag = true;
								}
							} else {
								num_bus_stop_query_flag = true;
							}

							if (num_bus_stop_query_flag) {
								myNode next_n;
								if (this.tmpStoreNodes.containsKey(np.endNode)) {
									next_n = tmpStoreNodes.get(np.endNode);
								} else {
									next_n = new myNode(queryD, np.endNode, this.distance_threshold, n);
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

			long exploration_rt = System.currentTimeMillis() - rt;

			long tt_sl = 0;

//            hotels_scope = new HashMap<>();

//            System.out.println("there are " + this.tmpStoreNodes.size() + " bus stops are visited");

			int process_visited_stations = 0;
			for (Map.Entry<Long, myNode> entry : tmpStoreNodes.entrySet()) {
				sk_counter += entry.getValue().skyPaths.size();
			}

//            System.out.println(sk_counter+"~~~~~");

//			Index idx = new Index(qp.city, this.distance_threshold, qp.type);
			Index idx = new Index(qp.city, -1, qp.type);
			for (Map.Entry<Long, myNode> entry : tmpStoreNodes.entrySet()) {
				long t_index_s = System.nanoTime();
				myNode my_n = entry.getValue();
				ArrayList<Data> d_list = idx.read_d_list_from_disk(my_n.id);
				index_s += (System.nanoTime() - t_index_s);

				for (path p : my_n.skyPaths) {
//                    if (!p.rels.isEmpty() && p.expaned) {
					if (p.expaned) {
						long ats = System.nanoTime();
						boolean f = addToSkylineResult(p, d_list);
						addResult_rt += System.nanoTime() - ats;
					}
//                    }
				}
			}

			visited_bus_stop = this.tmpStoreNodes.size();

//            System.out.println(sk_counter);

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

		visited_bus_stop = this.tmpStoreNodes.size();
		int bus_stop_in_result = final_bus_stops.size();

		sb.append(visited_bus_stop + "," + bus_stop_in_result + "," + (double) bus_stop_in_result / visited_bus_stop
				+ "|" + this.sky_add_result_counter);

		sb.append("," + add_counter + "," + sk_counter + "," + counter);

		System.out.println(sb.toString());
	}

	public void baseline(double lat, double lng) {
		clearTempResult();
		long r1 = System.currentTimeMillis();

		long s_sum = System.currentTimeMillis();
		long db_time = System.currentTimeMillis();
		connector n = new connector(graphPath);
		n.startDB();
		this.graphdb = n.getDBObject();

		long counter = 0;
		long addResult_rt = 0;
		long expasion_rt = 0;
		int sk_counter = 0; // the number of total candidate hotels of each bus station

		db_time = System.currentTimeMillis() - db_time;
		r1 = System.currentTimeMillis();
		long nn_rt = System.currentTimeMillis() - r1;
//        System.out.println("find the nearest bus stop " + nn_rt + "  ms  ");

		StringBuffer sb = new StringBuffer();
		sb.append("[" + lat + "," + lng + "]" + "|");

		Skyline sky = new Skyline(treePath);

		// find the skyline hotels of the whole dataset.
		sky.findSkyline(lat, lng);
		this.sky_hotel = new ArrayList<>(sky.sky_hotels);
		long index_s = 0;

		r1 = System.currentTimeMillis();
		// Find the hotels that aren't dominated by the query point
		sky.allDatas(lat, lng);
		long bbs_rt = System.currentTimeMillis() - r1;
		sNodes = sky.allNodes;

		for (Data d : sNodes) {
			double[] c = new double[constants.path_dimension + 3];
			c[0] = d.distance_q;

			if (c[0] <= this.distance_threshold) {
				double[] d_attrs = d.getData();
				for (int i = 4; i < c.length; i++) {
					c[i] = d_attrs[i - 4];
				}
				Result r = new Result(lat, lng, d, c, null);
				addToSkyline(r);
			}
		}

		sb.append(this.sNodes.size() + " " + this.sky_hotel.size() + "|");

		// find the minimum distance from query point to the skyline hotel that dominate
		// non-skyline hotel cand_d
		for (Data cand_d : sNodes) {
			double h_to_h_dist = Double.MAX_VALUE;
			if (!this.sky_hotel.contains(cand_d)) {
				for (Data s_h : this.sky_hotel) {
					if (checkDominated(s_h.getData(), cand_d.getData())) {
						double tmep_dist = s_h.distance_q;
						if (tmep_dist < h_to_h_dist) {
							h_to_h_dist = tmep_dist;
						}
					}
				}
			} else {
				h_to_h_dist = Double.MAX_VALUE;
			}
			dominated_checking.put(cand_d.getPlaceId(), h_to_h_dist);
		}

		int visited_bus_stop = 0;
		try (Transaction tx = n.graphDB.beginTx()) {

			long rt = System.currentTimeMillis();

			myNodePriorityQueue mqueue = new myNodePriorityQueue();

			HashSet<Long> nodesInRange = nearestNetworkNodeInRange(lat, lng, n);

			for (long sid : nodesInRange) {
				myNode s = new myNode(lat, lng, sid, this.distance_threshold, n);
//	            System.out.println(s);
				mqueue.add(s);
				this.tmpStoreNodes.put(s.id, s);
			}

			while (!mqueue.isEmpty()) {

				myNode v = mqueue.pop();
				v.inqueue = false;
				counter++;

				path[] needToProcess = new path[constants.path_dimension];
				for (path p : v.skyPaths) {
//                    if (!p.expaned) {
					if (p.isDummyPath()) {
						needToProcess[0] = p;
					} else if (needToProcess[1] == null) {
						for (int i = 1; i < needToProcess.length; i++) {
							needToProcess[i] = p;
						}
					} else {
						for (int i = 1; i < needToProcess.length; i++) {
							if (needToProcess[i].costs[i] > p.costs[i]) {
								needToProcess[i] = p;
							}
						}
					}
//                    }
				}

				for (int i = 0; i < needToProcess.length; i++) {
					path p = needToProcess[i];
					if (p != null && !p.expaned) {
						p.expaned = true;

						long ee = System.nanoTime();
						ArrayList<path> new_paths = p.expand(n);
						expasion_rt += (System.nanoTime() - ee);

						for (path np : new_paths) {
							boolean num_bus_stop_query_flag = false;

							if (this.qp.getNum_bus_stop() != -1) {
								if (np.rels.size() <= this.qp.getNum_bus_stop()) {
									num_bus_stop_query_flag = true;
								}
							} else {
								num_bus_stop_query_flag = true;
							}

							if (num_bus_stop_query_flag) {
								myNode next_n;
								if (this.tmpStoreNodes.containsKey(np.endNode)) {
									next_n = tmpStoreNodes.get(np.endNode);
								} else {
									next_n = new myNode(lat, lng, np.endNode, this.distance_threshold, n);
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

			long exploration_rt = System.currentTimeMillis() - rt;

			long tt_sl = 0;

			int process_visited_stations = 0;
			for (Map.Entry<Long, myNode> entry : tmpStoreNodes.entrySet()) {
				sk_counter += entry.getValue().skyPaths.size();
			}

//			Index idx = new Index(qp.city, this.distance_threshold, qp.type);
			Index idx = new Index(qp.city, -1, qp.type);
			for (Map.Entry<Long, myNode> entry : tmpStoreNodes.entrySet()) {
				long t_index_s = System.nanoTime();
				myNode my_n = entry.getValue();
				ArrayList<Data> d_list = idx.read_d_list_from_disk(my_n.id);
				index_s += (System.nanoTime() - t_index_s);

				for (path p : my_n.skyPaths) {
//                    if (!p.rels.isEmpty() && p.expaned) {
					if (p.expaned) {
						long ats = System.nanoTime();
						boolean f = addToSkylineResultLocation(lat, lng, p, d_list);
						addResult_rt += System.nanoTime() - ats;
					}
//                    }
				}
			}

			visited_bus_stop = this.tmpStoreNodes.size();

//            System.out.println(sk_counter);

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

		visited_bus_stop = this.tmpStoreNodes.size();
		int bus_stop_in_result = final_bus_stops.size();

		sb.append(visited_bus_stop + "," + bus_stop_in_result + "," + (double) bus_stop_in_result / visited_bus_stop
				+ "|" + this.sky_add_result_counter);

		sb.append("," + add_counter + "," + sk_counter + "," + counter);

		System.out.println(sb.toString());
	}

	private boolean addToSkylineResultLocation(double lat, double lng, path np, ArrayList<Data> d_list) {
		this.add_counter++;
		long r2a = System.nanoTime();
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
//            if (np.startNode.getId() == 286 && np.endNode.getId() == 1862) {
//                if (d.getPlaceId() == 62) {
//                    System.out.println("true");
//                }
//            }

			this.pro_add_result_counter++;
			long rrr = System.nanoTime();

			double[] final_costs = new double[np.costs.length + 3];
			System.arraycopy(np.costs, 0, final_costs, 0, np.costs.length);
//			double end_distance = Math.sqrt(Math.pow(my_endNode.locations[0] - d.location[0], 2)
//					+ Math.pow(my_endNode.locations[1] - d.location[1], 2));
//			d.distance_q = Math.sqrt(
//					Math.pow(queryD.location[0] - d.location[0], 2) + Math.pow(queryD.location[1] - d.location[1], 2));
			double end_distance = constants.distanceInMeters(my_endNode.locations[0], my_endNode.locations[1],
					d.location[0], d.location[1]);
			d.distance_q = constants.distanceInMeters(lat, lng, d.location[0], d.location[1]);
//			System.out.println(d);

			final_costs[0] += end_distance;

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

	private boolean addToSkylineResult(path np, ArrayList<Data> d_list) {
		this.add_counter++;
		long r2a = System.nanoTime();
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

			double[] final_costs = new double[np.costs.length + 3];
			System.arraycopy(np.costs, 0, final_costs, 0, np.costs.length);
			double end_distance = constants.distanceInMeters(my_endNode.locations[0], my_endNode.locations[1],
					d.location[0], d.location[1]);
			d.distance_q = constants.distanceInMeters(queryD.location[0], queryD.location[1], d.location[0],
					d.location[1]);

			final_costs[0] += end_distance;

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

	public HashSet<Long> nearestNetworkNodeInRange(Data queryD, connector connector) {

		HashSet<Long> nodeIDinRange = new HashSet<>();

		Node nn_node = null;
		double distz = Double.MAX_VALUE;
		int counter_in_range = 0;

		try (Transaction tx = connector.graphDB.beginTx()) {
			ResourceIterable<Node> iter = connector.graphDB.getAllNodes();
			for (Node n : iter) {
				double lat = (double) n.getProperty("lat");
				double log = (double) n.getProperty("log");

				double temp_distz = constants.distanceInMeters(lat, log, queryD.location[0], queryD.location[1]);
				if (distz > temp_distz && temp_distz <= this.distance_threshold) {
					nn_node = n;
					distz = temp_distz;
					nodeIDinRange.add(n.getId());
					counter_in_range++;
				}
			}

			tx.success();
		}
		return nodeIDinRange;
	}

	public HashSet<Long> nearestNetworkNodeInRange(double q_lat, double q_lng, connector connector) {
		HashSet<Long> nodeIDinRange = new HashSet<>();

		Node nn_node = null;
		double distz = Double.MAX_VALUE;
		int counter_in_range = 0;

		try (Transaction tx = connector.graphDB.beginTx()) {

			ResourceIterable<Node> iter = connector.graphDB.getAllNodes();
			for (Node n : iter) {
				double lat = (double) n.getProperty("lat");
				double lng = (double) n.getProperty("log");

//                double temp_distz = Math.sqrt(Math.pow(lat - q_lat, 2) + Math.pow(lng - q_lng, 2));
				double temp_distz = constants.distanceInMeters(lat, lng, q_lat, q_lng);
				if (distz > temp_distz && temp_distz <= this.distance_threshold) {
					nn_node = n;
					distz = temp_distz;
					counter_in_range++;
					nodeIDinRange.add(n.getId());
				}
			}
			tx.success();
		}

		return nodeIDinRange;
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
	

	public void clearTempResult() {
		this.tmpStoreNodes.clear();
		this.sNodes.clear();
		this.finalDatas.clear();
		this.skyPaths.clear();
		this.sky_hotel.clear();
	}

}