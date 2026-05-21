package petrolpark.mc.destroy.chemistry.serializer;

import petrolpark.mc.destroy.chemistry.legacy.LegacyBond.BondType;

/**
 * Directed edge between two {@link Node Nodes} carrying a {@link BondType}. Used by
 * {@link Branch}-based structure serialization to FROWNS.
*/
public class Edge {

    public BondType bondType;
    private Node srcNode;
    private Node destNode;
    public Boolean marked;

    public Edge(Node srcNode, Node destNode, BondType bondType) {
        this.srcNode = srcNode;
        this.destNode = destNode;
        this.bondType = bondType;
        marked = false;
    }

    public Node getSourceNode() {
        return srcNode;
    }

    public Node getDestinationNode() {
        return destNode;
    }

    public void flip() {
        Node temp = srcNode;
        srcNode = destNode;
        destNode = temp;
    }
}
