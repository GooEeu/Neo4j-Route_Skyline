package gdma.routeSkyline.impl;

import gdma.routeSkyline.MultiWeightedPath;
import gdma.routeSkyline.SkylinePathFinder;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.traversal.TraversalMetadata;

import java.util.*;
import java.util.regex.Pattern;

public class SkylinePathFinderImpl implements SkylinePathFinder {
    //final Map<Node,Map<PropertyKey,Double>> weights;
    private final static List<PropertyKey> propertyKeyList = new LinkedList<>();
    private static Pattern pattern = Pattern.compile("^(\\w*)$");
    //Don't directly get from this map. Call <code>getLowerBound()</code> instead.
    //private final Map<Node,Map<PropertyKey,Double>> lowerBounds;
    private final PathExpander pathExpander;
    private final RelationshipType relType;
    private Metadata lastMetadata;

    public SkylinePathFinderImpl(String relType, String[] propertyKeys) {
        this.relType = RelationshipType.withName(relType);

        generatePropertyKeySet(propertyKeys);
        pathExpander = buildPathExpander();

    }


    @Override
    public MultiWeightedPath findSinglePath(Node start, Node end) {
        lastMetadata = new Metadata();
        return this.findAllPaths(start, end).iterator().next();
    }

    @Override
    public Iterable<MultiWeightedPath> findAllPaths(Node start, Node end) {

        LowerBoundEstimator<Map<PropertyKey, Double>> estimator = new LowerBoundEstimatorImplDijkstra(propertyKeyList, pathExpander, start, end);
        SkylineComputation skylineComputation = new SkylineComputationImplARSC(propertyKeyList, pathExpander, estimator);

        return Collections.unmodifiableCollection(skylineComputation.findAllSkylinePath(start, end));
    }

    private void generatePropertyKeySet(String[] params) {

        for (String param : params) {
            param = param.trim();
            if (pattern.matcher(param).matches()) {
                propertyKeyList.add(new PropertyKey(param));
            }
        }

    }

    private PathExpander<Object> buildPathExpander() {
        return PathExpanderBuilder.empty().add(relType, Direction.OUTGOING).build();
    }

    @Override
    public TraversalMetadata metadata() {
        return null;
    }

    private static class Metadata implements TraversalMetadata {
        private int rels;
        private int paths;

        @Override
        public int getNumberOfPathsReturned() {
            return paths;
        }

        @Override
        public int getNumberOfRelationshipsTraversed() {
            return rels;
        }
    }
}
