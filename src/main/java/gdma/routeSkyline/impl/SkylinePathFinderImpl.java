package gdma.routeSkyline.impl;

import gdma.routeSkyline.MultiWeightedPath;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.traversal.BranchState;
import org.neo4j.graphdb.traversal.Paths;
import org.neo4j.graphdb.traversal.TraversalMetadata;
import org.neo4j.graphdb.traversal.Traverser;

import java.util.*;
import java.util.regex.Pattern;

public class SkylinePathFinderImpl implements PathFinder<MultiWeightedPath> {
    private static Pattern pattern= Pattern.compile("^(\\w*)$");
    //Don't directly get from this map. Call <code>getLowerBound()</code> instead.
    //private final Map<Node,Map<PropertyKey,Double>> lowerBounds;
    private final PathExpander pathExpander;

    //final Map<Node,Map<PropertyKey,Double>> weights;
    final static List<PropertyKey> propertyKeyList = new LinkedList<>();

    private final RelationshipType relType;
    private Metadata lastMetadata;

    public SkylinePathFinderImpl(String relType, String[] propertyKeys){
        /*if(Objects.isNull(propertyKeys)||propertyKeys.length==0){
            throw new NullPointerException();
        }*/

        //lowerBounds = new HashMap<>();
        //weights = new HashMap<>();

        this.relType=RelationshipType.withName(relType);

        generatePropertyKeySet(propertyKeys);
        pathExpander = buildPathExpander();

    }


    @Override
    public MultiWeightedPath findSinglePath(Node start, Node end) {
        lastMetadata = new Metadata();
        return this.findAllPaths(start,end).iterator().next();
    }

    @Override
    public Iterable<MultiWeightedPath> findAllPaths(Node start, Node end) {

        LowerBoundEstimator<Map<PropertyKey, Double>> estimator = new LowerBoundEstimatorImplDijkstra(propertyKeyList,pathExpander,start,end);
        SkylineComputation skylineComputation = new SkylineComputationImplARSC(propertyKeyList,pathExpander,estimator);
        Collection<MultiWeightedPath> pathCollection = Collections.unmodifiableCollection(skylineComputation.findAllPath(start,end));

        return ()->pathCollection.iterator();
    }

    private void generatePropertyKeySet(String[] params) {

        for(String param : params){
            param = param.trim();
            if(pattern.matcher(param).matches()){
                propertyKeyList.add(new PropertyKey(param));
            }
        }

    }

    private PathExpander<Object> buildPathExpander() {
        return PathExpanderBuilder.empty().add(relType,Direction.OUTGOING).build();
    }

    // TODO: 21/06/2019 neo4j traverser

    //just some notes/hints in the java format, useless for program running.
    private void customNotes(){

        Node startNode=null;
        Node endNode=null;
        Traverser traverser;
        Paths.singleNodePath(startNode);
        PathExpander pathExpander = buildPathExpander();
        //StandardExpander.RegularExpander pathExpander = StandardExpander.RegularExpander();
        pathExpander.expand(Paths.singleNodePath(startNode), BranchState.NO_STATE);
        //StandardExpander.RegularExpander().doExpand(Paths.singleNodePath(startNode), BranchState.NO_STATE);
    }

    @Override
    public TraversalMetadata metadata() {
        return null;
    }

    private static class Metadata implements TraversalMetadata
    {
        private int rels;
        private int paths;

        @Override
        public int getNumberOfPathsReturned()
        {
            return paths;
        }

        @Override
        public int getNumberOfRelationshipsTraversed()
        {
            return rels;
        }
    }
}
