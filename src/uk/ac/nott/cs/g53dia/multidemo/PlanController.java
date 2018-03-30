package uk.ac.nott.cs.g53dia.multidemo;

import uk.ac.nott.cs.g53dia.multilibrary.Station;
import uk.ac.nott.cs.g53dia.multilibrary.Tanker;

import java.util.Deque;
import java.util.Hashtable;
import java.util.List;

/**
 * Agent control subsystem that monitors current plan/moveset and ensure agent necessities are satisfied.
 */
public class PlanController extends Planner {
    public static final int EXPLORE = 0;
    public static final int CLUSTER_PLANNING = 1;
    public static final int LOCAL_PLANNING = 2;
    public static final int REPAIR_PLAN = 3;

    private static final int NO_ERROR = Integer.MAX_VALUE;
    private static final int INSUFFICIENT_FUEL = -1;
    private static final int INSUFFICIENT_WASTE_CAPACITY = -2;
    private static final int CONFLICT_WITH_OTHER_TANKER = -3;
    private static final int NO_WASTE_IN_STATION = -4;
    private static final int HAS_FULL_FUEL = -5;
    private static final int HAS_NO_WASTE_IN_TANKER = -6;
    private static final int SHOULD_BE_PLANNING = -7;
    private static final int UNKNOWN_ENTITY_ERROR = -8;
    private static final int EMPTY_MOVESET = -9;
    private static final int CONTINUE_EXPLORE = -10;
    private static final int UNREACHABLE_ENTITY = -11;

    private int moveStatus;
    private int previousPlan;
    private int override;

    PlanController() {
        moveStatus = NO_ERROR;
        previousPlan = Integer.MIN_VALUE;
        override = Integer.MIN_VALUE;
    }

    @Override
    public Deque<CoreEntity> plan(MapBuilder map, ClusterMapBuilder clustermap, Hashtable<Integer, List<CoreEntity>> entities,
                                  Deque<CoreEntity> moves, TankerCoordinator tc, long timestep) {
        CoreEntity displacedEntity;

        switch (moveStatus) {
            case NO_WASTE_IN_STATION:
            case HAS_FULL_FUEL:
            case HAS_NO_WASTE_IN_TANKER:
            case UNKNOWN_ENTITY_ERROR:
            case UNREACHABLE_ENTITY:
                displacedEntity = moves.removeFirst();
                DemoFleet.mapper.unsetTankerMoveTowardsEntity(displacedEntity);
                break;

            case INSUFFICIENT_FUEL:
                displacedEntity = tc.getClosestObservableFuelpump();
                if(!super.acceptableFuelLevel(tc.getFuelLevel(), tc.getTankerCoordinate(), displacedEntity.getCoord())) {
                    displacedEntity = getClosestEntityTo(map.getEntityMap(EntityChecker.FUELPUMP), tc.getEntityUnderTanker());
                }
                if(!EntityChecker.isFuelPump(moves.peekFirst().getEntity())) {
                    moves.addFirst(displacedEntity);
                }
                else if(EntityChecker.isFuelPump(moves.peekFirst().getEntity())) {
                    if(!super.acceptableFuelLevel(tc.getFuelLevel(), tc.getTankerCoordinate(), moves.peekFirst().getCoord())) {
                        moves.removeFirst();
                        moves.addFirst(displacedEntity);
                    }
                }
                break;

            case INSUFFICIENT_WASTE_CAPACITY:
                if(tc.getClosestObservableWell() != null) {
                    displacedEntity = tc.getClosestObservableWell();
                    moves.addFirst(displacedEntity);
                }
                else {
                    displacedEntity = getClosestEntityTo(map.getEntityMap(EntityChecker.WELL), tc.getEntityUnderTanker());
                    if(displacedEntity != null) {
                        if(!super.acceptableFuelLevel(tc.getFuelLevel(), distAtoCviaB(tc.getEntityUnderTanker(), displacedEntity, tc.getClosestObservableFuelpump()))) {
                            moves.addFirst(tc.getClosestObservableFuelpump());
                        }
                        else {
                            moves.addFirst(displacedEntity);
                        }
                    }
                    else {
                        override = EXPLORE;
                    }
                }
                break;

                default:
                    override = EXPLORE;
                    break;
        }

        return null;
    }

