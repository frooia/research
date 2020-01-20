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

import org.apache.commons.math3.stat.correlation.*;
import org.apache.commons.lang3.*;


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
                    String p = "/Users/lydia/Documents/GitHub/backup_CyberTF/network_research/" + args[i] + "/";
                    String f = "network_"+args[i]+".dat";
                    System.out.println("File: "+p+f);
                    net = this.readNet(p + f);
                    System.out.println("Density = "+net.getDensity());
                    System.out.println("EdgeCount = "+net.getEdgeCount());
                    net.setNodes();
                    System.out.println("NodeCount = "+net.getVertexCount());
                    net.setCorrelations();

                    /*
                    int in0 = 0; int in1 = 0; int in2 = 0; int in310 = 0; int in100 = 0; int in1000 = 0; int in1000p = 0;
                    for (int node : net.getNodes()) {
                        if (net.getPredecessors(node).size() == 0) { in0++; }
                        else if (net.getPredecessors(node).size() == 1) { in1++; }
                        else if (net.getPredecessors(node).size() == 2) { in2++; }
                        else if (net.getPredecessors(node).size() <= 10) { in310++; }
                        else if (net.getPredecessors(node).size() <= 100) { in100++; }
                        else if (net.getPredecessors(node).size() <= 1000) { in1000++; }
                        else { in1000p++; }
                    }
                    System.out.println("In-degrees:\n0: "+in0+"\n1: "+in1+"\n2: "+in2+"\n3-10: "+in310+"\n11-100: "+in100+"\n101-1000: "+in1000+"\n1001 and more: "+in1000p);

                    int out0 = 0; int out1 = 0; int out2 = 0; int out310 = 0; int out100 = 0; int out1000 = 0; int out1000p = 0;
                    for (int node : net.getNodes()) {
                        if (net.getSuccessors(node).size() == 0) { out0++; }
                        else if (net.getSuccessors(node).size() == 1) { out1++; }
                        else if (net.getSuccessors(node).size() == 2) { in2++; }
                        else if (net.getSuccessors(node).size() <= 10) { out310++; }
                        else if (net.getSuccessors(node).size() <= 100) { out100++; }
                        else if (net.getSuccessors(node).size() <= 1000) { out1000++; }
                        else { out1000p++; }
                    }
                    System.out.println("Out-degrees:\n0: "+out0+"\n1: "+out1+"\n2: "+out2+"\n3-10: "+out310+"\n11-100: "+out100+"\n101-1000: "+out1000+"\n1001 and more: "+out1000p);
                     */

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
    private HashMap<Integer, Double> readBC(String p, String name, Network net) {
        HashMap<Integer, Double> map = new HashMap<Integer, Double>();
        // Define charset and file path
        Charset c = Charset.forName("US-ASCII");
        // assumes the file is located in the same folder as the java class
        Path f = FileSystems.getDefault().getPath(p, name);
        System.out.println("File: "+f);

        // open a file using the specified charset
        try (BufferedReader r = Files.newBufferedReader(f, c)) {
            String ln = null; // a line in the file
            boolean start = false; // a flag to indicate start of reading data

            int numOriginalPos = 0;
            int numNodes = 0;
            // for each line, process the string to extract needed data
            while ((ln = r.readLine()) != null) {
                // split the line by comma and form an array of the component strings
                String[] str = ln.split(",");
                int j = 0;
                // print out each component in one line
                for (int i = 0; i < str.length; i++) {
                    if (!start && str[i].contains("edu.uci.ics.jung.algorithms.importance.Ranking")) start = true;
                    if (start && numNodes < net.getNodes().size()) {
                        //System.out.println((++j)+". "+str[i]);
                        // Your code starts here: capture needed data and store them in a hashmap
                        //extra condition for if statement: && net.getNodes().contains(Integer.parseInt(str[i].substring(14)))
                        if (str[i].contains("originalPos")) {
                            numOriginalPos++;
                            int node = Integer.parseInt(str[i+3].substring(8, str[i+3].length()-2));
                            if (net.getNodes().contains(node)) {
                                numNodes++;
                                map.put(Integer.parseInt(str[i].substring(14)), Double.parseDouble(str[i+1].substring(12)));
                            }
                        }
                        // Your code ends here
                    }
                }
            }
            System.out.println("numOriginalPos: " + numOriginalPos);
            System.out.println("numNodes: " + numNodes+" should be "+net.getNodes().size());
        } catch (IOException e) {
            System.err.format("IOException: %s%n", e);
        }
        System.out.println("Map size: "+map.size());
        return map;
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

    public static int sirTemp(ArrayList<Network<Integer, Integer>> networks, int starter, double beta) {
        Random random = new Random();
        ArrayList<Integer> infected = new ArrayList();
        ArrayList<Integer> recovered = new ArrayList();
        infected.add(starter);
        int t = 0;
        while (t < networks.size() && infected.size() > 0) {
            for (int i = 0; i < infected.size(); i++) {
                int node = infected.get(t);
                if (networks.get(t).containsVertex(node)) {
                    ArrayList neighbors = new ArrayList();
                    neighbors.addAll(networks.get(t).getNeighbors(node));
                    for (int j = 0; j < neighbors.size(); j++) {
                        if (!infected.contains(neighbors.get(j)) && !recovered.contains(neighbors.get(j)) && random.nextDouble() < beta) {
                            infected.add((int)neighbors.get(j));
                        }
                    }
                }
                recovered.add(node);
                infected.remove(node);
            }
            t++;
        }
        return recovered.size();
    }

    public static void main(String[] args) throws IOException {
        //Usage: NetworkHandler <Path to DB config file> <Path to store network files> <[Optional: DEBUG]>
        NetworkHandler nh = new NetworkHandler("./conf/conf_db.txt", args[0]);
        String [ ] dirs = (new File("/Users/lydia/Documents/GitHub/backup_CyberTF/network_research")).list();
        Arrays.sort(dirs);
//        ArrayList<Network<Integer, Integer>> netList = nh.init(Arrays.copyOfRange(dirs, 1, 30));
        ArrayList<Network<Integer, Integer>> netList = nh.init(Arrays.copyOfRange(dirs, 30, 57));
/* done
        File file = new File("/Users/lydia/Documents/GitHub/backup_CyberTF/correlation_scores.tsv");
        FileWriter fos = new FileWriter(file);
        PrintWriter dos = new PrintWriter(fos);
        dos.write("Network\t\n");
        for (int i = 1; i < dirs.length; i++) {
            dos.println(dirs[i]+"\t");
        }
        dos.close();
        fos.close();
        System.out.println("written");

 */
        File writeTo = new File("/Users/lydia/Documents/GitHub/backup_CyberTF/temp.tsv");
        ArrayList<String> lines = new ArrayList<>();
        lines.add("SIR vs. HubScore\tSIR vs. AuthorityScore\tIn-degree vs. Out-degree\tIn-degree vs. BCentrality\tIn-degree vs. HubScore\tIn-degree vs. AuthorityScore\tOut-degree vs. BCentrality\tOut-degree vs. HubScore\tOut-degree vs. AuthorityScore\tBCentrality vs. HubScore\tBCentrality vs. AuthorityScore\tHubScore vs. AuthorityScore");
        for (Network net : netList) {
            /* done
            net.readBC("/Users/lydia/Documents/GitHub/backup_CyberTF/network_research/"+net.getName(), "BCentrality_"+net.getName()+".dat");
            net.writeData();
            net.editData("HubScore", net.calcHits(true), "/Users/lydia/Documents/GitHub/backup_CyberTF/network_research/"+net.getName()+"/data_"+net.getName()+".tsv");
            net.editData("AuthorityScore", net.calcHits(false), "/Users/lydia/Documents/GitHub/backup_CyberTF/network_research/"+net.getName()+"/data_"+net.getName()+".tsv");
            System.out.println(net.getAverageOutDegree());
            System.out.println(net.getOutDegreeSD());
            double beta = net.getAverageOutDegree() / (net.getOutDegreeSD() - net.getAverageOutDegree());

            net.removeColumn("SIR", "/Users/lydia/Documents/GitHub/backup_CyberTF/network_research/"+net.getName()+"/data_"+net.getName()+".tsv");
            net.editData("SIR", net.runSIR(0.5), "/Users/lydia/Documents/GitHub/backup_CyberTF/network_research/"+net.getName()+"/data_"+net.getName()+".tsv");

            KendallsCorrelation kc = new KendallsCorrelation();
            List<Double> l1 = new ArrayList(net.mapRanker(net.readColumn("BCentrality")));
            double [] l1a = new double[l1.size()];
            for (int i = 0; i < l1.size(); i++) { l1a[i] = l1.get(i); }
            List<Double> l2 = new ArrayList(net.mapRanker(net.readColumn("SIR")));
            double [] l2a = new double[l2.size()];
            for (int i = 0; i < l2.size(); i++) { l2a[i] = l2.get(i); }
            double correlationScore = kc.correlation(l1a, l2a);
            System.out.println(net.getName()+": "+correlationScore);
             */
            String line = "";
            for (double correlationScore : net.kcorrelations) {
                line += correlationScore+"\t";
            }
            lines.add(line);

        }
        Files.write(writeTo.toPath(), lines, Charset.defaultCharset());
        System.out.println("written");

    }
}
