package gdma.routeSkyline.impl;

import gdma.routeSkyline.MultiWeightedPath;
import org.jetbrains.annotations.NotNull;
import org.neo4j.graphalgo.CostEvaluator;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.traversal.Paths;

import java.util.*;

import static java.util.Collections.unmodifiableList;
import static org.neo4j.graphalgo.CommonEvaluators.doubleCostEvaluator;

public class MultiWeightedPathImpl implements MultiWeightedPath, Cloneable {
    private static List<PropertyKey> propertyKeyList;
    private final LinkedList<Relationship> relationships;
    private final LinkedList<Node> nodes;
    private final Map<PropertyKey,Double> weight;
    private final
    class ReverseIterator<E> implements Iterator<E>{
        private ListIterator<E> listIterator;
        ReverseIterator(List<E> list){
            this.listIterator = list.listIterator(list.size());
        }
        @Override
        public boolean hasNext() {
            return listIterator.hasPrevious();
        }

        @Override
        public E next() {
            return listIterator.previous();
        }
    }

    MultiWeightedPathImpl(Node node, List<PropertyKey> propertyKeyList){
        if(Objects.nonNull(propertyKeyList)){
            MultiWeightedPathImpl.propertyKeyList = propertyKeyList;
        }
        nodes = new LinkedList<>();
        relationships = new LinkedList<>();
        weight = new HashMap<>((int)(propertyKeyList.size()*1.34)+1);

        nodes.add(node);

        for(PropertyKey propertyKey:propertyKeyList){
            weight.put(propertyKey,0.0);
        }
    }

    @Override
    public Node startNode() {
        return nodes.getFirst();
    }

    @Override
    public Node endNode() {
        return nodes.getLast();
    }

    @Override
    public Relationship lastRelationship() {
        return relationships.getLast();
    }

    @Override
    public Iterable<Relationship> relationships() {
        return ()-> unmodifiableList(relationships).iterator();
    }

    @Override
    public Iterable<Relationship> reverseRelationships() {
        return ()->new ReverseIterator<>(unmodifiableList(relationships));
    }

    @Override
    public Iterable<Node> nodes() {
        return ()->unmodifiableList(nodes).iterator();
    }

    @Override
    public Iterable<Node> reverseNodes() {
        return ()->new ReverseIterator<>(unmodifiableList(nodes));
    }

    @Override
    public int length() {
        return relationships.size();
    }

    @Override
    public Iterator<PropertyContainer> iterator() {
        return null;
    }

    public @NotNull MultiWeightedPathImpl append(Relationship relationship){
        if(Objects.isNull(relationship)||!this.endNode().equals(relationship.getStartNode())){
            throw new IllegalArgumentException("The startNode of relationship does not match the endNode of the path");
        }

        MultiWeightedPathImpl path = this.clone();
        path.addRelationship(relationship);
        return path;
    }

    private void addRelationship(@NotNull Relationship relationship){
        this.nodes.add(relationship.getEndNode());
        this.relationships.add(relationship);

        for(PropertyKey propertyKey:propertyKeyList){
            CostEvaluator<Double> costEvaluator = doubleCostEvaluator(propertyKey.NAME);
            Double oldCost = weight.get(propertyKey);
            Double newCost = costEvaluator.getCost(relationship,Direction.OUTGOING);
            oldCost = Objects.nonNull(oldCost) ? oldCost : 0.0;
            newCost = Objects.nonNull(newCost) ? newCost : 0.0;
            weight.put(propertyKey,oldCost+newCost);
        }
    }

    public List<Relationship> getRelationships() {
        return unmodifiableList(relationships);
    }

    public List<Node> getNodes() {
        return unmodifiableList(nodes);
    }

    public Map<PropertyKey, Double> getWeight() {
        return Collections.unmodifiableMap(weight);
    }

    @Override
    public int hashCode() {
        return this.relationships.hashCode();
    }

    @Override
    public boolean equals(Object obj) {

        if(! (obj instanceof MultiWeightedPathImpl)){
            return false;
        }

        return relationships.equals(((MultiWeightedPathImpl) obj).getRelationships());
    }

    private MultiWeightedPathImpl(LinkedList<Relationship> relationships, LinkedList<Node> nodes, Map<PropertyKey,Double> weight){
        this.relationships=relationships;
        this.nodes = nodes;
        this.weight = weight;
    }

    @Override
    protected MultiWeightedPathImpl clone() {
        return new MultiWeightedPathImpl(
                new LinkedList<>(this.relationships),
                new LinkedList<>(this.nodes),
                new HashMap<>(this.weight)
        );
    }

    @Override
    public String toString() {
        return Paths.defaultPathToString(this);
    }
}
