package cs.nmsu.edu.demo.neo4jTools;

import javafx.util.Pair;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.*;

public class mimicBusLine {
    //direction
    final int righttop = 1;
    final int rightbuttom = 2;
    final int leftbuttom = 3;
    final int lefttop = 4;
    private final int numofNode;
    String DBBase = "/home/gqxwolf/mydata/DemoProject/busline";
    String EdgesPath = DBBase + "SegInfo.txt";
    String NodePath = DBBase + "NodeInfo.txt";
    double movement;
    double samnode_t;

    HashMap<Integer, node> Nodes = new HashMap<>();
    HashMap<Pair<Integer, Integer>, String[]> Edges = new HashMap<>();

    int max_node_id;

    public mimicBusLine(int numofNode, double movement, double samnode_t) {
        this.DBBase = this.DBBase + "_" + numofNode + "_"+samnode_t+"/data/";
        this.EdgesPath = DBBase + "SegInfo.txt";
        this.NodePath = DBBase + "NodeInfo.txt";
        this.numofNode = numofNode;
        this.max_node_id = 0;
        this.samnode_t = samnode_t;
        this.movement = movement;


    }

    public static void main(String args[]) {
        int graphsize = 10000;
        mimicBusLine m = new mimicBusLine(graphsize, 10, 2);
        m.generateGraph(true);
        m.readFromDist();
        while (m.findComponent(m.Nodes).size() != graphsize) {
            m.connectedComponent();
        }
    }

