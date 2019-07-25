package gdma.routeSkyline.impl;

import gdma.routeSkyline.LowerBoundEstimator;
import gdma.routeSkyline.MultiWeightedPath;
import gdma.routeSkyline.SkylineComputation;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.PathExpander;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.traversal.BranchState;
import org.neo4j.kernel.impl.util.NoneStrictMath;

import java.util.*;

public class SkylineComputationImplARSC implements SkylineComputation {
    private final List<PropertyKey> propertyKeyList;
    private final LowerBoundEstimator<Map<PropertyKey, Double>> estimator;
    private PathExpander pathExpander;

    SkylineComputationImplARSC(List<PropertyKey> propertyKeyList,
                               PathExpander expander,
                               LowerBoundEstimator<Map<PropertyKey, Double>> estimator) {
        this.propertyKeyList = propertyKeyList;
        this.pathExpander = expander;
        this.estimator = estimator;

    }

    public List<MultiWeightedPath> findAllSkylinePath(Node startNode, Node endNode) {
        SkylineComparator skylineComparator = new SkylineComparator(propertyKeyList);

        //closer to target, higher priority
        Comparator<Node> nodePriorityComparator = new Comparator<Node>() {
            private SkylineComparator.PropertyComparator propertyComparator = skylineComparator.getPropertyComparator();

            @Override
            public int compare(Node o1, Node o2) {
                return (propertyComparator.compare(estimator.getLowerBound(o1), estimator.getLowerBound(o2)));
            }
        };
        PriorityQueue<Node> qnode = new PriorityQueue<>(nodePriorityComparator);
        List<MultiWeightedPath> skylineRoutes = new LinkedList<>();
        Map<Node, List<MultiWeightedPath>> subRoutesMap = new HashMap<>();
        Set<Path> flag = new HashSet<>();

        qnode.add(startNode);
        List<MultiWeightedPath> subRoutes = new LinkedList<>();
        subRoutes.add(new MultiWeightedPathImpl(startNode, propertyKeyList));
        subRoutesMap.put(startNode, subRoutes);

        while (!qnode.isEmpty()) {
            Node vi = qnode.poll();

            List<MultiWeightedPath> subroutes = subRoutesMap.get(vi);

            Iterator<MultiWeightedPath> subroutesIterator = subroutes.iterator();
            while (subroutesIterator.hasNext()) {
                MultiWeightedPath subroute = subroutesIterator.next();
                Node subrouteNode = subroute.endNode();
                Map<PropertyKey, Double> lowerbound = estimator.getLowerBound(subrouteNode);

                boolean dominated = isDominated(skylineRoutes, subroute, lowerbound);

                if (dominated) {
                    subroutesIterator.remove();
                } else {
                    if (flag.contains(subroute)) {
                        continue;
                    }
                    flag.add(subroute);

                    for (Relationship relationship : (Iterable<Relationship>) pathExpander.expand(subroute, BranchState.NO_STATE)) {
                        MultiWeightedPath p1 = subroute.append(relationship);


                        if (endNode.equals(p1.endNode())) {
                            if (!isDominated(p1, skylineRoutes)) {
                                updateSkylineCollection(p1, skylineRoutes);
                            }
                        } else {
                            Node vnext = p1.endNode();
                            List<MultiWeightedPath> nextSubRoutes = subRoutesMap.get(vnext);
                            if (Objects.isNull(nextSubRoutes)) {
                                nextSubRoutes = new LinkedList<>();
                                subRoutesMap.put(vnext, nextSubRoutes);
                            }
                            dominated = isDominated(p1, nextSubRoutes);
                            if (!dominated) {
                                updateSkylineCollection(p1, nextSubRoutes);
                            }

                            if (!qnode.contains(vnext)) {
                                qnode.add(vnext);
                            }
                        }
                    }
                }
            }
        }
        return skylineRoutes;
    }

    private void updateSkylineCollection(MultiWeightedPath path, Collection<MultiWeightedPath> collection) {
        collection.removeIf(toBeComparedRoute -> isDominated(path, toBeComparedRoute, null));
        collection.add(path);
    }

    private boolean isDominated(MultiWeightedPath subroute, Collection<MultiWeightedPath> skylineroutes) {
        return skylineroutes.parallelStream()
                .anyMatch(skylineRoute -> isDominated(skylineRoute, subroute, null));
    }

