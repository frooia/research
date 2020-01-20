import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.correlation.KendallsCorrelation;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.Pair;

import edu.uci.ics.jung.algorithms.scoring.*;
import edu.uci.ics.jung.algorithms.shortestpath.BFSDistanceLabeler;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraDistance;
import edu.uci.ics.jung.algorithms.shortestpath.Distance;
import edu.uci.ics.jung.algorithms.shortestpath.UnweightedShortestPath;
import edu.uci.ics.jung.graph.*;
import edu.uci.ics.jung.graph.util.EdgeType;

import java.io.*;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static java.lang.Double.NaN;

public class Network<V,E> extends SparseMultigraph<V,E> {
    private static final long serialVersionUID = 1L;
    protected String name;
    protected double density;
    protected double totalBC; // total BetweennessCentrality
    protected V largestDegree;
    protected ArrayList<V> nodes;
    protected HashMap<Integer, Double> bcentrality;
    protected double [] kcorrelations = new double[12];

    public Network(String n) {
        super();
        density = 0.0;
        totalBC = 0.0;
        name = n;
    }

    public void computeDensity() {
        long n = this.getVertexCount();
        long m = this.getEdgeCount();
        density = (double) m / ((double)(n*(n-1)) / 2);
    }

    public void setNodes() {
        ArrayList<V> n = new ArrayList<V>();
        boolean b = n.addAll(this.getVertices());
        nodes = n;
    }

    public ArrayList<V> getNodes() {
        return nodes;
    }

    public double getDensity() {
        return density;
    }

    public String getName() {
        return name;
    }

    public void setTotalBC(double bc) {
        totalBC = bc;
    }

    public double getTotalBC() {
        return totalBC;
    }

    public double getAverageBC() {
        if (this.getVertexCount() != 0)
            return totalBC / this.getVertexCount();
        else return 0.0;
    }

    public HashMap<Integer, Double> getBcentrality() {
        return bcentrality;
    }

    public V getLargestDegree() {
        int ld = Integer.MIN_VALUE;
        for (V vertex : this.getVertices()) {
            if (this.degree(vertex) > ld) {
                ld = this.degree(vertex);
                largestDegree=vertex;
            }
        }
        return largestDegree;
    }

    public double getAverageOutDegree() {
        int sumDegree = 0;
        for (V vertex : nodes) {
            sumDegree += getSuccessors(vertex).size();
        }
        return (double) sumDegree / nodes.size();
    }

    public double getOutDegreeSD() {
        double avg = getAverageOutDegree();
        double sd = 0;
        for (V vertex : nodes) {
            sd += Math.pow(getSuccessors(vertex).size() - avg, 2) / nodes.size();
        }
        return sd;
    }

    public double getAverageCloseness(V node) {
        ClosenessCentrality close = new ClosenessCentrality((Hypergraph) this);
        double sumCloseness = 0;
        for (V vertex : this.getVertices()) {
            sumCloseness+=close.getVertexScore(vertex);
        }
        return (double)sumCloseness/this.getVertexCount();
    }

