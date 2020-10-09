package cs.nmsu.edu.demo.methods;

import java.util.ArrayList;
import java.util.Random;

import cs.nmsu.edu.demo.RstarTree.*;
import cs.nmsu.edu.demo.utilities.constants;

public class Skyline {
    public ArrayList<Data> skylineStaticNodes = new ArrayList<>();
    public ArrayList<Data> allNodes = new ArrayList<>();
    public ArrayList<Data> sky_hotels = new ArrayList<>();
    String treePath = "data/test.rtr";
    RTree rt;
    Random r;

    public Skyline() {
        rt = new RTree(treePath, Constants.CACHESIZE);
        r = new Random(System.nanoTime());
    }

    public Skyline(String treePath) {
        rt = new RTree(treePath, Constants.CACHESIZE);
        r = new Random(System.nanoTime());
    }

    public static void main(String args[]) {
        Skyline sky = new Skyline();
        Data queryD = sky.generateQueryData();
        System.out.println(queryD);
        long rt = System.currentTimeMillis();
        sky.BBS(queryD);
        System.out.println(System.currentTimeMillis() - rt);

    }

    public int get_num_of_nodes() {
        return this.rt.num_of_data;
    }

    public void BBS(Data queryPoint) {
        myQueue queue = new myQueue(queryPoint);
        queue.add(rt.root_ptr);

        while (!queue.isEmpty()) {
            Object o = queue.pop();
            if (o.getClass() == RTDirNode.class) {
                RTDirNode dirN = (RTDirNode) o;
                int n = dirN.get_num();
                for (int i = 0; i < n; i++) {
                    Object succ_o = dirN.entries[i].get_son();
                    if (!isDominatedByQueryPoint((Node) succ_o, queryPoint)) {
                        queue.add(succ_o);
                    }
                }
            } else if (o.getClass() == RTDataNode.class) {
                RTDataNode dataN = (RTDataNode) o;
                int n = dataN.get_num();
                for (int i = 0; i < n; i++) {
                    if (!checkDominated(queryPoint.getData(), dataN.data[i].getData())) {
                        dataN.data[i].distance_q = constants.distanceInMeters(dataN.data[i].location[0], dataN.data[i].location[1], queryPoint.location[0], queryPoint.location[1]);
                        this.skylineStaticNodes.add(dataN.data[i]);
                    }
                }
            }
        }

        queryPoint.distance_q = 0;
        this.skylineStaticNodes.add(queryPoint);
    }

    public void allDatas(Data queryPoint) {
        myQueue queue = new myQueue(queryPoint);
        queue.add(rt.root_ptr);

        while (!queue.isEmpty()) {
            Object o = queue.pop();
            if (o.getClass() == RTDirNode.class) {
                RTDirNode dirN = (RTDirNode) o;
                int n = dirN.get_num();
                for (int i = 0; i < n; i++) {
                    Object succ_o = dirN.entries[i].get_son();
                    queue.add(succ_o);
                }
            } else if (o.getClass() == RTDataNode.class) {
                RTDataNode dataN = (RTDataNode) o;
                int n = dataN.get_num();
                for (int i = 0; i < n; i++) {
                    dataN.data[i].distance_q = constants.distanceInMeters(dataN.data[i].location[0], dataN.data[i].location[1], queryPoint.location[0], queryPoint.location[1]);
                    this.allNodes.add(dataN.data[i]);
                }
            }
        }

        queryPoint.distance_q = 0;
        this.allNodes.add(queryPoint);
    }
    
	public void allDatas(double lat, double lng) {
		myQueue queue = new myQueue();
        queue.add(rt.root_ptr);

        while (!queue.isEmpty()) {
            Object o = queue.pop();
            if (o.getClass() == RTDirNode.class) {
                RTDirNode dirN = (RTDirNode) o;
                int n = dirN.get_num();
                for (int i = 0; i < n; i++) {
                    Object succ_o = dirN.entries[i].get_son();
                    queue.add(succ_o);
                }
            } else if (o.getClass() == RTDataNode.class) {
                RTDataNode dataN = (RTDataNode) o;
                int n = dataN.get_num();
                for (int i = 0; i < n; i++) {
                    dataN.data[i].distance_q = constants.distanceInMeters(dataN.data[i].location[0], dataN.data[i].location[1], lat, lng);
                    this.allNodes.add(dataN.data[i]);
                }
            }
        }
	}