    public boolean allowMoveset(Deque<CoreEntity> moves, Hashtable<Integer, List<CoreEntity>> entities, TankerCoordinator tc, long timestep) {
        moveStatus = NO_ERROR;
        if (moves.isEmpty()) {
            moveStatus = EMPTY_MOVESET;
            return false;
        }
        else if(override == EXPLORE) {
            return true;
        }
        if (moves.peekFirst().isDirectionalEntity()) {
                if (!super.acceptableFuelLevel(tc.getFuelLevel(), tc.getTankerCoordinate(), tc.getClosestObservableFuelpump().getCoord())) {
                    moveStatus = INSUFFICIENT_FUEL;
                    return false;
                } else if (!super.acceptableWasteLevel(tc.getWasteLevel())) {
                    moveStatus = INSUFFICIENT_WASTE_CAPACITY;
                    return false;
                } else if (timestep <= 300) {
                    moveStatus = CONTINUE_EXPLORE;
                    return true;
                } else {
                    moveStatus = SHOULD_BE_PLANNING;
                    return false;
                }
        } else {
            if (allowAddMove(moves, EntityChecker.FUELPUMP, false) && !super.acceptableFuelLevel(tc.getFuelLevel(), tc.getTankerCoordinate(), tc.getClosestObservableFuelpump().getCoord())) {
                moveStatus = INSUFFICIENT_FUEL;
                return false;
            }
            else if(!EntityChecker.isFuelPump(moves.peekFirst().getEntity()) &&
                    !super.acceptableFuelLevel(tc.getFuelLevel(), tc.getTankerCoordinate(), moves.peekFirst().getCoord())) {
                moveStatus = UNREACHABLE_ENTITY;
                return false;
            }
            if (EntityChecker.isFuelPump(moves.peekFirst().getEntity())) {
                if (tc.getFuelLevel() == Tanker.MAX_FUEL && !tc.isMovingTowardsCluster()) {
                    moveStatus = HAS_FULL_FUEL;
                    return false;
                }
            } else if (EntityChecker.isWell(moves.peekFirst().getEntity())) {
                if (tc.getWasteLevel() <= 500) {
                    moveStatus = HAS_NO_WASTE_IN_TANKER;
                    return false;
                }
            } else if (EntityChecker.isStation(moves.peekFirst().getEntity())) {
                if (((Station) moves.peekFirst().getEntity()).getTask() == null) {
                    moveStatus = NO_WASTE_IN_STATION;
                    return false;
                } else {
                    int ind = entities.get(EntityChecker.TASKEDSTATION).indexOf(moves.peekFirst());
                    if (ind != -1 && ((Station) entities.get(EntityChecker.TASKEDSTATION).get(ind).getEntity()).getTask() == null) {
                        moveStatus = NO_WASTE_IN_STATION;
                        return false;
                    } else {
                        if (!super.acceptableFuelLevel(tc.getFuelLevel(), tc.getTankerCoordinate().distanceToCoordinate(tc.getClosestObservableFuelpump().getCoord()))) {
                            moveStatus = UNREACHABLE_ENTITY;
                            return false;
                        }
                        else if (!super.acceptableWasteLevel(tc.getWasteLevel())) {
                            moveStatus = INSUFFICIENT_WASTE_CAPACITY;
                            return false;
                        }
                    }
                }
            } else if (EntityChecker.isEmptyCell(moves.peekFirst().getEntity())) {
                moveStatus = UNKNOWN_ENTITY_ERROR;
                return false;
            }
        }

        moveStatus = NO_ERROR;
        return true;
    }

    public int decidePlan(Deque<CoreEntity> moves, ClusterMapBuilder clustermap, Hashtable<Integer, List<CoreEntity>> entities, TankerCoordinator tc, long timestep) {
        int currentPlan = Integer.MIN_VALUE;
        switch (moveStatus) {
            case INSUFFICIENT_FUEL:
            case INSUFFICIENT_WASTE_CAPACITY:
            case NO_WASTE_IN_STATION:
            case HAS_FULL_FUEL:
            case HAS_NO_WASTE_IN_TANKER:
            case UNKNOWN_ENTITY_ERROR:
            case UNREACHABLE_ENTITY:
                currentPlan = REPAIR_PLAN;
                break;

            case NO_ERROR:
                if(!moves.isEmpty() && !moves.peekFirst().isDirectionalEntity()) {
                    break;
                }
            case SHOULD_BE_PLANNING:
            case EMPTY_MOVESET:
                if (!entities.get(EntityChecker.FUELPUMP).isEmpty() && !entities.get(EntityChecker.WELL).isEmpty() &&
                        !entities.get(EntityChecker.TASKEDSTATION).isEmpty()) {
                    currentPlan = LOCAL_PLANNING;
                } else if (clustermap.getClusterMap().size() >= 1) {
                    List<ClusterEntity> clusters = clustermap.getClusterMap();
                    for (ClusterEntity cluster : clusters) {
                        if (!cluster.getHasTankerMoveTowards()) {
                            currentPlan = CLUSTER_PLANNING;
                            break;
                        }
                    }
                    if (currentPlan != CLUSTER_PLANNING) {
                        currentPlan = EXPLORE;
                    }
                } else {
                    currentPlan = EXPLORE;
                }
                break;

            case CONTINUE_EXPLORE:
                currentPlan = EXPLORE;
                break;

            default:
                override = EXPLORE;
                break;
        }

        if(currentPlan != REPAIR_PLAN && currentPlan == previousPlan || override == EXPLORE) {
            currentPlan = EXPLORE;
        }
        previousPlan = currentPlan;

        return currentPlan;
    }

    public void managedPlan() {
        previousPlan = Integer.MIN_VALUE;
        override = Integer.MIN_VALUE;
        moveStatus = NO_ERROR;
    }

    @Override
    public String toString() {
        return "Previous plan: " + previousPlan + "\nmoveStatus: " + moveStatus + "\nOverride: " + override;
    }
}
