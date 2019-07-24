package gbma.routeSkyline;

//import static org.junit.Assert.assertEquals;

import gbma.TestDatabase;
import gdma.routeSkyline.SkylineAlgo;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.graphdb.GraphDatabaseService;

import java.util.Map;
import java.util.Objects;

class SkylineTest {

    private static final String SETUP_SAMPLE_GRAPH = "CREATE " +
            "(n0 {name:\"n0\"}), " +
            "(n1 {name:\"n1\"}), " +
            "(n2 {name:\"n2\"}), " +
            "(n3 {name:\"n3\"}), " +
            "(n4 {name:\"n4\"}), " +
            "(n5 {name:\"n5\"}), " +
            "(n6 {name:\"n6\"}), " +
            "(n0)-[:WAY { dist:7, time:1 }]->(n1), " +
//			"(n0)-[:WAY { dist:6, time:7 }]->(n2), " +
            "(n0)-[:WAY { dist:8, time:7 }]->(n3), " +
//			"(n0)-[:WAY { dist:1, time:1 }]->(n4), " +
            "(n0)-[:WAY { dist:7, time:4 }]->(n5), " +
            "(n0)-[:WAY { dist:8, time:18 }]->(n6), " +
            "(n1)-[:WAY { dist:4, time:8 }]->(n0), " +
//			"(n1)-[:WAY { dist:6, time:8 }]->(n2), " +
//			"(n1)-[:WAY { dist:7, time:4 }]->(n3), " +
//			"(n1)-[:WAY { dist:6, time:7 }]->(n4), " +
//			"(n1)-[:WAY { dist:9, time:8 }]->(n5), " +
//			"(n1)-[:WAY { dist:8, time:7 }]->(n6), " +
//			"(n2)-[:WAY { dist:9, time:2 }]->(n0), " +
            "(n2)-[:WAY { dist:5, time:3 }]->(n1), " +
//			"(n2)-[:WAY { dist:8, time:0 }]->(n3), " +
//			"(n2)-[:WAY { dist:5, time:9 }]->(n4), " +
//			"(n2)-[:WAY { dist:9, time:5 }]->(n5), " +
            "(n2)-[:WAY { dist:4, time:1 }]->(n6), " +
//			"(n3)-[:WAY { dist:4, time:5 }]->(n0), " +
//			"(n3)-[:WAY { dist:5, time:0 }]->(n1), " +
            "(n3)-[:WAY { dist:6, time:4 }]->(n2), " +
//			"(n3)-[:WAY { dist:5, time:8 }]->(n4), " +
            "(n3)-[:WAY { dist:6, time:3 }]->(n5), " +
//			"(n3)-[:WAY { dist:8, time:8 }]->(n6), " +
//			"(n4)-[:WAY { dist:2, time:3 }]->(n0), " +
//			"(n4)-[:WAY { dist:8, time:8 }]->(n1), " +
//			"(n4)-[:WAY { dist:2, time:9 }]->(n2), " +
            "(n4)-[:WAY { dist:1, time:5 }]->(n3), " +
//			"(n4)-[:WAY { dist:3, time:7 }]->(n5), " +
//			"(n4)-[:WAY { dist:8, time:7 }]->(n6), " +
//			"(n5)-[:WAY { dist:8, time:3 }]->(n0), " +
//			"(n5)-[:WAY { dist:4, time:4 }]->(n1), " +
            "(n5)-[:WAY { dist:8, time:3 }]->(n2), " +
//			"(n5)-[:WAY { dist:8, time:3 }]->(n3), " +
            "(n5)-[:WAY { dist:7, time:4 }]->(n4), " +
//			"(n5)-[:WAY { dist:8, time:7 }]->(n6), " +
//			"(n6)-[:WAY { dist:2, time:3 }]->(n0), " +
//			"(n6)-[:WAY { dist:3, time:5 }]->(n1), " +
//			"(n6)-[:WAY { dist:5, time:0 }]->(n2), " +
//			"(n6)-[:WAY { dist:7, time:7 }]->(n3), " +
//			"(n6)-[:WAY { dist:1, time:6 }]->(n4), " +
            "(n6)-[:WAY { dist:1, time:8 }]->(n5)";

    private static GraphDatabaseService db;

    @BeforeAll
    public static void setUp() {
        db = TestDatabase.getTestDatabase();
        TestDatabase.registerProcedure(db, SkylineAlgo.class);
        db.execute(SETUP_SAMPLE_GRAPH).close();
    }

    @AfterAll
    public static void tearDown() {
        db.shutdown();
    }

    @Test
    public void testSkyline() {
        TestDatabase.testAllPathsCall(db,
                "MATCH (from {name:'n0'}), (to {name:'n6'}) " +
                        "CALL gdma.routeSkyline.stream(from, to, 'WAY','time; dist') yield path " +
                        "RETURN path",
                row -> {
                    if (Objects.nonNull(row)) {
                        System.out.println(row.get("path").toString());
                    } else {
                        System.out.println("no path found");
                    }

                }
        );
    }

    @Test
    public void testSkylineWithWeight() {
        TestDatabase.testAllPathsCall(db,
                "MATCH (from {name:'n1'}), (to {name:'n6'}) " +
                        "CALL gdma.routeSkyline.stream(from, to, 'WAY','time; dist') yield path, weight " +
                        "RETURN path, weight",
                row -> {
                    if (Objects.nonNull(row)) {
                        System.out.println(row.get("path").toString());
                        try {
                            ((Map<String, Double>) row.get("weight")).forEach((k, v) -> {
                                System.out.print(k + ":" + v + "; ");
                            });
                            System.out.println();
                        } catch (ClassCastException e) {
                            e.printStackTrace();
                        }
                    } else {
                        System.out.println("no path found");
                    }

                }
        );
    }
}	