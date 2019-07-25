package gdma.routeSkyline.impl;

import gdma.routeSkyline.LowerBoundEstimator;
import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphalgo.WeightedPath;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PathExpander;

import java.util.*;

import static org.neo4j.graphalgo.CommonEvaluators.doubleCostEvaluator;

public class LowerBoundEstimatorImplDijkstra implements LowerBoundEstimator<Map<PropertyKey, Double>> {
    private final Node endNode;
    private final Collection<PropertyKey> propertyKeyList;
    private final HashMap<Node, Map<PropertyKey, Double>> nodeToLowerBounds = new HashMap<>();
    private final Map<PropertyKey, PathFinder<WeightedPath>> pathFinderHashMap = new HashMap<>();
    private PathExpander pathExpander;

    LowerBoundEstimatorImplDijkstra(Collection<PropertyKey> propertyKeyList, PathExpander pathExpander, Node startNode, Node endNode) {
        this.propertyKeyList = propertyKeyList;
        this.pathExpander = pathExpander.reverse();
        this.endNode = endNode;
        buildPathFinderHashMap();
    }

    @Override
    public Map<PropertyKey, Double> getLowerBound(Node node) {
        Map<PropertyKey, Double> map = nodeToLowerBounds.get(node);
        if (!Objects.isNull(map)) {
            return map;
        }
        map = new HashMap<>((int) (propertyKeyList.size() * 1.34) + 1);

        for (Map.Entry<PropertyKey, PathFinder<WeightedPath>> entry : pathFinderHashMap.entrySet()) {
            WeightedPath path = entry.getValue().findSinglePath(endNode, node);
            Double weight = Objects.isNull(path) ? (double) Integer.MAX_VALUE : path.weight();
            map.put(entry.getKey(), weight);
        }

        nodeToLowerBounds.put(node, map);

        return Collections.unmodifiableMap(map);
    }

    private void buildPathFinderHashMap() {
        for (PropertyKey propertyKey : propertyKeyList) {
            PathFinder<WeightedPath> pathFinder = GraphAlgoFactory.dijkstra(pathExpander,
                    doubleCostEvaluator(propertyKey.NAME));

            pathFinderHashMap.put(propertyKey, pathFinder);
        }
    }
}
