package uk.ac.nott.cs.g53dia.multidemo;

import uk.ac.nott.cs.g53dia.multilibrary.Tanker;

import java.util.*;

/**
 * All planning essentials
 */
public abstract class Planner {
    protected static final int DJIKSTRA_ALGORITHM = 0;
    protected static final int ASTAR_ALGORITHM = 1;
    protected final FScoreCompare fscorecompare = new FScoreCompare();

    /**
     * Core planning function the tanker makes
     *
     * @param map        All entities observed by the tanker
     * @param clustermap
     * @param tc         Tanker coordinator
     * @param timestep   The current timestep
     * @return A sequence of moves which has been planned
     */
    public abstract Deque<CoreEntity> plan(MapBuilder map, ClusterMapBuilder clustermap,
                                           Hashtable<Integer, List<CoreEntity>> entities, Deque<CoreEntity> moves,
                                           TankerCoordinator tc, long timestep);

    protected boolean acceptableFuelLevel(int estFuelLevel, int distToFuelPump) {
        return estFuelLevel > distToFuelPump + Threshold.REFUEL_ERROR_MARGIN.getThreshold();
    }

    protected boolean acceptableFuelLevel(int estFuelLevel, Coordinates tankerCoordinate, Coordinates fuelpump) {
        return acceptableFuelLevel(estFuelLevel, tankerCoordinate.distanceToCoordinate(fuelpump));
    }

    protected boolean acceptableWasteLevel(int estWasteLevel) {
        return !Threshold.HIGHEST_WASTE.hitThreshold(estWasteLevel);
    }

    protected int getClosestEntityDistanceTo(List<CoreEntity> desiredEntity, CoreEntity current) {
        if (desiredEntity.isEmpty())
            return Integer.MIN_VALUE;
        else if (desiredEntity.size() == 1)
            return Calculation.diagonalDistance(current.getCoord(), desiredEntity.get(0).getCoord());

        int min = 0;
        int argmin = 0;
        int[] dist = new int[desiredEntity.size()];
        for (int i = 0; i < desiredEntity.size(); i++) {
            dist[i] = Calculation.diagonalDistance(current.getCoord(), desiredEntity.get(i).getCoord());
            if (i == 0)
                min = dist[i];
            else if (dist[i] > min) {
                argmin = i;
                min = dist[i];
            }
        }

        return dist[argmin];
    }

    protected CoreEntity getClosestEntityTo(List<CoreEntity> desiredEntity, CoreEntity current) {
        if (desiredEntity.isEmpty())
            return null;
        else if (desiredEntity.size() == 1)
            return desiredEntity.get(0);

        int min = Integer.MAX_VALUE;
        int argmin = -1;
        for (int i = 0; i < desiredEntity.size(); i++) {
            int dist = Calculation.diagonalDistance(current.getCoord(), desiredEntity.get(i).getCoord());
            if (dist < min) {
                min = dist;
                argmin = i;
            }
        }

        return argmin >= 0 ? desiredEntity.get(argmin) : null;
    }

    protected int distAtoCviaB(CoreEntity a, CoreEntity b, CoreEntity c) {
        return a.getCoord().distanceToCoordinate(b.getCoord()) + b.getCoord().distanceToCoordinate(c.getCoord());
    }

    protected boolean allowAddMove(Deque<CoreEntity> moves, int entityType) {
        if (moves.isEmpty()) {
            return true;
        }
        if (moves.peekFirst().isDirectionalEntity() || moves.peekLast().isDirectionalEntity()) {
            if(EntityChecker.DUMMY == entityType) {
                return false;
            }
            return true;
        }
        else {
            if(EntityChecker.getEntityType(moves.peekLast().getEntity(), true) == entityType) {
                return false;
            }
            return true;
        }
    }

