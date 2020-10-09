package cs.nmsu.edu.demo.methods;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;

import cs.nmsu.edu.demo.neo4jTools.Line;
import cs.nmsu.edu.demo.neo4jTools.connector;
import cs.nmsu.edu.demo.utilities.constants;

import java.util.ArrayList;
import java.util.Iterator;

public class path {
	public double[] costs;
	public boolean expaned;
	public long startNode, endNode;

	public ArrayList<Long> nodes;
	public ArrayList<Long> rels;
	public ArrayList<String> propertiesName;

	public path(myNode current,connector n) {
		this.costs = new double[constants.path_dimension];
		costs[0] = current.distance_q;
		costs[1] = costs[2] = costs[3] = 0;
//        constants.print(costs);
		this.startNode = current.node;
		this.endNode = current.node;
		this.expaned = false;

		this.nodes = new ArrayList<>();
		this.rels = new ArrayList<>();
		this.propertiesName = new ArrayList<>();

		this.setPropertiesName(n);

		// store the Long Objects
//		this.nodes.add(getLongObject_Node(this.endNode));
		this.nodes.add(current.id);
	}

	public path(path old_path, Relationship rel) {
		this.costs = new double[constants.path_dimension];
		this.startNode = old_path.startNode;
		this.endNode = rel.getEndNodeId();

		this.nodes = new ArrayList<>(old_path.nodes);
//		for (long n : old_path.nodes) {
////			this.nodes.add(getLongObject_Node(n));
//			this.nodes.add(n);
//		}
//
		this.rels = new ArrayList<>(old_path.rels);
//		for (long e : old_path.rels) {
////			this.rels.add(getLongObject_Edge(e));
//			this.rels.add(e);
//		}

		this.propertiesName = new ArrayList<>(old_path.propertiesName);

		expaned = false;

//        this.nodes.add(getLongObject_Node(this.endNode));
//        this.rels.add(getLongObject_Edge(rel.getId()));
		this.rels.add(rel.getId());
		this.nodes.add(this.endNode);
		System.arraycopy(old_path.costs, 0, this.costs, 0, this.costs.length);
		calculateCosts(rel);
	}

//    public ArrayList<path> expand() {
//        ArrayList<path> result = new ArrayList<>();
//
//        ArrayList<Relationship> outgoing_rels = connector.getOutgoutingEdges(this.endNode);
////        System.out.println("  expand " +this.endNode+" "+outgoing_rels.size()+" | "+this.nodes.size()+" "+this.rels.size());
////        System.out.println(outgoing_rels.size());
//
//        for (Relationship r : outgoing_rels) {
//            path nPath = new path(this, r);
//            result.add(nPath);
//        }
//        return result;
//    }

	public ArrayList<path> expand(connector n) {
		ArrayList<path> result = new ArrayList<>();
		try (Transaction tx = n.graphDB.beginTx()) {
			Iterable<Relationship> rels = n.graphDB.getNodeById(this.endNode).getRelationships(Line.Linked, Direction.OUTGOING);
			Iterator<Relationship> rel_Iter = rels.iterator();
			while (rel_Iter.hasNext()) {
				Relationship rel = rel_Iter.next();
				path nPath = new path(this, rel);
				result.add(nPath);
			}
			tx.success();
		}
		return result;
	}

	private void calculateCosts(Relationship rel) {
//        System.out.println(this.propertiesName.size());
		if (this.startNode != this.endNode) {
			int i = 1;
			for (String pname : this.propertiesName) {
//                System.out.println(i+" "+this.costs[i]+"  "+Double.parseDouble(rel.getProperty(pname).toString()));

				this.costs[i] = this.costs[i] + (double) rel.getProperty(pname);
				i++;
			}
		}
	}

	public void setPropertiesName(connector n) {
		this.propertiesName = n.propertiesName;
	}

	public String toString() {
//        System.out.println("dasdasd:   "+this.nodes.size()+"  "+this.rels.size());
		StringBuffer sb = new StringBuffer();
//        if (this.rels.isEmpty()) {
//            sb.append("(" + this.startNode + ")");
//        } else {
//            int i;
//            for (i = 0; i < this.nodes.size() - 1; i++) {
//                sb.append("(" + this.nodes.get(i) + ")");
//                // sb.append("-[Linked," + this.relationships.get(i).getId() +
//                // "]->");
//                sb.append("-[" + this.rels.get(i) + "]-");
//            }
//            sb.append("(" + this.nodes.get(i) + ")");
//        }

		sb.append(",[");
		for (double d : this.costs) {
			sb.append(" " + d);
		}
		sb.append("]");
		return sb.toString();
	}

	@Override
	public boolean equals(Object obj) {

		if ((obj == null && this != null) || (obj != null && this == null)) {
			return false;
		}

		if (obj == this)
			return true;
		if (!(obj instanceof path))
			return false;

		path o_path = (path) obj;
		if (o_path.endNode != endNode || o_path.startNode != startNode) {
			return false;
		}

		for (int i = 0; i < costs.length; i++) {
			if (o_path.costs[i] != costs[i]) {
				return false;
			}
		}

		if (!o_path.nodes.equals(this.nodes) || !o_path.rels.equals(this.rels)) {
			return false;
		}
		return true;
	}

//	public Long getLongObject_Node(long id) {
//		Long id_obj = new Long(id);
//		Long Lobj;
//		if (!constants.accessedNodes.containsKey(id_obj)) {
//			Lobj = new Long(id);
//			constants.accessedNodes.put(id_obj, Lobj);
//		} else {
//			Lobj = constants.accessedNodes.get(id_obj);
//		}
//
//		return Lobj;
//	}

//	public Long getLongObject_Edge(long id) {
//		Long id_obj = new Long(id);
//		Long Lobj;
//		if (!constants.accessedEdges.containsKey(id_obj)) {
//			Lobj = new Long(id);
//			constants.accessedEdges.put(id_obj, Lobj);
//		} else {
//			Lobj = constants.accessedEdges.get(id_obj);
//		}
//
//		return Lobj;
//	}

	public boolean isDummyPath() {
		for (int i = 1; i < this.costs.length; i++) {
			if (this.costs[i] != 0) {
				return false;
			}
		}
		return true;
	}

	public boolean hasCycle() {
		for (int i = 0; i < rels.size() - 2; i++) {
			if (this.endNode == rels.get(i)) {
				return true;
			}
		}
		return false;
	}
}