    /*
    public Set<V> getPredecessors(V vertex)
    {
        if (!containsVertex(vertex))
            return null;

        Set<V> preds = new HashSet<>();
        for (E edge : getIncoming_internal(vertex)) {
            if(getEdgeType(edge) == EdgeType.DIRECTED) {
                preds.add(this.getSource(edge));
            } else {
                preds.add(getOpposite(vertex, edge));
            }
        }
        return preds;
    }


    public Set<V> getAllPredecessors(V vertex)
    {
        Set<V> directPreds = getPredecessors(vertex);
        Set<V> preds = new HashSet<>(directPreds);
        if (preds.size() == 0) {
            return Collections.emptySet();
        }
        else {
            System.out.println();
            for (V dp : directPreds) {
                if (!preds.contains(dp)) {
                    System.out.println(dp);
                    preds.addAll(getAllPredecessors(dp));
                }
            }
            return preds;
        }
    }

    public boolean isStar(V node) {
        V[] neighbors = (V[]) this.getNeighbors(node).toArray();
        ArrayList<V> community = new ArrayList<>();
        for (V n : neighbors) { community.add(n); }
        for (V member : community) {
            for (V neighbor : this.getNeighbors(member)) {
                if (!community.contains(neighbor)) {
                    return false;
                }
            }
        }
        System.out.print(node+" is a star with neighbors ");
        for (V member : community) { System.out.print(member+" "); };
        System.out.println();
        return true;
    }

    public boolean isPair(V node) {
        V[] community = (V[]) this.getNeighbors(node).toArray();
        if (community.length == 1 && this.getNeighbors(community[0]).size() == 1) {
            System.out.println(node+" is a pair with "+community[0]);
            return true;
        }
        return false;
    }

    public double closeness(V node, boolean isIn) {
        ArrayList<V> editedNodes = (ArrayList<V>) nodes.clone();
        editedNodes.remove(node);
        UnweightedShortestPath path = new UnweightedShortestPath((Hypergraph) this);
        Map<V, Integer> distanceMap = new HashMap<>(path.getDistanceMap(node));
        distanceMap.remove(node);
        double value = 0.0;
//        BFSDistanceLabeler<V, Integer> bfsdl = new BFSDistanceLabeler<>();
//        bfsdl.labelDistances((Hypergraph<V, Integer>) this, new HashSet<>(editedNodes));
        Set predecessors = this.getAllPredecessors(node);
        if (isIn) {
            System.out.println("Predecessors: "+predecessors);
            for (Object element : predecessors) {
                    value += (int) path.getDistance(element, node);
            }
        }
        else {
            System.out.println("Successors: "+distanceMap);
            for (Map.Entry<V, Integer> element : distanceMap.entrySet()) {
                if (((Hypergraph) this).getEdgeType(((Hypergraph) this).findEdge(node, element.getKey())).compareTo(EdgeType.DIRECTED) == 0) {
                    value += element.getValue();
                }
            }
        }
        return value / (nodes.size() - 1);
    }

    public void setCloseness() throws IOException {
        File readFrom = new File("/Users/lydia/Documents/GitHub/backup_CyberTF/network_research/"+name+"/data_"+name+".tsv");
        File writeTo = new File("/Users/lydia/Documents/GitHub/backup_CyberTF/network_research/"+name+"/closeness_"+name+".tsv");
        PrintWriter dos = new PrintWriter(writeTo);
        Scanner inFile = new Scanner(readFrom);
        dos.println(inFile.nextLine()+"\tIn-Closeness\tOut-Closeness");
        HashMap<V, Double> inMap = new HashMap<V, Double>();
        HashMap<V, Double> outMap = new HashMap<V, Double>();
        int count = 0;
        double inclose;
        double outclose;
        for (V node : nodes) {
            count++;
            inclose = this.closeness(node, true);
            outclose = this.closeness(node, false);
            inMap.put(node, inclose);
            outMap.put(node, outclose);
            System.out.print(count+". "+node+": In-Closeness = "+inclose+" Out-Closeness = "+outclose);
            System.out.print(" and has "+((Hypergraph) this).getPredecessors(node).size()+" predecessors and "+((Hypergraph) this).getSuccessors(node).size()+" successors: ");
//            for (V n : (V[])((Hypergraph) this).getPredecessors(node).toArray()) { System.out.print(n+" ("+path.getDistance(node, n)+" "+((Hypergraph) this).getEdgeType(((Hypergraph) this).findEdge(node, n))+")  ");}
            System.out.println();

            String fileLine = inFile.nextLine();
            ArrayList<String> line = new ArrayList();
            boolean x = Collections.addAll(line, fileLine.split("\t"));
            line.add(Double.toString(inclose));
            line.add(Double.toString(outclose));
            String l = "";
            for (String s : line) { l+=s+"\t"; }
            dos.println(l);
        }
    }
*/