    protected List<EntityNode> calculateFScore(CoreEntity source, List<CoreEntity> targets, int algorithm, int tankerWasteLevel) {
        List<EntityNode> fscoredNodes = new ArrayList<>(targets.size());
        EntityNode src = (EntityNode) source;

        // safe init
        src.setGscore(0);
        src.setHscore(0);
        src.setWeight(1);
        src.calculateFscore();

        for (CoreEntity target : targets) {
            EntityNode t = (EntityNode) target;
            int dist = source.getCoord().distanceToCoordinate(t.getCoord());
            t.setFuelConsumption(dist);
            t.setGscore(src.getGscore() + dist);
            switch (algorithm) {
                case DJIKSTRA_ALGORITHM:
                    t.setHscore(0);
                    break;
                case ASTAR_ALGORITHM:
                    if(t.hasTask()) {
                        t.setHscore(t.getWasteRemaining());
                        int offset = tankerWasteLevel + t.getWasteRemaining() - Tanker.MAX_WASTE;
                        if(offset <= 0) {
                            t.setWeight(1 + (offset * 0.001));
                        }
                        else {
                            double weight = 1 - (offset * 0.001);
                            if(weight < (double) 0) {
                                weight = 0;
                            }
                            t.setWeight(weight);
                        }
                    }
                    else {
                        t.setHscore(0); // Fuelpumps and wells default to Djikstra
                    }
                    break;
                default:
                    throw new IllegalArgumentException("(Planner) Invalid algorithm selected!");
            }
            t.calculateFscore();
            t.setParent(src);
            fscoredNodes.add(t);
        }

        return fscoredNodes;
    }

    protected List<CoreEntity> sourceToDestination(CoreEntity source, CoreEntity destination, List<CoreEntity> entities, int algorithm,
                                                   int tankerFuelLevel, int tankerWasteLevel) {
        List<EntityNode> path = new ArrayList<>();
        List<CoreEntity> nominee = new ArrayList<>(entities);
        EntityNode src = (EntityNode) source;
        EntityNode dest = (EntityNode) destination;
        int estFuelLevel = tankerFuelLevel;
        int estWasteLevel = tankerWasteLevel;

        // Safe init
        src.setGscore(0);
        src.setHscore(0);
        src.setWeight(1);
        src.calculateFscore();
        path.add(src);

        while(src.getEntityHash() != dest.getEntityHash()) {
            List<EntityNode> fscoredNodes = new ArrayList<>();
            for (CoreEntity target : nominee) {
                EntityNode t = (EntityNode) target;
                int dist = source.getCoord().distanceToCoordinate(t.getCoord());
                t.setFuelConsumption(dist);
                t.setGscore(src.getGscore() + dist);
                switch (algorithm) {
                    case DJIKSTRA_ALGORITHM:
                        t.setHscore(0);
                        break;
                    case ASTAR_ALGORITHM:
                        t.setHscore(source.getCoord().distanceToCoordinate(destination.getCoord()));
                        break;
                    default:
                        throw new IllegalArgumentException("(Planner) Invalid algorithm selected!");
                }
                t.calculateFscore();
                t.setParent(src);
                fscoredNodes.add(t);
            }

            fscoredNodes = filterFeasibleNodes(fscoredNodes, estFuelLevel);
            if(fscoredNodes.isEmpty()) {
                return new ArrayList<>();
            }
            else {
                EntityNode minNode = Collections.min(fscoredNodes, fscorecompare);
                path.add(minNode);
                source = minNode;
                nominee.remove(minNode);

                estFuelLevel -= minNode.getFuelConsumption();
                if(EntityChecker.isFuelPump(minNode.getEntity())) {
                    estFuelLevel = 100;
                }
            }
        }

        return new ArrayList<>(path);
    }

    protected List<EntityNode> filterFeasibleNodes(List<EntityNode> fscorednodes, int tankerFuelLevel) {
        List<EntityNode> feasibleFscoredNodes = new ArrayList<>(fscorednodes.size());

        for(EntityNode node : fscorednodes) {
            if(acceptableFuelLevel(tankerFuelLevel, node.getFuelConsumption())) {
                feasibleFscoredNodes.add(node);
            }
        }

        return feasibleFscoredNodes;
    }
}


class FScoreCompare implements Comparator<EntityNode> {

    @Override
    public int compare(EntityNode o1, EntityNode o2) {
        return Integer.compare(o1.getFscore(), o2.getFscore());
    }
}
