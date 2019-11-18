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
        return largestDegree;
    }

    public double getAverageDegree() {
        int sumDegree = 0;
        int ld = Integer.MIN_VALUE;
        for (V vertex : this.getVertices()) {
            if (this.degree(vertex)>ld) {
                ld = this.degree(vertex);
                largestDegree=vertex;
            }
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
            for (V v : infected) {
                for (V neighbor : this.getNeighbors(v)) {
                    if (random.nextDouble() < beta) {
                        infected.add(neighbor);
                    }
                    infected.remove(v);
                    recovered.add(v);
                }
            }
        }

        return recovered.size();
    }
}