    public Map<V, Double> calcHits(boolean isHub) {
        HITS<V, E> ranker = new HITS<>((Graph<V, E>) this);
        ranker.evaluate();
        Map<V, Double> scoreMap = new HashMap<>();
        for (V node : nodes) {
            HITS.Scores hitScores = ranker.getVertexScore(node);
//            scoreMap.put(node, hitScores.hub);
            if (isHub) { scoreMap.put(node, hitScores.hub); }
            else { scoreMap.put(node, hitScores.authority); }
        }
        return scoreMap;
    }

    //runs SIR model with infection rate beta
    public int sir(V starter, double beta) {
        Random random = new Random();
        ArrayList<V> infected = new ArrayList();
        ArrayList<V> recovered = new ArrayList();
        infected.add(starter);
        int count = 0;
        while (infected.size() > 0) {
            for (int i = 0; i < infected.size(); i++) {
                V node = infected.get(i);
                int n = 0;
                V[] successors = (V[]) this.getSuccessors(node).toArray();
                for (int j = 0; j < successors.length; j++) {
                    if (!infected.contains(successors[j]) && !recovered.contains(successors[j]) && random.nextDouble() < beta) {
                        infected.add(successors[j]);
                    }
                    n++;
                }
                recovered.add(node);
                infected.remove(node);
                count++;
                //Degree and neighbor count are different because more than one edge may connect two nodes
                //System.out.print(count+". "+node+" has "+n+" neighbors should be "+this.degree(node)+" which connect: ");
                //for (E edge : this.getIncidentEdges(node)) { System.out.print(this.getIncidentCount(edge)+" "); }
                //System.out.println();
            }
        }
        //System.out.println("Count of recovered nodes: "+count);
        return recovered.size();
    }

    public Map<V, Double> runSIR(double beta) {
        Map<V, Double> recoveredMap = new HashMap<>();
        for (V node : nodes) {
            recoveredMap.put(node, sir(node, beta) / (double) nodes.size());
        }
        return recoveredMap;
    }

