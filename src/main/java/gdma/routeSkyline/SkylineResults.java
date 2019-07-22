package gdma.routeSkyline;

import gdma.routeSkyline.impl.SkylinePathFinderImpl;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class SkylineResults {
    private static PathFinder<MultiWeightedPath> skylineAlgo;
    public Path path;

    private SkylineResults(Path path) {
        this.path = path;
    }

    public static Stream<SkylineResults> streamPathResult(Node startNode, Node endNode, String relType, String[] propertyKeys) {
        skylineAlgo=new SkylinePathFinderImpl(relType, propertyKeys);
        Iterable<? extends Path> allPaths =skylineAlgo.findAllPaths(startNode, endNode);
        return StreamSupport.stream(allPaths.spliterator(), false).map(SkylineResults::new);
    }

}
