package gdma.routeSkyline;


import org.neo4j.graphdb.Node;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Mode;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;

import java.util.Objects;
import java.util.stream.Stream;

// import Neo4j Apoc Procedures

public class SkylineAlgo {

    /**
     * @param startNode    the startNode(aka sourceNode)
     * @param endNode      the startNode(aka targetNode)
     * @param relType      the relation type to traverse
     * @param propertyKeys the key of the property, can be multiple, divide by semicolon
     * @return a collection of paths and weights(Map between propertyKey and weight),
     * where each of them has at least one weight of property that is no worse than any other path.
     */
    @Procedure(mode = Mode.READ)
    @Description("gdma.routeSkyline.stream(start, end, 'WAY', 'cost1; cost2;...') YIELD path, weight")
    public Stream<SkylineResults> stream(@Name("startNode") org.neo4j.graphdb.Node startNode,
                                         @Name("endNode") Node endNode,
                                         @Name("relationshipType") String relType,
                                         @Name("params") String propertyKeys) {

        if (Objects.isNull(propertyKeys) || Objects.equals(propertyKeys, "")) {
            return Stream.empty();
        }

        return SkylineResults.streamPathResult(startNode, endNode, relType, propertyKeys.split(";"));
    }

}