    public void readBC(String p, String name) {
        HashMap<Integer, Double> map = new HashMap<Integer, Double>();
        // Define charset and file path
        Charset c = Charset.forName("US-ASCII");
        // assumes the file is located in the same folder as the java class
        Path f = FileSystems.getDefault().getPath(p, name);

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
                    if (start && numNodes < this.getVertexCount()) {
                        // Your code starts here: capture needed data and store them in a hashmap
                        //extra condition for if statement: && net.getNodes().contains(Integer.parseInt(str[i].substring(14)))
                        if (str[i].contains("originalPos")) {
                            numOriginalPos++;
                            int node = Integer.parseInt(str[i+3].substring(8, str[i+3].length()-2));
                            if (this.getNodes().contains(node) && !map.containsKey(node)) {
                                numNodes++;
                                map.put(node, Double.parseDouble(str[i+1].substring(12)));
//                                System.out.println(numNodes+". "+node+" "+Double.parseDouble(str[i+1].substring(12)));
                            }
                        }
                        // Your code ends here
                    }
                }
            }
            System.out.println("numOriginalPos: " + numOriginalPos);
            System.out.println("numNodes: " + numNodes+" should be "+this.getNodes().size());
        } catch (IOException e) {
            System.err.format("IOException: %s%n", e);
        }
        System.out.println("Map size: "+map.size());
        bcentrality = map;
    }

    public Map<Integer, Double> readColumn(String col) throws FileNotFoundException { // works
        File readFrom = new File("/Users/lydia/Documents/GitHub/backup_CyberTF/network_research/"+name+"/data_"+name+".tsv");
        Scanner inFile = new Scanner(readFrom);
        Map map = new HashMap<V, Double>();
        int index = -1;
        while (inFile.hasNext()) {
            String fileLine = inFile.nextLine();
            ArrayList<String> line = new ArrayList();
            boolean x = Collections.addAll(line, fileLine.split("\t"));
            if (line.get(0).equals("Node")) { index = line.indexOf(col); }
            else { map.put(Integer.parseInt(line.get(0)), Double.parseDouble(line.get(index))); }
        }
        return map;
    }

    public List<Double> mapRanker(Map unranked) {
        Set entries = unranked.entrySet();
        Comparator<Map.Entry<Integer, Double>> comparator = new Comparator<Map.Entry<Integer, Double>>() {
            @Override
            public int compare(Map.Entry<Integer, Double> o1, Map.Entry<Integer, Double> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        };
        List<Map.Entry<Integer, Double>> entryList = new ArrayList<Map.Entry<Integer, Double>>(entries);
        Collections.sort(entryList, comparator);
        List<Double> sorted = new ArrayList<Double>();
        for (Map.Entry entry : entryList) {
            sorted.add(Double.valueOf(entry.getKey().toString()));
        }
        return sorted; // list of nodes
    }

    public void writeData() throws IOException {
        File file = new File("/Users/lydia/Documents/GitHub/backup_CyberTF/network_research/"+name+"/data_"+name+".tsv");
        FileWriter fos = new FileWriter(file);
        PrintWriter dos = new PrintWriter(fos);
        dos.write("Node\tIn-Degree\tOut-Degree\tBCentrality\t\n");
        for (V node : nodes) {
            dos.print(node+"\t"+this.getPredecessors(node).size()+"\t"+this.getSuccessors(node).size()+"\t"+bcentrality.get(node)+"\t\n");
        }
        dos.close();
        fos.close();
        System.out.println("written");
    }

    // adds a node property to the end of each line, effectively makes a new column
    public void editData(String title, Map data) throws IOException {
        File readFrom = new File("/Users/lydia/Documents/GitHub/backup_CyberTF/network_research/"+name+"/data_"+name+".tsv");
        File writeTo = new File("/Users/lydia/Documents/GitHub/backup_CyberTF/network_research/"+name+"/editdata_"+name+".tsv");
        Scanner inFile = new Scanner(readFrom);
        ArrayList<String> lines = new ArrayList<>();
        while (inFile.hasNext()) {
            String fileLine = inFile.nextLine();
            ArrayList<String> line = new ArrayList();
            boolean x = Collections.addAll(line, fileLine.split("\t"));
            if (line.get(0).equals("Node")) { line.add(title); }
            else { line.add(data.get(Integer.parseInt(line.get(0))).toString()); }
            String l = "";
            for (String s : line) {
                l+= s+"\t";
            }
            lines.add(l);
        }
        Files.write(writeTo.toPath(), lines, Charset.defaultCharset());
    }

    // insert your own file path and name
    public void editData(String title, Map data, String writeToPath) throws IOException {
        File readFrom = new File("/Users/lydia/Documents/GitHub/backup_CyberTF/network_research/"+name+"/data_"+name+".tsv");
        File writeTo = new File(writeToPath);
        Scanner inFile = new Scanner(readFrom);
        ArrayList<String> lines = new ArrayList<>();
        while (inFile.hasNext()) {
            String fileLine = inFile.nextLine();
            ArrayList<String> line = new ArrayList();
            boolean x = Collections.addAll(line, fileLine.split("\t"));
            if (line.get(0).equals("Node")) { line.add(title); }
            else { line.add(data.get(Integer.parseInt(line.get(0))).toString()); }
            String l = "";
            for (String s : line) {
                l+= s+"\t";
            }
            lines.add(l);
        }
        Files.write(writeTo.toPath(), lines, Charset.defaultCharset());
        System.out.println(name+" was edited");
    }

    public void removeColumn(String title, String writeToPath) throws IOException {
        File readFrom = new File("/Users/lydia/Documents/GitHub/backup_CyberTF/network_research/"+name+"/data_"+name+".tsv");
        File writeTo = new File(writeToPath);
        Scanner inFile = new Scanner(readFrom);
        ArrayList<String> lines = new ArrayList<>();
        int index = -1;
        while (inFile.hasNext()) {
            String fileLine = inFile.nextLine();
            ArrayList<String> line = new ArrayList();
            boolean x = Collections.addAll(line, fileLine.split("\t"));
            if (line.get(0).equals("Node")) { index = line.indexOf(title); }
            line.remove(index);
            String l = "";
            for (String s : line) {
                l+= s+"\t";
            }
            lines.add(l);
        }
        Files.write(writeTo.toPath(), lines, Charset.defaultCharset());
        System.out.println(title+" was removed");
    }

    public void setCorrelations() throws FileNotFoundException {
        KendallsCorrelation kc = new KendallsCorrelation();
        List<Double> l1 = new ArrayList(this.mapRanker(this.readColumn("BCentrality")));
        double [] l1a = new double[l1.size()];
        for (int i = 0; i < l1.size(); i++) { l1a[i] = l1.get(i); }
        List<Double> l2 = new ArrayList(this.mapRanker(this.readColumn("SIR")));
        double [] l2a = new double[l2.size()];
        for (int i = 0; i < l2.size(); i++) { l2a[i] = l2.get(i); }
        List<Double> l3 = new ArrayList(this.mapRanker(this.readColumn("In-Degree")));
        double [] l3a = new double[l3.size()];
        for (int i = 0; i < l3.size(); i++) { l3a[i] = l3.get(i); }
        List<Double> l4 = new ArrayList(this.mapRanker(this.readColumn("Out-Degree")));
        double [] l4a = new double[l4.size()];
        for (int i = 0; i < l4.size(); i++) { l4a[i] = l4.get(i); }
        List<Double> l5 = new ArrayList(this.mapRanker(this.readColumn("HubScore")));
        double [] l5a = new double[l5.size()];
        for (int i = 0; i < l5.size(); i++) { l5a[i] = l5.get(i); }
        List<Double> l6 = new ArrayList(this.mapRanker(this.readColumn("AuthorityScore")));
        double [] l6a = new double[l6.size()];
        for (int i = 0; i < l6.size(); i++) { l6a[i] = l6.get(i); }
        double [] scores = new double[12];
        scores[0] = kc.correlation(l2a, l5a); //SIR vs. HubScore
        scores[1] = kc.correlation(l2a, l6a); //SIR vs. AuthorityScore
        scores[2] = kc.correlation(l3a, l4a); //In-degree vs. Out-degree
        scores[3] = kc.correlation(l3a, l1a); //In-degree vs. BCentrality
        scores[4] = kc.correlation(l3a, l5a); //In-degree vs. HubScore
        scores[5] = kc.correlation(l3a, l6a); //In-degree vs. AuthorityScore
        scores[6] = kc.correlation(l4a, l1a); //Out-degree vs. BCentrality
        scores[7] = kc.correlation(l4a, l5a); //Out-degree vs. HubScore
        scores[8] = kc.correlation(l4a, l6a); //Out-degree vs. AuthorityScore
        scores[9] = kc.correlation(l1a, l5a); //BCentrality vs. HubScore
        scores[10] = kc.correlation(l1a, l6a); //BCentrality vs. AuthorityScore
        scores[11] = kc.correlation(l5a, l6a); //HubScore vs. AuthorityScore
        kcorrelations = scores;
    }

    public String toString() {
        // name n m density avgoutdegree largestdegreenode totalbc
        return name+"\t"+this.getVertexCount()+"\t"+this.getEdgeCount()+"\t"+this.getDensity()+"\t"+this.getAverageOutDegree()+"\t"+this.getLargestDegree()+"\t"+totalBC;
    }
}