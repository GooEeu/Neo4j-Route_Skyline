package gdma.routeSkyline;

import org.neo4j.graphdb.Node;

public interface LowerBoundEstimator<T> {
    T getLowerBound(Node node);
}
