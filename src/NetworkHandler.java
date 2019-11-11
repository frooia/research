import java.io.*;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.*;

import edu.uci.ics.jung.algorithms.importance.BetweennessCentrality;
import edu.uci.ics.jung.algorithms.importance.Ranking;
import edu.uci.ics.jung.algorithms.scoring.ClosenessCentrality;
import edu.uci.ics.jung.algorithms.scoring.DistanceCentralityScorer;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Hypergraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonIoException;


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

    public ArrayList<Network<Integer, Integer>> init(String[] args) {
        // args[0] format: YYmmdd_HHMMSS
        ArrayList<Network<Integer, Integer>> netList = new ArrayList<Network<Integer, Integer>>(args.length);
        for (int i = 0; i < args.length; i++) {
            if (args[i].length()==13){
                System.out.println("Network computation started: "+args[i]);
                long t0 = System.currentTimeMillis();
                try {

                    //String a = new File(".").getAbsolutePath() +"/";
                    String p = "input/" + args[i] + "/";
                    String f = "network_"+args[i]+".dat";
                    System.out.println("File: "+p+f);
                    net = this.readNet(p + f);
                    System.out.println("Density = "+net.getDensity());
                    System.out.println("EdgeCount = "+net.getEdgeCount());
                    System.out.println("NodeCount = "+net.getVertexCount());
                    System.out.println();
                    netList.add(net);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        //long t1 = System.currentTimeMillis();
        //System.out.println("Time elapsed = "+form.format((double)(t1-t0)/60000) + " minutes.");
        return netList;
    }

    //read BC from a file
    private void readBC(String p, String name) {
        HashMap<Integer, Double> map = new HashMap<Integer, Double>();
        /*
        try {
            Scanner in = new Scanner(f);
            String remaining = in.next();
            int count = 0;
            System.out.println(in.hasNext());
            while (in.hasNext()) {
                int firstIndex = remaining.indexOf("\"originalPos\"");
                remaining = remaining.substring(firstIndex + 14);
                in = new Scanner(remaining);
                map.put(in.nextInt(), 1.0);
                count++;
                //map.put(all.substring(all.indexOf("\"originalPos\"", start)+14),);

            }
            System.out.println("BCentrality calculated. n = "+n+" and count = "+count);
            //form = new DecimalFormat("#0.####");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
*/
        // Define charset and file path
        Charset c = Charset.forName("US-ASCII");
        // assumes the file is located in the same folder as the java class
        Path f = FileSystems.getDefault().getPath(p, name);

        // open a file using the specified charset
        try (BufferedReader r = Files.newBufferedReader(f, c)) {
            String ln = null; // a line in the file
            boolean start = false; // a flag to indicate start of reading data

            // for each line, process the string to extract needed data
            while ((ln = r.readLine()) != null) {
                // split the line by comma and form an array of the component strings
                String[] str = ln.split(",");
                int j = 0;
                // print out each component in one line
                for (int i = 0; i < str.length; i++) {
                    if (!start && str[i].contains("edu.uci.ics.jung.algorithms.importance.Ranking")) start = true;
                    if (start) {
                        System.out.println((++j)+". "+str[i]);

                        // Your code starts here: capture needed data and store them in a hashmap
                        if (str[i].contains("originalPos")) {
                            map.put(Integer.parseInt(str[i].substring(14)), Double.parseDouble(str[i+1].substring(12)));
                        }




                        // Your code ends here
                    }
                }
            }
            int numNull = 0;
            for (Integer k : map.keySet()) {
                if (map.get(k) <= 3.0) {
                    numNull++;
                }
            }
            System.out.println("numNull: "+numNull);
        } catch (IOException e) {
            System.err.format("IOException: %s%n", e);
        }
        System.out.println("File: "+f);
        System.out.println("Map size: "+map.size());
        //System.out.println("BC of 143345: "+map.get(143345));
    }

    private Network<Integer, Integer> readNet(String f) {
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
        String [ ] dirs = (new File("input")).list();
        ArrayList<Network<Integer, Integer>> netList = nh.init(dirs);
        for (int i = 0; i < dirs.length; i++) {
            if (dirs[i].contains("180207_190200")){
                nh.readBC("input/"+dirs[i],"BCentrality_"+dirs[i]+".dat");
            }
        }

        //System.out.println("Average degree: "+network.getAverageDegree());
        //ClosenessCentrality close = new ClosenessCentrality((Hypergraph) network);
        //System.out.println("Closeness of largest degree node: "+close.getVertexScore(network.getLargestDegree()));;

        //sameNodes=nh.getIntersection(sameNodes, network);
        //System.out.println("Intersection: "+sameNodes.size()+" nodes");
    }

}
