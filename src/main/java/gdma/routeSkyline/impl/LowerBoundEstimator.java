package gdma.routeSkyline.impl;

import org.neo4j.graphdb.Node;

public interface LowerBoundEstimator<T> {
    T getLowerBound(Node node);
}
