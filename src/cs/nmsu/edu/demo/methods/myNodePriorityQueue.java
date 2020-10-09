package cs.nmsu.edu.demo.methods;

import java.util.Comparator;
import java.util.PriorityQueue;

import cs.nmsu.edu.demo.RstarTree.Data;
import cs.nmsu.edu.demo.RstarTree.Node;

public class myNodePriorityQueue {
	PriorityQueue<myNode> queue;

	public myNodePriorityQueue() {
		NodeComparator nc = new NodeComparator();
		this.queue = new PriorityQueue<>(nc);
	}

	public boolean add(myNode p) {
		return this.queue.add(p);
	}

	public int size() {
		return this.queue.size();
	}

	public boolean isEmpty() {
		return this.queue.isEmpty();
	}

	public myNode pop() {
		return this.queue.poll();
	}
}

class NodeComparator implements Comparator<myNode> {
	@Override
	public int compare(myNode o1, myNode o2) {
		if (o1.distance_q == o2.distance_q) {
			return 0;
		} else if (o1.distance_q > o2.distance_q) {
			return 1;
		} else {
			return -1;
		}
	}
}

class myQueue {
	PriorityQueue queue;

	public myQueue(Data queryD) {
		ObjComparator mc = new ObjComparator(queryD);
		this.queue = new PriorityQueue(mc);
	}

	public myQueue() {
		defaultComparator mc = new defaultComparator();
		this.queue = new PriorityQueue(mc);
	}

	public boolean add(Object d) {
		return this.queue.add(d);
	}

	public int size() {
		return this.queue.size();
	}

	public boolean isEmpty() {
		return this.queue.isEmpty();
	}

	public Object pop() {
		return this.queue.poll();
	}
}

class ObjComparator implements Comparator {
	Data qD;

	public ObjComparator(Data queryD) {
		this.qD = queryD;
	}

	@Override
	public int compare(Object x, Object y) {
		double disX = 0, disY = 0;
		if (x.getClass() == Data.class) {
			Data dx = (Data) x;
			float[] x_mbr = dx.get_mbr();
			disX = getDistance_Point(x_mbr, qD);
		} else if (x instanceof Node) {
			float[] x_mbr = ((Node) x).get_mbr();
			disX = getDistance_Node(x_mbr, qD);
		}

		if (y.getClass() == Data.class) {
			Data dy = (Data) y;
			float[] y_mbr = dy.get_mbr();
			disY = getDistance_Point(y_mbr, qD);
		} else if (y instanceof Node) {
			float[] y_mbr = ((Node) y).get_mbr();
			disY = getDistance_Node(y_mbr, qD);
		}

		if (disX == disY) {
			return 0;
		} else if (disX > disY) {
			return 1;
		} else {
			return -1;
		}
	}

	private double getDistance_Node(float[] mbr, Data qD) {
		float sum = (float) 0.0;
		float r;
		int i;

		float points[] = new float[qD.dimension];
		for (int j = 0; j < qD.dimension; j++) {
			points[j] = qD.data[j * 2];

		}

		for (i = 0; i < qD.dimension; i++) {
			if (points[i] < mbr[2 * i]) {
				r = mbr[2 * i];
			} else {
				if (points[i] > mbr[2 * i + 1]) {
					r = mbr[2 * i + 1];
				} else {
					r = points[i];
				}
			}

			sum += Math.pow(points[i] - r, 2);
		}
		return Math.sqrt(sum);
	}

	private double getDistance_Point(float[] mbr, Data qD) {
		double dist = 0;
		for (int i = 0; i < 2 * qD.dimension; i += 2) {
			dist += Math.pow(qD.data[i] - mbr[i], 2);
		}
		return Math.sqrt(dist);
	}
}

class defaultComparator implements Comparator {

	@Override
	public int compare(Object x, Object y) {
		double disX = 0, disY = 0;
		if (x.getClass() == Data.class) {
			Data dx = (Data) x;
			float[] x_mbr = dx.get_mbr();
			disX = getDistance_Point(x_mbr);
		} else if (x instanceof Node) {
			float[] x_mbr = ((Node) x).get_mbr();
			disX = getDistance_Node(x_mbr);
		}

		if (y.getClass() == Data.class) {
			Data dy = (Data) y;
			float[] y_mbr = dy.get_mbr();
			disY = getDistance_Point(y_mbr);
		} else if (y instanceof Node) {
			float[] y_mbr = ((Node) y).get_mbr();
			disY = getDistance_Node(y_mbr);
		}

		if (disX == disY) {
			return 0;
		} else if (disX > disY) {
			return 1;
		} else {
			return -1;
		}
	}

	private double getDistance_Node(float[] mbr) {
		float sum = (float) 0.0;
		float r;
		int i;

		float points[] = new float[mbr.length / 2];

		for (i = 0; i < points.length; i++) {
			if (points[i] < mbr[2 * i]) {
				r = mbr[2 * i];
			} else {
				if (points[i] > mbr[2 * i + 1]) {
					r = mbr[2 * i + 1];
				} else {
					r = points[i];
				}
			}

			sum += Math.pow(points[i] - r, 2);
		}
		return Math.sqrt(sum);
	}

	private double getDistance_Point(float[] mbr) {
		double dist = 0;
		for (int i = 0; i < mbr.length; i += 2) {
			dist += Math.pow(mbr[i], 2);
		}
		return Math.sqrt(dist);
	}
}