    public void generateGraph(boolean deleteBefore) {
        if (deleteBefore) {
            File dataF = new File(DBBase);
            try {
                FileUtils.deleteDirectory(dataF);
                dataF.mkdirs();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        while (Nodes.size() < this.numofNode) {
            generateBusline();
        }
        writeNodeToDisk();
        writeEdgeToDisk();
    }

    public void generateBusline() {

        int num_bus_inLine;


        if (numofNode - Nodes.size() < 20) {
            num_bus_inLine = numofNode - Nodes.size();
        } else {
            num_bus_inLine = getRandomNumberInRange_int(10, 20);
        }
        System.out.println(num_bus_inLine);

        int[] bus_ids = new int[num_bus_inLine];

        for (int i = 0; i < num_bus_inLine; i++) {
            node new_n = new node();

//            System.out.print(i);
            if (i == 0) {
                new_n.latitude = getRandomNumberInRange(0, 360);
                new_n.longitude = getRandomNumberInRange(0, 360);
////                System.out.println(new_n.latitude + " " + new_n.longitude);
            } else if (i == 1) {


                double p_l = Nodes.get(bus_ids[i - 1]).latitude;
                double p_g = Nodes.get(bus_ids[i - 1]).longitude;

                int next_direction = getRandomNumberInRange_int(1, 4);
                switch (next_direction) {
                    case righttop:
                        new_n.latitude = getRandomNumberInRange(p_l, p_l + movement);
                        new_n.longitude = getRandomNumberInRange(p_g, p_g + movement);
                        break;
                    case rightbuttom:
                        new_n.latitude = getRandomNumberInRange(p_l, p_l + movement);
                        new_n.longitude = getRandomNumberInRange(p_g - movement, p_g);
                        break;
                    case leftbuttom:
                        new_n.latitude = getRandomNumberInRange(p_l - movement, p_l);
                        new_n.longitude = getRandomNumberInRange(p_g - movement, p_g);
                        break;
                    case lefttop:
                        new_n.latitude = getRandomNumberInRange(p_l - movement, p_l);
                        new_n.longitude = getRandomNumberInRange(p_g, p_g + movement);
                        break;
                }


                while (Math.sqrt(Math.pow(new_n.latitude - p_l, 2) + Math.pow(new_n.longitude - p_g, 2)) < samnode_t ||
                        (new_n.latitude > 360 || new_n.latitude < 0) ||
                        (new_n.longitude > 360 || new_n.longitude < 0)) {
                    next_direction = getRandomNumberInRange_int(1, 4);
                    switch (next_direction) {
                        case righttop:
                            new_n.latitude = getRandomNumberInRange(p_l, p_l + movement);
                            new_n.longitude = getRandomNumberInRange(p_g, p_g + movement);
                            break;
                        case rightbuttom:
                            new_n.latitude = getRandomNumberInRange(p_l, p_l + movement);
                            new_n.longitude = getRandomNumberInRange(p_g - movement, p_g);
                            break;
                        case leftbuttom:
                            new_n.latitude = getRandomNumberInRange(p_l - movement, p_l);
                            new_n.longitude = getRandomNumberInRange(p_g - movement, p_g);
                            break;
                        case lefttop:
                            new_n.latitude = getRandomNumberInRange(p_l - movement, p_l);
                            new_n.longitude = getRandomNumberInRange(p_g, p_g + movement);
                            break;
                    }
                }

            } else {
                //find the direction can not go
                int not_go_direction = 0;
                if (Nodes.get(bus_ids[i - 1]).latitude - Nodes.get(bus_ids[i - 2]).latitude >= 0 && Nodes.get(bus_ids[i - 1]).longitude - Nodes.get(bus_ids[i - 2]).longitude >= 0) {
                    not_go_direction = this.leftbuttom;
                } else if (Nodes.get(bus_ids[i - 1]).latitude - Nodes.get(bus_ids[i - 2]).latitude >= 0 && Nodes.get(bus_ids[i - 1]).longitude - Nodes.get(bus_ids[i - 2]).longitude <= 0) {
                    not_go_direction = this.lefttop;
                } else if (Nodes.get(bus_ids[i - 1]).latitude - Nodes.get(bus_ids[i - 2]).latitude <= 0 && Nodes.get(bus_ids[i - 1]).longitude - Nodes.get(bus_ids[i - 2]).longitude <= 0) {
                    not_go_direction = this.righttop;
                } else {
                    not_go_direction = this.rightbuttom;
                }

                int next_direction = getRandomDirection(not_go_direction);

                double p_l = Nodes.get(bus_ids[i - 1]).latitude;
                double p_g = Nodes.get(bus_ids[i - 1]).longitude;

                switch (next_direction) {
                    case righttop:
                        new_n.latitude = getRandomNumberInRange(p_l, p_l + movement);
                        new_n.longitude = getRandomNumberInRange(p_g, p_g + movement);
                        break;
                    case rightbuttom:
                        new_n.latitude = getRandomNumberInRange(p_l, p_l + movement);
                        new_n.longitude = getRandomNumberInRange(p_g - movement, p_g);
                        break;
                    case leftbuttom:
                        new_n.latitude = getRandomNumberInRange(p_l - movement, p_l);
                        new_n.longitude = getRandomNumberInRange(p_g - movement, p_g);
                        break;
                    case lefttop:
                        new_n.latitude = getRandomNumberInRange(p_l - movement, p_l);
                        new_n.longitude = getRandomNumberInRange(p_g, p_g + movement);
                        break;
                }

                int tried_times = 0;
                while (Math.sqrt(Math.pow(new_n.latitude - p_l, 2) + Math.pow(new_n.longitude - p_g, 2)) < samnode_t ||
                        Math.sqrt(Math.pow(new_n.latitude - Nodes.get(bus_ids[i - 2]).latitude, 2) + Math.pow(new_n.longitude - Nodes.get(bus_ids[i - 2]).longitude, 2)) < samnode_t ||
                        getAngleInDegree(new double[]{p_l - Nodes.get(bus_ids[i - 2]).latitude, p_g - Nodes.get(bus_ids[i - 2]).longitude}, new double[]{new_n.latitude - p_l, new_n.longitude - p_g}) > 80 ||
                        (new_n.latitude > 360 || new_n.latitude < 0) ||
                        (new_n.longitude > 360 || new_n.longitude < 0)) {
//                    boolean f1 = Math.sqrt(Math.pow(new_n.latitude - p_l, 2) + Math.pow(new_n.longitude - p_g, 2)) < samnode_t;
//                    boolean f2 = Math.sqrt(Math.pow(new_n.latitude - Nodes.get(bus_ids[i - 2]).latitude, 2) + Math.pow(new_n.longitude - Nodes.get(bus_ids[i - 2]).longitude, 2)) < samnode_t;
//                    boolean f3 = getAngleInDegree(new double[]{p_l - Nodes.get(bus_ids[i - 2]).latitude, p_g - Nodes.get(bus_ids[i - 2]).longitude}, new double[]{new_n.latitude - p_l, new_n.longitude - p_g}) > 90;
//                    boolean f4 = new_n.latitude > 360 || new_n.latitude < 0;
//                    boolean f5 = new_n.longitude > 360 || new_n.longitude < 0;
//                    System.out.println(f1+" "+f2+" "+f3+" "+f4+" "+f5+" ");

                    if (tried_times == 200) {
                        break;
                    }

                    next_direction = getRandomDirection(not_go_direction);
                    switch (next_direction) {
                        case righttop:
                            new_n.latitude = getRandomNumberInRange(p_l, p_l + movement);
                            new_n.longitude = getRandomNumberInRange(p_g, p_g + movement);
                            break;
                        case rightbuttom:
                            new_n.latitude = getRandomNumberInRange(p_l, p_l + movement);
                            new_n.longitude = getRandomNumberInRange(p_g - movement, p_g);
                            break;
                        case leftbuttom:
                            new_n.latitude = getRandomNumberInRange(p_l - movement, p_l);
                            new_n.longitude = getRandomNumberInRange(p_g - movement, p_g);
                            break;
                        case lefttop:
                            new_n.latitude = getRandomNumberInRange(p_l - movement, p_l);
                            new_n.longitude = getRandomNumberInRange(p_g, p_g + movement);
                            break;
                    }

                    tried_times++;
                }


                if (tried_times == 200) {
                    while (Math.sqrt(Math.pow(new_n.latitude - p_l, 2) + Math.pow(new_n.longitude - p_g, 2)) < samnode_t ||
                            Math.sqrt(Math.pow(new_n.latitude - Nodes.get(bus_ids[i - 2]).latitude, 2) + Math.pow(new_n.longitude - Nodes.get(bus_ids[i - 2]).longitude, 2)) < samnode_t ||
                            (new_n.latitude > 360 || new_n.latitude < 0) ||
                            (new_n.longitude > 360 || new_n.longitude < 0)) {

                        next_direction = getRandomDirection(not_go_direction);
                        switch (next_direction) {
                            case righttop:
                                new_n.latitude = getRandomNumberInRange(p_l, p_l + movement);
                                new_n.longitude = getRandomNumberInRange(p_g, p_g + movement);
                                break;
                            case rightbuttom:
                                new_n.latitude = getRandomNumberInRange(p_l, p_l + movement);
                                new_n.longitude = getRandomNumberInRange(p_g - movement, p_g);
                                break;
                            case leftbuttom:
                                new_n.latitude = getRandomNumberInRange(p_l - movement, p_l);
                                new_n.longitude = getRandomNumberInRange(p_g - movement, p_g);
                                break;
                            case lefttop:
                                new_n.latitude = getRandomNumberInRange(p_l - movement, p_l);
                                new_n.longitude = getRandomNumberInRange(p_g, p_g + movement);
                                break;
                        }
                    }
                }


            }

            int newid;
            if ((newid = hasNodes(new_n)) == -1) {
                newid = max_node_id;
                max_node_id++;
                this.Nodes.put(newid, new_n);
            } else {
                new_n.latitude = this.Nodes.get(newid).latitude;
                new_n.longitude = this.Nodes.get(newid).longitude;
            }
            new_n.id = newid;
            bus_ids[i] = newid;


            if (i > 0) {
                double p_l = Nodes.get(bus_ids[i - 1]).latitude;
                double p_g = Nodes.get(bus_ids[i - 1]).longitude;
                double dist = Math.sqrt(Math.pow(new_n.latitude - p_l, 2) + Math.pow(new_n.longitude - p_g, 2));

                System.out.println(newid + "  " + bus_ids[i - 1] + "  " + new_n.latitude + " " + new_n.longitude + " " + p_l + " " + p_g + " " + dist);

                String[] costs = new String[3];
                for (int j = 0; j < 3; j++) {
//                    costs[j] = String.valueOf(getRandomNumberInRange(0, 5));
                    costs[j] = String.valueOf(getGaussian(dist, dist));
                }

                System.out.println(bus_ids[i - 1] + "," + new_n.id + " " + costs[0] + " " + costs[1] + " " + costs[2]);

                Edges.put(new Pair<>(bus_ids[i - 1], new_n.id), costs);
            }
        }


    }

    private int hasNodes(node node) {
        int flag = -1;
        for (Map.Entry<Integer, node> n : this.Nodes.entrySet()) {
            if (Math.sqrt(Math.pow(n.getValue().latitude - node.latitude, 2) + Math.pow(n.getValue().longitude - node.longitude, 2)) < samnode_t) {
                flag = n.getKey();
//                System.out.println("find a node " + flag);
            }
        }
        return flag;
    }

    private double getRandomNumberInRange(double min, double max) {

        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }

        Random r = new Random();
        return r.nextDouble() * (max - min) + min;
    }


    private int getRandomNumberInRange_int(int min, int max) {

        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }

        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }

