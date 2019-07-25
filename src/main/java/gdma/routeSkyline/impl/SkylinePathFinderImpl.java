package gdma.routeSkyline.impl;

import gdma.routeSkyline.LowerBoundEstimator;
import gdma.routeSkyline.MultiWeightedPath;
import gdma.routeSkyline.SkylineComputation;
import gdma.routeSkyline.SkylinePathFinder;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.traversal.TraversalMetadata;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class SkylinePathFinderImpl implements SkylinePathFinder {
    private final static List<PropertyKey> propertyKeyList = new LinkedList<>();
    private static Pattern pattern = Pattern.compile("^(\\w*)$");

    private final PathExpander pathExpander;
    private final RelationshipType relType;
    //Don't know what exactly MetaData do
    private TraversalMetadata lastMetadata;

    public SkylinePathFinderImpl(String relType, String[] propertyKeys) {
        this.relType = RelationshipType.withName(relType);

        generatePropertyKeySet(propertyKeys);
        pathExpander = buildPathExpander();
        lastMetadata = new Metadata();
    }


    @Override
    public MultiWeightedPath findSinglePath(Node start, Node end) {

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

    private PathExpander buildPathExpander() {
        return PathExpanderBuilder.empty().add(relType, Direction.OUTGOING).build();
    }

    @Override
    public TraversalMetadata metadata() {
        // TODO: 19.07.25 check how to write the Metadata
        return lastMetadata;
    }

    private static class Metadata implements TraversalMetadata {
        int rels;
        int paths;

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
