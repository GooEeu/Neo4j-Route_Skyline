package gdma.routeSkyline;

import org.neo4j.graphdb.Node;

import java.util.List;

public interface SkylineComputation {
    List<MultiWeightedPath> findAllSkylinePath(Node start, Node end);
}
