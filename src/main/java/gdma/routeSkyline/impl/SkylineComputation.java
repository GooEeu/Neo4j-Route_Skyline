package gdma.routeSkyline.impl;

import gdma.routeSkyline.MultiWeightedPath;
import org.neo4j.graphdb.Node;

import java.util.List;

public interface SkylineComputation {
    List<MultiWeightedPath> findAllPath(Node start, Node end);
}
