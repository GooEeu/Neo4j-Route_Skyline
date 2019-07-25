package gdma.routeSkyline;

import gdma.routeSkyline.impl.PropertyKey;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;

import java.util.Map;

public interface MultiWeightedPath extends Path {

    Map<PropertyKey, Double> getWeight();

    /**
     * attach a relationship at the end of the path
     *
     * @param relationship that has a startNode equal to the endNode of this MultiWeightedPath
     * @return a MultiWeightedPath that will attach the relationship at the end
     */
    MultiWeightedPath append(Relationship relationship);

}
