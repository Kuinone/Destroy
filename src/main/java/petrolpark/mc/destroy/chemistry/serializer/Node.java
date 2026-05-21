package petrolpark.mc.destroy.chemistry.serializer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import petrolpark.mc.destroy.chemistry.legacy.LegacyAtom;
import petrolpark.mc.destroy.chemistry.legacy.LegacyBond.BondType;
import petrolpark.mc.destroy.chemistry.legacy.LegacyElement;

/**
 * Node in a FROWNS serialization graph — wraps one {@link LegacyAtom} with traversal state and
 * branch membership. {@link #serialize()} emits the recursive FROWNS string for this node's
 * subtree including side branches sorted by mass.
*/
public class Node {

    private final LegacyAtom atom;
    public Boolean visited;
    private final List<Edge> edges;
    private Branch branch;
    private final Map<Branch, BondType> sideBranches;

    public Node(LegacyAtom atom) {
        this.atom = atom;
        this.visited = false;
        this.edges = new ArrayList<>();
        this.sideBranches = new HashMap<>();
    }

    public String serialize() {
        StringBuilder sb = new StringBuilder(getAtom().getElement().getSymbol());
        boolean isTerminal = true;
        Edge nextEdge = null;
        for (Edge edge : edges) {
            if (edge.getSourceNode() == this) {
                isTerminal = false;
                nextEdge = edge;
                break;
            }
        }
        if (atom.rGroupNumber != 0 && atom.getElement() == LegacyElement.R_GROUP) {
            sb.append(atom.rGroupNumber);
        }
        if (atom.formalCharge != 0) {
            sb.append("^");
            sb.append(atom.formalCharge % 1.0 != 0
                ? String.format("%s", atom.formalCharge)
                : String.format("%.0f", atom.formalCharge));
        }
        if (!isTerminal && nextEdge != null) {
            sb.append(nextEdge.bondType.getFROWNSCode());
        }
        for (Entry<Branch, BondType> entry : getSideBranches().entrySet()) {
            sb.append("(").append(entry.getValue().getFROWNSCode()).append(entry.getKey().serialize()).append(")");
        }
        if (!isTerminal && nextEdge != null) {
            sb.append(nextEdge.getDestinationNode().serialize());
        }
        return sb.toString();
    }

    public LegacyAtom getAtom() {
        return atom;
    }

    public Node addEdge(Edge edge) {
        edges.add(edge);
        return this;
    }

    public Node deleteEdge(Edge edge) {
        edges.remove(edge);
        return this;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public Node setBranch(Branch branch) {
        this.branch = branch;
        return this;
    }

    public Branch getBranch() {
        return branch;
    }

    public Node addSideBranch(Branch branch, BondType bondType) {
        sideBranches.put(branch, bondType);
        return this;
    }

    public Map<Branch, BondType> getSideBranches() {
        return sideBranches;
    }

    public List<Entry<Branch, BondType>> getOrderedSideBranches() {
        List<Entry<Branch, BondType>> sideBranchesAndBondTypes = new ArrayList<>(getSideBranches().entrySet());
        Collections.sort(sideBranchesAndBondTypes,
            (e1, e2) -> e1.getKey().getMassOfLongestChain().compareTo(e2.getKey().getMassOfLongestChain()));
        return sideBranchesAndBondTypes;
    }
}