    private boolean isDominated(Collection<MultiWeightedPath> skylineroutes, MultiWeightedPath subroute, Map<PropertyKey, Double> lowerbound) {
        return skylineroutes.parallelStream()
                .anyMatch(skylineRoute -> isDominated(skylineRoute, subroute, lowerbound));
    }

    /**
     * @param skylineRoute      the "already-inserted" route
     * @param toBeComparedRoute the route to be compared
     * @return if the <code>toBeComparedRoute</code> is dominated by the <code>skylineRoute</code>
     */
    private boolean isDominated(MultiWeightedPath skylineRoute, MultiWeightedPath toBeComparedRoute, Map<PropertyKey, Double> lowerbound) {
        Map<PropertyKey, Double> skylineRouteWeight = skylineRoute.getWeight();
        Map<PropertyKey, Double> weight = toBeComparedRoute.getWeight();
        return weight.entrySet().parallelStream().allMatch(weightEntry -> {
            Double lowerBoundValue;
            if (Objects.nonNull(lowerbound)) {
                lowerBoundValue = lowerbound.get(weightEntry.getKey());
                if (Objects.isNull(lowerBoundValue)) {
                    lowerBoundValue = 0.0;
                }
            } else {
                lowerBoundValue = 0.0;
            }
            return (NoneStrictMath.compare(skylineRouteWeight.get(weightEntry.getKey()),
                    (weightEntry.getValue() + lowerBoundValue)
            ) < 0);
        });
    }

    public class SkylineComparator implements Comparator<MultiWeightedPathImpl> {
        //final Map<Node, Map<PropertyKey,Double>> weights;
        final PropertyComparator propertyComparator;

        SkylineComparator(Collection<PropertyKey> propertyKeyList) {
            //this.weights=weights;
            propertyComparator = new PropertyComparator(propertyKeyList);
        }

        @Override
        public int compare(MultiWeightedPathImpl p1, MultiWeightedPathImpl p2) {
            return propertyComparator.compare(p1.getWeight(), p2.getWeight());
        }

        public int compareWithLowerBound(MultiWeightedPathImpl p1, MultiWeightedPathImpl p2, Map<PropertyKey, Double> p2LowerBound) {
            return propertyComparator.compareWithLowerBound(p1.getWeight(), p2.getWeight(), p2LowerBound);
        }

        public PropertyComparator getPropertyComparator() {
            return propertyComparator;
        }

        class PropertyComparator implements Comparator<Map<PropertyKey, Double>> {
            final Collection<PropertyKey> propertyKeyList;
            double EPSILON;

            PropertyComparator(Collection<PropertyKey> propertyKeyList) {
                this(propertyKeyList, NoneStrictMath.EPSILON);
            }

            PropertyComparator(Collection<PropertyKey> propertyKeyList, Double epsilon) {
                this.propertyKeyList = propertyKeyList;
                EPSILON = epsilon;

            }

            @Override
            public int compare(Map<PropertyKey, Double> first, Map<PropertyKey, Double> second) {
                return compareWithLowerBound(first, second, null);
            }

            int compareWithLowerBound(Map<PropertyKey, Double> first, Map<PropertyKey, Double> second, Map<PropertyKey, Double> secondLowerBound) {

                if (Objects.isNull(first) || Objects.isNull(second)) {
                    throw new NullPointerException(Objects.isNull(first) ? "first" : "last");
                }

                int compare;
                boolean lowerBound = Objects.nonNull(secondLowerBound);

                for (PropertyKey propertyKey : propertyKeyList) {
                    Double firstValue = first.get(propertyKey);
                    Double secondValue = second.get(propertyKey);
                    Double secondLowerBoundValue = lowerBound ? secondLowerBound.get(propertyKey) : 0.0;
                    if (Objects.isNull(firstValue)) {
                        firstValue = 0.0;
                    }
                    if (Objects.isNull(secondValue)) {
                        secondValue = 0.0;
                    }
                    if (Objects.isNull(secondLowerBoundValue)) {
                        secondLowerBoundValue = 0.0;
                    }

                    compare = NoneStrictMath.compare(firstValue, secondValue + secondLowerBoundValue, EPSILON);
                    if (compare != 0) {
                        return compare;
                    }
                }
                return 0;
            }
        }

    }


}
