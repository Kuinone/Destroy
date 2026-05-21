package petrolpark.mc.destroy.chemistry.serializer;

import java.util.ArrayList;
import java.util.List;

import petrolpark.mc.destroy.chemistry.legacy.LegacyAtom;
import petrolpark.mc.destroy.chemistry.legacy.LegacyBond.BondType;
import petrolpark.mc.destroy.chemistry.legacy.LegacyElement;

/**
 * Linear chain of {@link Node Nodes} connected by {@link Edge Edges} — the backbone of a FROWNS
 * branch. Side branches hang off individual nodes (see {@link Node#addSideBranch}).
*/
public class Branch {

    private final List<Node> nodes = new ArrayList<>();
    private Node startNode;
    private Node endNode;

    public Branch(Node node) {
        this.nodes.add(node);
        this.startNode = node;
        this.endNode = node;
    }

    public String serialize() {
        return startNode.serialize();
    }

    public Node getStartNode() {
        return startNode;
    }

    public Node getEndNode() {
        return endNode;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public Branch add(Node node, BondType bondType) {
        nodes.add(node);
        Edge newEdge = new Edge(endNode, node, bondType);
        endNode.addEdge(newEdge);
        node.addEdge(newEdge);
        node.setBranch(this);
        node.visited = true;
        endNode = node;
        return this;
    }

    /**
 * Connects the <em>start</em> of {@code branchToAdd} to the <em>end</em> of this Branch,
 * advancing this Branch's end pointer to {@code branchToAdd}'s end.
*/
    public Branch add(Branch branchToAdd, BondType bondType) {
        Edge newEdge = new Edge(endNode, branchToAdd.getStartNode(), bondType);
        nodes.addAll(branchToAdd.getNodes());
        for (Node node : branchToAdd.getNodes()) {
            node.setBranch(this);
        }
        branchToAdd.getStartNode().addEdge(newEdge);
        endNode.addEdge(newEdge);
        endNode = branchToAdd.endNode;
        return this;
    }

    public Branch flip() {
        for (Node node : nodes) {
            for (Edge edge : node.getEdges()) {
                if (!edge.marked) {
                    edge.flip();
                    edge.marked = true;
                }
            }
        }
        for (Node node : nodes) {
            for (Edge edge : node.getEdges()) {
                edge.marked = false;
            }
        }
        Node temp = startNode;
        startNode = endNode;
        endNode = temp;
        return this;
    }

    public Float getMass() {
        float total = 0f;
        for (Node node : nodes) {
            total += getMassForComparisonInSerialization(node.getAtom());
            for (Branch branch : node.getSideBranches().keySet()) {
                total += branch.getMass();
            }
        }
        return total;
    }

    public Float getMassOfLongestChain() {
        float total = 0f;
        for (Node node : nodes) {
            total += getMassForComparisonInSerialization(node.getAtom());
        }
        return total;
    }

    public static Float getMassForComparisonInSerialization(LegacyAtom atom) {
        return atom.getElement().getMass() * (atom.getElement() == LegacyElement.R_GROUP ? atom.rGroupNumber : 1);
    }
}
