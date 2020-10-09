package cs.nmsu.edu.demo.RstarTree;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Test {

    RTree rt;

    static final int NUMRECTS = 200;

    Test() throws FileNotFoundException, IOException {
//         initialize tree
//        File fp = new File("test.rtr");
//        if (fp.exists()) {
//            System.out.println("Tree already exits");
//            fp.delete();
//        }
//        rt = new RTree("test.rtr", Constants.BLOCKLENGTH, Constants.CACHESIZE, 2);
//        Data d = new Data(2);
////        d.setPlaceId("ChIJSRvZ1yHGwoARcLrfvc-APl4");
//        String placeId = "ChIJSRvZ1yHGwoARcLrfvc-APl4";
//        System.out.println("number of the bytes array "+placeId.getBytes().length);
//        placeId = "ChIJaUBtetO-woARqyrV4ASPECg";
//        System.out.println(placeId.length());
//        float latitude = (float) 34.05240960;
//        float longitude = (float) -118.30071400;
//        d.data = new float[]{latitude,latitude,longitude,longitude};
//        d.setPlaceId(placeId);
//        System.out.println("qqqq"+d.getPlaceId().getBytes().length);
//        rt.insert(d);
//        rt.delete();
//        rt = new RTree("test.rtr", Constants.CACHESIZE);
//        System.out.println(rt.file.fp.length());
//        Data d = new Data(2);
//        String placeId = "ChIJGyYzkgrIwoARk5yDz3-8T88";
//        float latitude = (float) 34.01735550;
//        float longitude = (float) -118.27840490;
//        d.data = new float[]{latitude, latitude, longitude, longitude};
//        d.setPlaceId(placeId);
//        rt.insert(d);
//        System.out.println(rt.file.fp.length());
////        ((RTDirNode)rt.root_ptr).print();
//        rt.delete();
//        SortMbr m1 = new SortMbr();
//        SortMbr m2 = new SortMbr();
//        m1.dimension = m2.dimension = 1;
//        m1.index = m2.index = 0;
    }

    public static void main(String argv[]) throws FileNotFoundException, IOException {
        Test t = new Test();
//        t.test1();
//        t.test2();

//        SortedLinList res = new SortedLinList();
//        rectangle r = new rectangle(0);
//        r.LX = 10;
//        r.UX = 75;
//        r.LY = 40;
//        r.UY = 45;
//
//        double dist[] = new double[2];
//        dist[0] = (double)0;
//        dist[1] = (double)50;
//        t.rt.constraints_query(r, dist, /*relationSet.p2((byte)5)*/(short)255, /*(short)(relationSet.p2((byte)5)*/ relationSet.p2((byte)7), res);
    }

//    private void test1() {
//        //         initialize tree
//        File fp = new File("./data/test.rtr");
//        if (fp.exists()) {
//            System.out.println("Tree already exits");
//            fp.delete();
//        }
//
//        String path = "sample";
//        rt = new RTree("test.rtr", Constants.BLOCKLENGTH, Constants.CACHESIZE, 2);
//        BufferedReader br;
////        Data d = new Data(2);
////        d.data = new float[]{1, 1, 1, 1};
////        rt.insert(d);
//
//        try {
//            br = new BufferedReader(new FileReader(path));
//            String line;
//            String placeId = null;
//            String placeName = null;
//            String lat = null;
//            String log = null;
//            while ((line = br.readLine()) != null) {
//                if (!line.equals("=======================================================================")) {
//                    if (line.startsWith("placeId")) {
//                        placeId = line.split(":")[1].trim();
//                    } else if (line.startsWith("name")) {
//                        placeName = line.split(":")[1].trim();
//                    } else if (line.trim().startsWith("locations")) {
//                        lat = line.trim().split(":")[1].split(",")[0].trim();
//                        log = line.trim().split(":")[1].split(",")[1].trim();
//                    }
//                } else {
//                    System.out.println(placeId + ":" + placeName + " --- " + lat + "#" + log);
//                    Data d = new Data(2);
//                    d.setPlaceId(placeId);
//                    float l1 = Float.valueOf(lat);
//                    float l2 = Float.valueOf(log);
//                    d.data = new float[]{l1, l1, l2, l2};
//                    rt.insert(d);
//                }
//            }
//            System.out.println(placeId + ":" + placeName + " --- " + lat + "#" + log);
//            Data d = new Data(2);
//            d.setPlaceId(placeId);
//            float l1 = Float.valueOf(lat);
//            float l2 = Float.valueOf(log);
//            d.data = new float[]{l1, l1, l2, l2};
//            rt.insert(d);
//        } catch (FileNotFoundException ex) {
//            Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (IOException ex) {
//            Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        rt.delete();
//    }

    private void test2() {
        System.out.println("==========================================================");
        rt = new RTree("./data/POI_Tree.rtr", Constants.CACHESIZE);
        float[] mbr = ((Node) rt.root_ptr).get_mbr();
        float x1, x2, y1, y2;
        x1 = mbr[0];
        x2 = mbr[1];
        y1 = mbr[2];
        y2 = mbr[3];
        System.out.println("(" + x1 + "," + y1 + "),(" + x2 + "," + y2 + ")");
        System.out.println(rt.root_ptr.getClass());
        if (rt.root_ptr.getClass().getName().endsWith("RTDirNode")) {
            System.out.println("Search in sub Dir");
            RTDirNode DirNode = (RTDirNode) rt.root_ptr;
            int n = DirNode.get_num();
            for (int i = 0; i < n; i++) {
                RTNode succ = DirNode.entries[i].get_son();
                mbr = ((Node) succ).get_mbr();
                RTDataNode q = ((RTDataNode) succ);
                x1 = mbr[0];
                x2 = mbr[1];
                y1 = mbr[2];
                y2 = mbr[3];
                System.out.println("   (" + x1 + "," + y1 + "),(" + x2 + "," + y2 + ")" + "@@ " + q.get_num());
                for (Data d : q.data) {
                    if (d != null) {
                        mbr = d.get_mbr();
                        x1 = mbr[0];
                        x2 = mbr[1];
                        y1 = mbr[2];
                        y2 = mbr[3];
                        System.out.println("         (" + x1 + "," + y1 + "),(" + x2 + "," + y2 + ")" + "@@ " + d.getPlaceId());
                    }
                }
            }
        };
    }
}