    public void allDatas() {
        myQueue queue = new myQueue();
        queue.add(rt.root_ptr);

        while (!queue.isEmpty()) {
            Object o = queue.pop();
            if (o.getClass() == RTDirNode.class) {
                RTDirNode dirN = (RTDirNode) o;
                int n = dirN.get_num();
                for (int i = 0; i < n; i++) {
                    Object succ_o = dirN.entries[i].get_son();
                    queue.add(succ_o);
                }
            } else if (o.getClass() == RTDataNode.class) {
                RTDataNode dataN = (RTDataNode) o;
                int n = dataN.get_num();
                for (int i = 0; i < n; i++) {
                    this.allNodes.add(dataN.data[i]);
                }
            }
        }
    }

    public Data generateQueryData() {
        Data d = new Data(3);
        d.setPlaceId(9999999);
        float latitude = randomFloatInRange(0f, 180f);
        float longitude = randomFloatInRange(0f, 180f);
        d.setLocation(new double[]{latitude, longitude});


        float priceLevel = randomFloatInRange(0f, 5f);
        float Rating = randomFloatInRange(0f, 5f);
        float other = randomFloatInRange(0f, 5f);
        d.setData(new float[]{priceLevel, Rating, other});
        return d;
    }

    public float randomFloatInRange(float min, float max) {
        float random = min + r.nextFloat() * (max - min);
        return random;
    }


    private boolean addToSkyline(Data d) {
        int i = 0;
        if (sky_hotels.isEmpty()) {
            this.sky_hotels.add(d);
        } else {
            boolean can_insert_np = true;
            for (; i < sky_hotels.size(); ) {
                if (checkDominated(sky_hotels.get(i).getData(), d.getData())) {
                    can_insert_np = false;
                    break;
                } else {
                    if (checkDominated(d.getData(), sky_hotels.get(i).getData())) {
                        this.sky_hotels.remove(i);
                    } else {
                        i++;
                    }
                }
            }

            if (can_insert_np) {
                this.sky_hotels.add(d);
                return true;
            }
        }
        return false;
    }

    private boolean isDominatedByQueryPoint(Node node, Data queryD) {

        double[] q_points = queryD.getData();
//            System.out.println(queryD);
        float[] n_mbr = node.get_mbr();
        boolean flag = true;
        for (int j = 0; j < n_mbr.length; j += 2) {
            flag = flag & (n_mbr[j] >= q_points[j / 2]);
            //if one dimension of the node is less than the point d
            //It means the point d can not fall into the left-bottom partition.
            //So the node can not be dominated by this d.
            if (!flag) {
                break;
            }
        }
        //if one of the data d dominate the node
        if (flag) {
            return true;
        }
        return false;
    }


    private boolean isDominatedByResult(Node node) {
        if (sky_hotels.isEmpty()) {
            return false;
        } else {
            float[] n_mbr = node.get_mbr();
            for (Data s : sky_hotels) {
                boolean flag = true;
                double[] dd = s.getData();
                for (int i = 0; i < dd.length; i++) {
                    if (dd[i] > n_mbr[i * 2]) {
                        flag = false;
                        break;
                    }
                }

                if (flag == true) {
                    return true;
                }
            }

        }
        return false;
    }


    private boolean checkDominated(double[] costs, double[] estimatedCosts) {
        for (int i = 0; i < costs.length; i++) {
            if (costs[i] > estimatedCosts[i]) {
                return false;
            }
        }
        return true;
    }


