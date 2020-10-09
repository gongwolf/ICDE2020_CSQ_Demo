package cs.nmsu.edu.demo.neo4jTools;

import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseBuilder;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.io.fs.FileUtils;
import org.neo4j.jmx.JmxUtils;

import javax.management.ObjectName;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public class connector {
	String home_folder = System.getProperty("user.home");
    public static GraphDatabaseService graphDB;
    public ArrayList<String> propertiesName = new ArrayList<>();
    public String DB_PATH = home_folder+"/neo4j334/testdb1000000_4/databases/graph.db";
    String conFile = home_folder+"/neo4j334/conf/neo4j.conf";


    public connector(String DB_PATH) {
        this.DB_PATH = DB_PATH;
    }

    public connector() {
    }

    private static void registerShutdownHook(final GraphDatabaseService graphDb) {
        // Registers a shutdown hook for the Neo4j instance so that it
        // // shuts down nicely when the VM exits (even if you "Ctrl-C" the
        // // running application).
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                graphDb.shutdown();
            }
        });
    }

    public static void main(String args[]) {
        connector n = new connector();
        n.startBD_without_getProperties();
//        n.test();
//        n.clean();
        System.out.println(n.getNumberofNodes());
        System.out.println(n.getNumberofEdges());

        n.shutdownDB();
    }

    public long getNumberofEdges() {
        long result = 0;
        try (Transaction tx = this.graphDB.beginTx()) {
            ResourceIterable<Relationship> r = this.graphDB.getAllRelationships();
            tx.success();
            result = r.stream().count();
        }
        return result;
    }

    public ArrayList<Relationship> getOutgoutingEdges(long Node_id) {
        ArrayList<Relationship> results = new ArrayList<>();
        try (Transaction tx = graphDB.beginTx()) {
            Iterable<Relationship> rels = graphDB.getNodeById(Node_id).getRelationships(Line.Linked, Direction.OUTGOING);
            Iterator<Relationship> rel_Iter = rels.iterator();
            while (rel_Iter.hasNext()) {
                Relationship rel = rel_Iter.next();
                results.add(rel);
            }
            tx.success();
        }

        return results;
    }

    public void getPropertiesName() {
        propertiesName.clear();
        try (Transaction tx = graphDB.beginTx()) {
        	
        	ResourceIterable<Node> nodes_iterable = graphDB.getAllNodes();
        	ResourceIterator<Node> nodes_iter = nodes_iterable.iterator();
        	
        	while(nodes_iter.hasNext()) {
        		Node n = nodes_iter.next();
//        		System.out.println(n);
        		Iterable<Relationship> rels = graphDB.getNodeById(n.getId()).getRelationships(Line.Linked, Direction.BOTH);
        		if (rels.iterator().hasNext()) {
                    Relationship rel = rels.iterator().next();
                    Map<String, Object> pnamemap = rel.getAllProperties();
                    for (Map.Entry<String, Object> entry : pnamemap.entrySet()) {
                        propertiesName.add(entry.getKey());
//                        System.out.println(entry.getKey());
                    }
                    break;
                }
        	}
        	
//            System.out.println(propertiesName.size());
            tx.success();
        }
    }

    public void startDB() {
        this.graphDB = null;
        //this.graphDB = new GraphDatabaseFactory().newEmbeddedDatabase(new File(DB_PATH));
//        System.out.println(this.DB_PATH);
        GraphDatabaseBuilder builder = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(new File(this.DB_PATH));
//        builder.loadPropertiesFromFile(conFile)
//        builder.setConfig(GraphDatabaseSettings.mapped_memory_page_size, "2k")
        builder.setConfig(GraphDatabaseSettings.pagecache_memory, "8G");

        this.graphDB = builder.newGraphDatabase();


        //this.graphDB = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(new File(DB_PATH)).loadPropertiesFromFile("/home/gqxwolf/neo4j/conf/neo4j.properties").newGraphDatabase();


        registerShutdownHook(this.graphDB);
        getPropertiesName();
//        if (graphDB == null) {
//            System.out.println("Initialize fault");
//        } else {
//            System.out.println("Initialize success");
//        }
    }

    public void startBD_without_getProperties() {
        GraphDatabaseBuilder builder = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(new File(this.DB_PATH));
        builder.setConfig(GraphDatabaseSettings.pagecache_memory, "8G");
        this.graphDB = builder.newGraphDatabase();
        registerShutdownHook(this.graphDB);

    }

    public void shutdownDB() {
        //System.out.println("Shut downing....");
        this.graphDB.shutdown();
        this.graphDB=null;
    }

    public void clean() {
        startDB();
        if (this.graphDB != null) {
            System.out.println("Connect the db successfully");
        }
        try (Transaction tx = this.graphDB.beginTx()) {
            this.graphDB.execute("match (n) detach delete n;");
            tx.success();
        }
        shutdownDB();

        //System.out.println("Clean data base success");
    }

    public void test() {
        startDB();
        if (this.graphDB != null) {
            System.out.println("Connect the db successfully");
        }
        try (Transaction tx = this.graphDB.beginTx()) {

            Node javaNode = this.graphDB.createNode(BNode.BusNode);
            javaNode.setProperty("TutorialID", "JAVA001");
            javaNode.setProperty("Title", "Learn Java");
            javaNode.setProperty("NoOfChapters", "25");
            javaNode.setProperty("Status", "Completed");

            Node scalaNode = graphDB.createNode(BNode.BusNode);
            scalaNode.setProperty("TutorialID", "SCALA001");
            scalaNode.setProperty("Title", "Learn Scala");
            scalaNode.setProperty("NoOfChapters", "20");
            scalaNode.setProperty("Status", "Completed");

            Relationship relationship = javaNode.createRelationshipTo(scalaNode, Line.Linked);
            relationship.setProperty("Id", "1234");
            relationship.setProperty("OOPS", "YES");
            relationship.setProperty("FP", "YES");
            tx.success();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Done successfully");
        shutdownDB();

    }

    public void deleteDB() {
        try {
            FileUtils.deleteRecursively(new File(DB_PATH));
            System.out.println("delete    "+DB_PATH);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public GraphDatabaseService getDBObject() {
        return this.graphDB;
    }

    public long getNumberofNodes() {
//        startDB();
        long result = 0;
        try (Transaction tx = this.graphDB.beginTx()) {
            ResourceIterable<Node> r = this.graphDB.getAllNodes();
            tx.success();
            result = r.stream().count();
        }
//        System.out.println("dbms.memory.pagecache.size ~~~~  " + Long.valueOf((String) getFromManagementBean("Configuration", "dbms.memory.pagecache.size")) / 1024 + "k");
//        System.out.println("dbms.memory.pagecache.size ~~~~  " + Long.valueOf((String) getFromManagementBean("Configuration", "unsupported.dbms.memory.pagecache.pagesize")) / 1024 + "k");
//        System.out.println("NumberOfCommittedTransactions ~~~~  " + (long) getFromManagementBean("Transactions", "NumberOfCommittedTransactions"));
//        System.out.println("TotalStoreSize ~~~~  " + (long) getFromManagementBean("Store sizes", "TotalStoreSize") / 1024 + "k");
//        System.out.println("StringStoreSize ~~~~  " + (long) getFromManagementBean("Store sizes", "StringStoreSize") / 1024 + "k");
//        System.out.println("TransactionLogsSize ~~~~  " + (long) getFromManagementBean("Store sizes", "TransactionLogsSize") / 1024 + "k");


//        System.out.println("~~~~  " + Long.valueOf((String)getFromManagementBean("Configuration", "dbms.memory.heap.initial_size"))/1024);
//        System.out.println("~~~~  " + Long.valueOf((String)getFromManagementBean("Configuration", "dbms.memory.heap.max_size"))/1024);


//        for (Map.Entry e : Neo4jManager.get().getConfiguration().entrySet()) {
//            System.out.println(e.getValue()+"   "+e.getKey());
//        }


        return result;
    }

    private Object getFromManagementBean(String Object, String Attribuite) {
        ObjectName objectName = JmxUtils.getObjectName(this.graphDB, Object);
        Object value = JmxUtils.getAttribute(objectName, Attribuite);
        return value;
    }

    public void restartDB()
    {
        this.shutdownDB();
        this.startBD_without_getProperties();
    }


}
