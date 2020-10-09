package cs.nmsu.edu.demo.neo4jTools;

import java.util.Comparator;

public class StringComparator implements Comparator<String> {

    @Override
    public int compare(String o1, String o2) {
        return Integer.parseInt(o1) - Integer.parseInt(o2);
    }
}


class IntegerComparator implements Comparator<Integer> {

    @Override
    public int compare(Integer o1, Integer o2) {
        return o1-o2;
    }
}