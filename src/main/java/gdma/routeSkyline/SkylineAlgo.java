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
     *
     * @param startNode
     * @param endNode
     * @param propertyKeys params for comparison with the ranking(DESC or ASC), default ASC.
     *               params not found in the edge will be considered as 0 by ASC,
     *                  and MAX by DESC
     *               params should be separated to the ranking by a space,
     *                  and to another param by a semicolon.
     */
    @Procedure(mode = Mode.READ)
    @Description("gdma.routeSkyline.stream(start, end, 'WAY', 'cost1; cost2;...') YIELD path, weight")
    public Stream<SkylineResults> stream(@Name("startNode") org.neo4j.graphdb.Node startNode,
                                         @Name("endNode") Node endNode,
                                         @Name("relationshipType") String relType,
                                         @Name("params") String propertyKeys){

        if(Objects.isNull(propertyKeys)||propertyKeys==""){
            return Stream.<SkylineResults>empty();
        }

        return SkylineResults.streamPathResult(startNode,endNode,relType, propertyKeys.split(";"));
    }

}

