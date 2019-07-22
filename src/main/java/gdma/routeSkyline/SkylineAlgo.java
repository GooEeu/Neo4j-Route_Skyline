package gdma.routeSkyline;


import org.neo4j.graphdb.*;
import org.neo4j.graphdb.traversal.BranchState;
import org.neo4j.graphdb.traversal.Paths;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Mode;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;

import java.util.Objects;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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
    @Description("gdma.routeSkyline.stream(start, end, 'WAY', 'cost1; cost2;...') YIELD path, propertyKey, propertyValue")
    public Stream<SkylineResults> stream(@Name("startNode") org.neo4j.graphdb.Node startNode,
                                         @Name("endNode") Node endNode,
                                         @Name("relationshipType") String relType,
                                         @Name("params") String propertyKeys){

        if(Objects.isNull(propertyKeys)||propertyKeys==""){
            return Stream.<SkylineResults>empty();
        }

        return SkylineResults.streamPathResult(startNode,endNode,relType, propertyKeys.split(";"));
    }

    //@UserFunction
    @Procedure(name = "gdma.routeSkyline.expander",mode = Mode.READ)
    @Description("gdma.routeSkyline.expander(start, end, 'WAY') YIELD path, propertyKey, propertyValue")
    public Stream<RelationshipResult> expander(
            @Name("startNode") Node startNode,
            @Name("endNode") Node endNode,
            @Name("relationshipType") String relType){

        PathExpander pathExpander = PathExpanderBuilder.empty().add(RelationshipType.withName(relType),Direction.BOTH).build();
        Iterable<Relationship> it = pathExpander.expand(Paths.singleNodePath(startNode), BranchState.NO_STATE);
        return StreamSupport.stream(it.spliterator(), false).map(RelationshipResult::new);

    }

    public class RelationshipResult{
        public Relationship relationship;

        RelationshipResult(Relationship r){
            relationship=r;
        }
    }

}