    private int getRandomDirection(int not_go_dirct) {
        int direction = getRandomNumberInRange_int(1, 4);


        while (direction == not_go_dirct) {
            direction = getRandomNumberInRange_int(1, 4);
        }
        return direction;
    }

    private double getGaussian(double mean, double sd) {
        Random r = new Random();
        double value = r.nextGaussian() * sd + mean;

        while (value <= 0) {
            value = r.nextGaussian() * sd + mean;
        }

        return value;
    }


    private void writeNodeToDisk() {
        try (FileWriter fw = new FileWriter(NodePath, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            System.out.println(NodePath);
            TreeMap<Integer, node> tm = new TreeMap<Integer, node>(new IntegerComparator());
            tm.putAll(Nodes);
            for (Map.Entry<Integer, node> node : tm.entrySet()) {
                StringBuffer sb = new StringBuffer();
                Integer nodeId = node.getKey();
                sb.append(nodeId).append(" ");
                sb.append(node.getValue().latitude).append(" ");
                sb.append(node.getValue().longitude).append(" ");
                out.println(sb.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void writeEdgeToDisk() {
        try (FileWriter fw = new FileWriter(EdgesPath, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            for (Map.Entry<Pair<Integer, Integer>, String[]> node : this.Edges.entrySet()) {
//                System.out.println(EdgesPath);
                StringBuffer sb = new StringBuffer();
                int snodeId = node.getKey().getKey();
                int enodeId = node.getKey().getValue();
                sb.append(snodeId).append(" ");
                sb.append(enodeId).append(" ");
                for (String cost : node.getValue()) {
                    sb.append(cost).append(" ");
                }
                out.println(sb.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void connectedComponent() {
        ArrayList<HashSet<Integer>> component_list = new ArrayList<>();
        System.out.println(this.Nodes.size() + " " + this.Edges.size());
        HashMap<Integer, node> reamming_nodes = new HashMap<>(this.Nodes);

        while (reamming_nodes.size() > 1) {
            HashSet<Integer> nodesets = findComponent(reamming_nodes);
            System.out.println(nodesets.size());

            component_list.add(nodesets);

            for (int n : nodesets) {
                reamming_nodes.remove(n);
            }
        }

        for (int i = 0; i < component_list.size() - 1; i++) {
//            HashSet<Integer> nodes_ets = component_list.get(i);
//            HashSet<Integer> next_sets = component_list.get(i + 1);
//            createConnectedEdge(nodes_ets, next_sets);
            HashSet<Integer> nodes_ets = component_list.get(i);
            createConnectedEdge(nodes_ets, i, component_list);

        }

        if (reamming_nodes.size() == 1) {
            for (Map.Entry<Integer, node> n : reamming_nodes.entrySet()) {
                int sid = n.getKey();
                int eid = getNN(sid);
                double dist = Math.sqrt(Math.pow(this.Nodes.get(sid).latitude - this.Nodes.get(eid).latitude, 2) + Math.pow(this.Nodes.get(sid).longitude - this.Nodes.get(eid).longitude, 2));

                String[] costs = new String[3];
                for (int j = 0; j < 3; j++) {
//                    costs[j] = String.valueOf(getRandomNumberInRange(0, 5));
                    costs[j] = String.valueOf(getGaussian(dist, dist));
                }
                this.Edges.put(new Pair<>(sid, eid), costs);
            }

        }


        File f = new File(this.EdgesPath);
        if (f.exists()) {
            f.delete();
        }

        writeEdgeToDisk();

    }

    private void createConnectedEdge(HashSet<Integer> nodes_ets, int set_i, ArrayList<HashSet<Integer>> component_list) {
        double min_dist = Double.MAX_VALUE;
        int component_id = -1;
        for (int i = 0; i < component_list.size(); i++) {
            if (i != set_i) {
                double dist = nearest_dist(nodes_ets, component_list.get(i));
                if (dist < min_dist) {
                    component_id = i;
                    min_dist = dist;
                }
            }
        }

//        System.out.println(set_i+"------"+component_id);


        int[] ids = nearest_id_pair(nodes_ets, component_list.get(component_id));
        int sid = ids[0];
        int did = ids[1];
        double dist = Math.sqrt(Math.pow(this.Nodes.get(sid).latitude - this.Nodes.get(did).latitude, 2) + Math.pow(this.Nodes.get(sid).longitude - this.Nodes.get(did).longitude, 2));

        String[] costs = new String[3];
        for (int j = 0; j < 3; j++) {
//                    costs[j] = String.valueOf(getRandomNumberInRange(0, 5));
            costs[j] = String.valueOf(getGaussian(dist, dist));
        }
        this.Edges.put(new Pair<>(sid, did), costs);


    }

    private double nearest_dist(HashSet<Integer> nodes_ets, HashSet<Integer> next_sets) {
        double min_dist = Double.MAX_VALUE;
        for (int c_n : nodes_ets) {
            double s_latitude = this.Nodes.get(c_n).latitude;
            double s_longitude = this.Nodes.get(c_n).longitude;

            for (int n : next_sets) {
                double n_latitude = this.Nodes.get(n).latitude;
                double n_longitude = this.Nodes.get(n).longitude;
                double dist = Math.sqrt(Math.pow(s_latitude - n_latitude, 2) + Math.pow(s_longitude - n_longitude, 2));
                if (dist < min_dist) {
                    min_dist = dist;
                }
            }
        }

        return min_dist;
    }


    private int[] nearest_id_pair(HashSet<Integer> nodes_ets, HashSet<Integer> next_sets) {
        int result[] = new int[2];
        double min_dist = Double.MAX_VALUE;
        for (int c_n : nodes_ets) {
            double s_latitude = this.Nodes.get(c_n).latitude;
            double s_longitude = this.Nodes.get(c_n).longitude;

            for (int n : next_sets) {
                double n_latitude = this.Nodes.get(n).latitude;
                double n_longitude = this.Nodes.get(n).longitude;
                double dist = Math.sqrt(Math.pow(s_latitude - n_latitude, 2) + Math.pow(s_longitude - n_longitude, 2));
                if (dist < min_dist) {
                    min_dist = dist;
                    result[0] = c_n;
                    result[1] = n;
                }
            }
        }

        return result;
    }

    private int getNN(int sid) {
        double min_dist = Double.MAX_VALUE;
        int result = 0;
        double s_latitude = this.Nodes.get(sid).latitude;
        double s_longitude = this.Nodes.get(sid).longitude;
        for (int n : this.Nodes.keySet()) {
            if (n != sid) {
                double n_latitude = this.Nodes.get(n).latitude;
                double n_longitude = this.Nodes.get(n).longitude;
                double dist = Math.sqrt(Math.pow(s_latitude - n_latitude, 2) + Math.pow(s_longitude - n_longitude, 2));
                if (dist < min_dist) {
                    min_dist = dist;
                    result = n;
                }
            }
        }

        return result;
    }

    private int getNNById(int sid, HashSet<Integer> next_sets) {
        double min_dist = Double.MAX_VALUE;
        int result = 0;
        double s_latitude = this.Nodes.get(sid).latitude;
        double s_longitude = this.Nodes.get(sid).longitude;
        for (int n : next_sets) {
            double n_latitude = this.Nodes.get(n).latitude;
            double n_longitude = this.Nodes.get(n).longitude;
            double dist = Math.sqrt(Math.pow(s_latitude - n_latitude, 2) + Math.pow(s_longitude - n_longitude, 2));
            if (dist < min_dist) {
                min_dist = dist;
                result = n;
            }
        }

        return result;
    }

    //Find the connected component in the given remaining nodes set
    private HashSet<Integer> findComponent(HashMap<Integer, node> reamming_nodes) {
        HashSet<Integer> node_sets = new HashSet<>();
        Queue<Integer> queue = new LinkedList<>();
        int startNode = getRandomeStartingNode(reamming_nodes);

        queue.add(startNode);

        while (!queue.isEmpty()) {
            int v = queue.poll();
            node_sets.add(v);
//            System.out.println(v);
            HashSet<Integer> n_nodes = getNeigbor(v);
            for (int n : n_nodes) {
                if (!node_sets.contains(n) && !queue.contains(n)) {
                    queue.add(n);
                }
            }
        }

        return node_sets;


    }

    private HashSet<Integer> getNeigbor(int v) {
        HashSet<Integer> result = new HashSet<>();
        for (Map.Entry<Pair<Integer, Integer>, String[]> e : this.Edges.entrySet()) {
            if (e.getKey().getKey() == v) {
                result.add(e.getKey().getValue());
            } else if (e.getKey().getValue() == v) {
                result.add(e.getKey().getKey());
            }
        }
        return result;
    }

    private int getRandomeStartingNode(HashMap<Integer, node> reamming_nodes) {
        Set<Integer> keyList = reamming_nodes.keySet();
        int randomIndex = getRandomNumberInRange_int(0, keyList.size() - 1);

        int i = 0;
        for (Integer key : keyList) {
            if (i == randomIndex) {
                return key;
            }

            i++;
        }
        return -1;
    }

    private void readFromDist() {
        try {

            File f = new File(this.NodePath);
            BufferedReader b = new BufferedReader(new FileReader(f));
            String readLine = "";
//            System.out.println("Reading file using Buffered Reader");

            while ((readLine = b.readLine()) != null) {
                node n = new node();
                n.id = Integer.parseInt(readLine.split(" ")[0]);
                n.latitude = Double.parseDouble(readLine.split(" ")[1]);
                n.longitude = Double.parseDouble(readLine.split(" ")[2]);
                this.Nodes.put(n.id, n);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


        try {

            File f = new File(this.EdgesPath);
            BufferedReader b = new BufferedReader(new FileReader(f));
            String readLine = "";
//            System.out.println("Reading file using Buffered Reader");

            while ((readLine = b.readLine()) != null) {
                int s_id = Integer.parseInt(readLine.split(" ")[0]);
                int e_id = Integer.parseInt(readLine.split(" ")[1]);

                String costs[] = new String[3];
                costs[0] = readLine.split(" ")[2];
                costs[1] = readLine.split(" ")[3];
                costs[2] = readLine.split(" ")[4];


                this.Edges.put(new Pair<>(s_id, e_id), costs);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public double getAngleInDegree(double[] x, double[] y) {
        double r1 = x[0] * y[0] + x[1] * y[1];
        double r2 = Math.sqrt(x[0] * x[0] + x[1] * x[1]);
        double r3 = Math.sqrt(y[0] * y[0] + y[1] * y[1]);
//        System.out.println(r1/(r2*r3));
//        System.out.println(Math.toDegrees(Math.acos(r1/(r2*r3))));
        return Math.toDegrees(Math.acos(r1 / (r2 * r3)));
    }
}

class node {
    int id;
    double latitude, longitude;
}
