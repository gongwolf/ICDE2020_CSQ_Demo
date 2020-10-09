package cs.nmsu.edu.demo.services;

import java.util.ArrayList;

import cs.nmsu.edu.demo.methods.Result;
import cs.nmsu.edu.demo.utilities.constants;

public class ResultBean {
	public long start, end;
	public double[] start_location;
	public double[] end_location;
	public String end_name;
	public ArrayList<NodeBeans> nodeIDs;
	public ArrayList<Long> relsIDs;
	public double[] costs = new double[constants.path_dimension + 3];
	public double[] querycosts = new double[constants.path_dimension + 3];

	public ResultBean() {
		this.start = this.end = -1;
		this.nodeIDs = new ArrayList<>();
		this.nodeIDs = new ArrayList<>();
		this.start_location = new double[2];
		this.end_location = new double[2];
		this.end_name = "";

	}

	public ResultBean(Result r) {
		this.start = r.start.getPlaceId();
		this.end = r.end.getPlaceId();

		this.start_location = new double[2];
		this.end_location = new double[2];
		this.start_location = r.start.location;
		this.end_location = r.end.location;
		
		this.nodeIDs = new ArrayList<NodeBeans>();
		this.relsIDs = new ArrayList<Long>();

		if (r.p != null) {
			this.relsIDs.addAll(r.p.rels);
			
			for (long nid : r.p.nodes) {
				NodeBeans nbean = new NodeBeans();
				nbean.setId(nid);
				this.nodeIDs.add(nbean);
			}
		}

		this.costs = r.costs;
		this.costs[4]= 5-r.costs[4];
		this.costs[5]= r.costs[5];
		this.costs[6]= 10-r.costs[6];
	}

}
