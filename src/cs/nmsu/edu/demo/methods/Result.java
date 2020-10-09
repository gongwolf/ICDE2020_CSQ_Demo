package cs.nmsu.edu.demo.methods;

import cs.nmsu.edu.demo.RstarTree.Data;
import cs.nmsu.edu.demo.utilities.*;

public class Result implements Comparable<Result> {
    public Data start, end;
    public path p;
    public double[] costs = new double[constants.path_dimension + 3];
    public double score=0.0;


    public Result(Data queryD, Data destination, double[] c, path np) {
        this.start = queryD;
        this.end = destination;
        System.arraycopy(c, 0, this.costs, 0, c.length);
        p = np;
    }

    public Result(double lat, double lng, Data destination, double[] c, path np) {
    	this.start = new Data(constants.path_dimension-1);
    	this.start.setPlaceId(-999);
    	this.start.setLocation(new double[] {lat,lng});
        
    	this.end = destination;
        System.arraycopy(c, 0, this.costs, 0, c.length);
        p = np;
	}

	@Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Result)) {
            return false;
        }
        Result or = (Result) obj;
        if (!start.equals(or.start)) {
            return false;
        } else if (!end.equals(or.end)) {
            return false;
        } else if (!or.p.equals(this.p)) {
            return false;
        } else {
            return true;
        }

    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.start.getPlaceId()).append(",").append(this.start.location[0]).append(",").append(this.start.location[1]).append(",");
        sb.append(this.end.getPlaceId()).append(",").append(this.end.location[0]).append(",").append(this.end.location[1]).append(",");
//        sb.append("path.append(\""+this.p+"\")");
        if(this.p==null)
        {
            sb.append(",[]");
        }else {
            sb.append(p);
        }
        sb.append(",[");
        for (double c : this.costs) {
            sb.append(c).append(" ");
        }
        sb.substring(0,sb.lastIndexOf(" "));
        sb.append("]");
        return sb.toString();
    }

    @Override
    public int compareTo(Result o) {
        if (o.score == this.score) {
            return 0;
        } else if (o.score > this.score) {
            return 1;
        } else {
            return -1;
        }
    }
}