    public void findSkyline() {
        myQueue queue = new myQueue();
        queue.add(rt.root_ptr);
        while (!queue.isEmpty()) {
            Object o = queue.pop();
            if (o.getClass() == RTDirNode.class) {
                RTDirNode dirN = (RTDirNode) o;
                int n = dirN.get_num();
                for (int i = 0; i < n; i++) {
                    Object succ_o = dirN.entries[i].get_son();
                    if (!isDominatedByResult((Node) succ_o)) {
                        queue.add(succ_o);
                    }
                }
            } else if (o.getClass() == RTDataNode.class) {
                RTDataNode dataN = (RTDataNode) o;
                int n = dataN.get_num();
                for (int i = 0; i < n; i++) {
                    Data d = dataN.data[i];
                    d.distance_q = Math.pow(dataN.data[i].location[0], 2) + Math.pow(dataN.data[i].location[1], 2);
                    d.distance_q = Math.sqrt(dataN.data[i].distance_q);
                    addToSkyline(d);
                }
            }
        }
    }
    
    public void findSkyline(Data queryD) {
        myQueue queue = new myQueue();
        queue.add(rt.root_ptr);
        while (!queue.isEmpty()) {
            Object o = queue.pop();
            if (o.getClass() == RTDirNode.class) {
                RTDirNode dirN = (RTDirNode) o;
                int n = dirN.get_num();
                for (int i = 0; i < n; i++) {
                    Object succ_o = dirN.entries[i].get_son();
                    if (!isDominatedByResult((Node) succ_o)) {
                        queue.add(succ_o);
                    }
                }
            } else if (o.getClass() == RTDataNode.class) {
                RTDataNode dataN = (RTDataNode) o;
                int n = dataN.get_num();
                for (int i = 0; i < n; i++) {
                    Data d = dataN.data[i];
                    dataN.data[i].distance_q = constants.distanceInMeters(dataN.data[i].location[0], dataN.data[i].location[1], queryD.location[0], queryD.location[1]);
                    addToSkyline(d);
                }
            }
        }
    }
    
    
    public void findSkyline(double lat, double lng) {
        myQueue queue = new myQueue();
        queue.add(rt.root_ptr);
        while (!queue.isEmpty()) {
            Object o = queue.pop();
            if (o.getClass() == RTDirNode.class) {
                RTDirNode dirN = (RTDirNode) o;
                int n = dirN.get_num();
                for (int i = 0; i < n; i++) {
                    Object succ_o = dirN.entries[i].get_son();
                    if (!isDominatedByResult((Node) succ_o)) {
                        queue.add(succ_o);
                    }
                }
            } else if (o.getClass() == RTDataNode.class) {
                RTDataNode dataN = (RTDataNode) o;
                int n = dataN.get_num();
                for (int i = 0; i < n; i++) {
                    Data d = dataN.data[i];
                    dataN.data[i].distance_q = constants.distanceInMeters(dataN.data[i].location[0], dataN.data[i].location[1], lat, lng);
                    addToSkyline(d);
                }
            }
        }
    }


    public void find_candidate() {
        myQueue queue = new myQueue();
        queue.add(rt.root_ptr);
        while (!queue.isEmpty()) {

            Object o = queue.pop();
            if (o.getClass() == RTDirNode.class) {
                RTDirNode dirN = (RTDirNode) o;
                int n = dirN.get_num();
                for (int i = 0; i < n; i++) {
                    Object succ_o = dirN.entries[i].get_son();
                    if (!isDominatedByResult((Node) succ_o)) {
                        queue.add(succ_o);
                    }
                }
            } else if (o.getClass() == RTDataNode.class) {
                RTDataNode dataN = (RTDataNode) o;
                int n = dataN.get_num();
                for (int i = 0; i < n; i++) {
                    Data d = dataN.data[i];
                    d.distance_q = Math.pow(dataN.data[i].location[0], 2) + Math.pow(dataN.data[i].location[1], 2);
                    d.distance_q = Math.sqrt(dataN.data[i].distance_q);
                    addToSkyline(d);
//                    this.sky_hotels.add(d);
                }
            }
        }

    }
}
