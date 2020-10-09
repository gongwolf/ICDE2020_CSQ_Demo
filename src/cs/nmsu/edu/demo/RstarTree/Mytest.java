package cs.nmsu.edu.demo.RstarTree;

import java.io.*;

public class Mytest {

    public static void main(String[] args) throws IOException {
        Mytest t = new Mytest();
        t.testTree();

    }

    private void testTree() {
        File fp = new File("data/test.rtr");
        if (fp.exists()) {
            System.out.println("Tree already exits");
            fp.delete();
        }else{
        	System.out.println("Tree doesn't exits");
        }

        RTree rt = new RTree("data/test.rtr", Constants.BLOCKLENGTH, Constants.CACHESIZE, 2);
        Data d1 = new Data();
        d1.data = new float[]{2,1,4,3};
        System.out.println(d1.get_mbr()[3]);
//        d1.setPlaceId("ppid");
        rt.insert(d1);
        System.out.println("==========");

        Data d2 = new Data();
        d2.data = new float[]{4,5,6,7};
//        d2.setPlaceId("ppid2");
        rt.insert(d2);
        System.out.println("==========");

        System.out.println(rt.root_is_data);
        System.out.println(((Node)rt.root_ptr).get_mbr().length);

        PPoint qp = new PPoint(2);
        qp.data=new float[]{3,3};


//        Data d3 = new Data();
//        d3.data = new float[]{1,2,3,4};
//        d3.setPlaceId("ppid3");
//        rt.insert(d3);
//        System.out.println("==========");
//
//
//        Data d4 = new Data();
//        d4.data = new float[]{1,2,3,4};
//        d4.setPlaceId("ppid4");
//        rt.insert(d4);
//        System.out.println("==========");
//
//        Data d5 = new Data();
//        d5.data = new float[]{1,2,3,4};
//        d5.setPlaceId("ppid5");
//        rt.insert(d5);
//        System.out.println("==========");
//
//        Data d6 = new Data();
//        d6.data = new float[]{1,2,3,4};
//        d6.setPlaceId("ppid6");
//        rt.insert(d6);
//        System.out.println("==========");
//
//        Data d7 = new Data();
//        d7.data = new float[]{1,2,3,4};
//        d7.setPlaceId("ppid7");
//        rt.insert(d7);
//        System.out.println("==========");
//
//        Data d8 = new Data();
//        d8.data = new float[]{1,2,3,4};
//        d8.setPlaceId("ppid8");
//        rt.insert(d8);
//        System.out.println("==========");
    }

    public void test1() throws IOException{
        RandomAccessFile fp = new RandomAccessFile("my.rtr", "rw");
        System.out.println(fp.length());
        fp.writeInt(333233);
        System.out.println(fp.length());
        fp.writeInt(99);
        System.out.println(fp.length());

        byte[] d = new byte[]{0, 1, 2, 3};
        fp.write(d, 0, d.length);
        System.out.println(fp.length());

        fp.seek(0);
        System.out.println(fp.readInt());
        System.out.println(fp.readInt());

        System.out.println(fp.readBoolean());
        System.out.println(fp.readBoolean());
        System.out.println(fp.readBoolean());
        System.out.println(fp.readBoolean());

        String a = new String("aa");
        if (a == null) {
            if (1 == 1) {
                System.out.println(
                        "aaaaaa");
            } else {
                System.out.println("bbbbbbbbbbb");
            }
        }

        byte[] header = new byte[1024];
        ByteArrayInputStream byte_in = new ByteArrayInputStream(header);
        DataInputStream in = new DataInputStream(byte_in);
        int dimension = in.readInt();
        int num_of_data = in.readInt();
        int num_of_dnodes = in.readInt();
        int num_of_inodes = in.readInt();
        boolean root_is_data = in.readBoolean();
        int root = in.readInt();
        in.close();
        byte_in.close();
    }
}
