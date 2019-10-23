import java.io.*;
import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import edu.uci.ics.jung.algorithms.importance.BetweennessCentrality;
import edu.uci.ics.jung.algorithms.importance.Ranking;
import edu.uci.ics.jung.algorithms.scoring.ClosenessCentrality;
import edu.uci.ics.jung.algorithms.scoring.DistanceCentralityScorer;
import edu.uci.ics.jung.graph.Hypergraph;
import edu.uci.ics.jung.graph.util.EdgeType;

public class NetworkHandler {
    private Network<Integer, Integer> net;
    private BetweennessCentrality<Integer,Integer> bc;
    private String path; //path to input folder
    private Format form;
    private static long NETWORK_WINDOWS = 36;
    private int numErrNU = 0;

    public NetworkHandler(String d, String p) {
        //dba = new DBAccess(d);
        path = p;
        net = null;
    }

    public void init(String[] args) {
        // args[0] format: YYmmdd_HHMMSS
        System.out.println("Network computation started: "+args[0]);
        long t0 = System.currentTimeMillis();
        try {

            //String a = new File(".").getAbsolutePath() +"/";
            String p = "input/" + args[0] + "/";
            String f = "network_"+args[0]+".dat";
            System.out.println("File: "+p+f);
            net = this.read(p + f);

            System.out.println("Density = "+net.getDensity());
            System.out.println("EdgeCount = "+net.getEdgeCount());
        } catch (Exception e) {
            e.printStackTrace();
        }
        //long t1 = System.currentTimeMillis();
        //System.out.println("Time elapsed = "+form.format((double)(t1-t0)/60000) + " minutes.");
    }

    //read BC from a file
    private Network<Integer, Integer> read(String f) {
        Network<Integer, Integer> n = null;
        try {
            File a = new File(f);
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(a));
            n = (Network<Integer, Integer>) ois.readObject();
            //ois.flush();
            ois.close();
            form = new DecimalFormat("#0.####");
            System.out.println("File read: "+f+ " ("+form.format( a.length() / Math.pow(2, 20))+" MB).");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException cnf) {
            cnf.printStackTrace();
        }
        return n;
    }

    //write network as a file
    private void write(String f) {
        try {
            File a = new File(f);
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(a));
            oos.writeObject(net);
            oos.flush();
            oos.close();
            form = new DecimalFormat("#0.####");
            System.out.println("File written: "+f+ " ("+form.format( a.length() / Math.pow(2, 20))+" MB).");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList getIntersection(ArrayList sameNodes, Network network) {
        if (sameNodes==null){
            sameNodes = new ArrayList(network.getVertices());
        }
        else{
            sameNodes.retainAll(network.getVertices());
        }
        return sameNodes;
    }

    public static void main(String[] args) {
        //Usage: NetworkHandler <Path to DB config file> <Path to store network files> <[Optional: DEBUG]>
        NetworkHandler nh = new NetworkHandler("./conf/conf_db.txt", args[0]);
        //nh.init(args);
        File input = new File("input");
        String [ ] dirs = input.list();
        ArrayList sameNodes=null;
        for(int i = 0; i < dirs.length; i++){
            String s = dirs[i];
            File snap = new File("input/"+s);
            if (snap.isDirectory()){
                String path = "input/"+s+"/network_"+s+".dat";
                Network<Integer, Integer> network = nh.read(path);
                System.out.println("Average degree: "+network.getAverageDegree());
                ClosenessCentrality close = new ClosenessCentrality((Hypergraph) network);
                System.out.println("Closeness of largest degree node: "+close.getVertexScore(network.getLargestDegree()));;

                //sameNodes=nh.getIntersection(sameNodes, network);
                //System.out.println("Intersection: "+sameNodes.size()+" nodes");


            }
        }
    }

}
