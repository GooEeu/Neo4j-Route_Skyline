package gdma.routeSkyline;

import gdma.routeSkyline.impl.SkylinePathFinderImpl;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class SkylineResults {
    public final Path path;
    public final Map<String, Double> weight;

    private SkylineResults(MultiWeightedPath path) {
        this.path = path;
        weight = new HashMap<>();
        path.getWeight().forEach((propertyKey, cost) -> weight.put(propertyKey.NAME, cost));
    }

    static Stream<SkylineResults> streamPathResult(Node startNode, Node endNode, String relType, String[] propertyKeys) {
        PathFinder<MultiWeightedPath> skylineAlgo = new SkylinePathFinderImpl(relType, propertyKeys);
        return StreamSupport.stream(skylineAlgo.findAllPaths(startNode, endNode).spliterator(), false).map(SkylineResults::new);
    }

}
