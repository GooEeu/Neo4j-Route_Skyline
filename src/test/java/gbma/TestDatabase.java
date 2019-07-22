package gbma;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.internal.kernel.api.exceptions.KernelException;
import org.neo4j.kernel.impl.proc.Procedures;
import org.neo4j.kernel.internal.GraphDatabaseAPI;
import org.neo4j.test.TestGraphDatabaseFactory;

import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestDatabase {
	
	private static GraphDatabaseService testDB = null;
	
	public static GraphDatabaseService getTestDatabase() {
		if(testDB == null)
			testDB = new TestGraphDatabaseFactory().newImpermanentDatabase();
		return testDB;
    }
	
	public static void registerProcedure(GraphDatabaseService db, Class<?>...procedures) {
        Procedures proceduresService = ((GraphDatabaseAPI) db).getDependencyResolver().resolveDependency(Procedures.class);
        try {
        	for (Class<?> procedure : procedures) {
        		proceduresService.registerProcedure(procedure,true);
				proceduresService.registerFunction(procedure, true);
				proceduresService.registerAggregationFunction(procedure, true);
        	}
        } catch (KernelException e) {
			e.printStackTrace();
		}
    }
	
	public static void testDijkstraCall(GraphDatabaseService db, String call, Consumer<Map<String, Object>> consumer) {
        testResult(db, call, (res) -> {
            try {
                assertTrue(res.hasNext());
                Map<String, Object> row = res.next();
                consumer.accept(row);
                assertFalse(res.hasNext());
            } catch(Throwable t) {
                throw t;
            }
        });
    }
	
	public static void testAllPathsCall(GraphDatabaseService db, String call, Consumer<Map<String, Object>> consumer) {
        testResult(db, call, (res) -> {
        	while(res.hasNext()) {
        		try {
                	Map<String, Object> row = res.next();
                	consumer.accept(row);
            	} catch(Throwable t) {
                	throw t;
            	}
        	}
        });
    }
	
	private static void testResult(GraphDatabaseService db, String call, Consumer<Result> resultConsumer) {
		try (Transaction tx = db.beginTx()) {
            Map<String, Object> p = Collections.<String, Object>emptyMap();
            resultConsumer.accept(db.execute(call, p));
            tx.success();
        }
    }
}
