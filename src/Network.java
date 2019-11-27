import edu.uci.ics.jung.algorithms.scoring.*;
import edu.uci.ics.jung.graph.*;

import java.lang.reflect.Array;
import java.util.*;

public class Network<V,E> extends SparseMultigraph<V,E> {
    private static final long serialVersionUID = 1L;
    protected String name;
    protected double density;
    protected double totalBC; // total BetweennessCentrality
    protected V largestDegree;
    protected ArrayList<V> nodes;
    protected HashMap<V, Double> bcentrality;

    public Network(String n) {
        super();
        density = 0.0;
        totalBC = 0.0;
        name = n;
    }

    public void setBCentrality(HashMap<V, Double> bc) {
        bcentrality = bc;
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

    public void setName(String name) {
        this.name = name;
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

    public double getAverageDegree() {
        int sumDegree = 0;
        for (V vertex : this.getVertices()) {
            sumDegree+=this.degree(vertex);
        }
        return (double)sumDegree/this.getVertexCount();
    }

    public double getAverageCloseness() {
        ClosenessCentrality close = new ClosenessCentrality((Hypergraph) this);
        double sumCloseness = 0;
        for (V vertex : this.getVertices()) {
            sumCloseness+=close.getVertexScore(vertex);
        }
        return (double)sumCloseness/this.getVertexCount();
    }

    //runs SIR model with infection rate beta
    public int sir(V starter, double beta) {
        Random random = new Random();
        ArrayList<V> infected = new ArrayList();
        ArrayList<V> recovered = new ArrayList();
        infected.add(starter);
        while (infected.size() != 0) {
            for (int i = 0; i < infected.size(); i++) { // for every infected node
                V node = infected.get(i);
                int neighbors = 0;
                if (this.getNeighbors(node).size() > 0) { // if the infected node has neighbors
                    for (int j = 0; j < this.getNeighbors(node).size(); j++) { // for every neighbor
                        if (random.nextDouble() < beta) {
                            // add the neighbor to infected with probability beta
                            infected.add((V)this.getNeighbors(node).toArray()[j]);
                        }
                        neighbors++;
                        recovered.add(node);
                        infected.remove(node);
                    }
                }
                System.out.println(i+". "+node+" has "+neighbors+" neighbors should be "+this.degree(node)+" neighbors");
            }
        }

        return recovered.size();
    }
}