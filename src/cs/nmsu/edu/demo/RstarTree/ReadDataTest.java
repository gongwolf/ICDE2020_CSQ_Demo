package cs.nmsu.edu.demo.RstarTree;


public class ReadDataTest {
    public static void main(String args[]) {
        ReadDataTest rdt = new ReadDataTest();
        rdt.test1();

    }

    private RTree rt;

    void test1() {
        rt = new RTree("data/POI_Tree.rtr", Constants.CACHESIZE);
        float l1 = (float) 40.71510770;
        float l2 = (float) -74.04676070;
        float[] p = new float[] { l1, l2 };
        rt.point_query(p);
    }

}
