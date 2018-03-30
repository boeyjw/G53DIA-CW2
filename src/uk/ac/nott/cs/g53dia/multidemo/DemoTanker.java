package uk.ac.nott.cs.g53dia.multidemo;

import uk.ac.nott.cs.g53dia.multilibrary.*;

import java.util.*;

/**
 * A simple example Tanker
 *
 * @author Julian Zappala
 */
/*
 *
 * Copyright (c) 2011 Julian Zappala
 *
 * See the file "license.terms" for information on usage and redistribution
 * of this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
public class DemoTanker extends Tanker {
    // Debugging utility object
    private Log l;

    // Tanker identification
    private TankerCoordinator tc;

    // Entity objects
    private Hashtable<Integer, List<CoreEntity>> entities;
    private List<CoreEntity> fuelpump, well, station, taskedStation;
    private Deque<CoreEntity> moves;
    private List<CoreEntity> history;

    private Explorer explorer;
    private Planner singleplanner;
    private PlanController plancontroller;

//    public DemoTanker() {
//        this(new Random());
//    }

    public DemoTanker(Random r, int tankerID) {
        this.r = r;
        l = new Log(true);

        tc = new TankerCoordinator(this, tankerID);
        DemoFleet.allTankers.put(tankerID, tc);

        entities = new Hashtable<>();
        fuelpump = new ArrayList<>();
        well = new ArrayList<>();
        station = new ArrayList<>();
        taskedStation = new ArrayList<>();
        moves = new ArrayDeque<>();
        history = new ArrayList<>();

        explorer = new Explorer();
        plancontroller = new PlanController();
        singleplanner = new SinglePlanner();

        moves.addFirst(explorer.startUpDirection());
        DemoFleet.explorationDirection.put(tc.getTankerID(), moves.peekFirst().getBearing());
    }

    public Action senseAndAct(Cell[][] view, long timestep) {
        l.d("");
        l.d("Timestep: " + timestep + "\t" + "TankerID: " + tc.getTankerID());
        // Init
        cleanup();
        tc.checkActionFailed(actionFailed, moves, getCurrentCell(view), this, history.isEmpty() ? null : history.get(history.size() - 1));
        tc.setTankerStatus(this, new EntityNode(getCurrentCell(view), tc.getTankerCoordinate(), timestep),
                moves.isEmpty() ? TankerCoordinator.NOACTION : tc.setCurrentAction(moves.peekFirst()), timestep);
        DemoFleet.history.get(tc.getTankerID()).add(getCurrentCell(view).getPoint().toString());
        if (moves.peekFirst().isDirectionalEntity()) {
            DemoFleet.explorationDirection.put(tc.getTankerID(), moves.peekFirst().getBearing());
        }
        else if (Calculation.targetBearing(tc.getTankerCoordinate(), moves.peekFirst().getCoord()) != Calculation.ONTANKER) {
            DemoFleet.explorationDirection.put(tc.getTankerID(), Calculation.targetBearing(tc.getTankerCoordinate(), moves.peekFirst().getCoord()));
            if(EntityChecker.isFuelPump(moves.peekFirst().getEntity())) {
                tc.setMovingTowardsCluster();
                DemoFleet.clustermap.setLastVisitedCluster(moves.peekFirst(), timestep);
            }
        }

        // Scan stage
        spiralScanView(view, timestep);
        tc.setClosestFuelWell(
                fuelpump.isEmpty() ? null : fuelpump.get(fuelpump.size() - 1),
                well.isEmpty() ? null : well.get(well.size() - 1)
        );
        int clusterStatus = DemoFleet.clustermap.buildCluster(DemoFleet.mapper.getMap());
        System.out.println("Cluster size: " + DemoFleet.clustermap.getClusterMap().size());

        boolean isExplore = false;
        explorer.plan(DemoFleet.mapper, DemoFleet.clustermap, entities, moves, tc, timestep);
        while (!plancontroller.allowMoveset(moves, entities, tc, timestep) && !isExplore) {
            int planType = plancontroller.decidePlan(moves, DemoFleet.clustermap, entities, tc, timestep);
            switch (planType) {
                case PlanController.EXPLORE:
                    isExplore = true;
                    explorer.plan(DemoFleet.mapper, DemoFleet.clustermap, entities, moves, tc, timestep);
                    break;
                case PlanController.CLUSTER_PLANNING:
                    DemoFleet.multiplanner.plan(DemoFleet.mapper, DemoFleet.clustermap, entities, moves, tc, timestep);
                    if(!moves.isEmpty() && moves.peekFirst().isDirectionalEntity()) {
                        moves.removeFirst();
                    }
                    break;
                case PlanController.LOCAL_PLANNING:
                    singleplanner.plan(DemoFleet.mapper, DemoFleet.clustermap, entities, moves, tc, timestep);
                    if(!moves.isEmpty() && moves.peekFirst().isDirectionalEntity()) {
                        moves.removeFirst();
                    }
                    break;
                case PlanController.REPAIR_PLAN:
                    plancontroller.plan(DemoFleet.mapper, DemoFleet.clustermap, entities, moves, tc, timestep);
                    break;
                default:
                    plancontroller.plan(DemoFleet.mapper, DemoFleet.clustermap, entities, moves, tc, timestep);
                    break;
            }
            System.out.println("Plan type: " + planType);
            System.out.println(plancontroller);
        }
        plancontroller.managedPlan();
        if(moves.isEmpty()) {
            moves.addLast(explorer.decideDirection(DemoFleet.mapper.getEntityMap(EntityChecker.FUELPUMP), tc));
        }
        if(!moves.peekLast().isDirectionalEntity()) {
            moves.addLast(explorer.decideDirection(DemoFleet.mapper.getEntityMap(EntityChecker.FUELPUMP), tc));
        }

        l.d("Fuel: " + getFuelLevel());
        l.d("Waste Level: " + getWasteLevel());
        l.d("Tanker Coordinate: " + tc.getTankerCoordinate());
        l.d("True Coordinate: " + getPosition());
        l.dc("[");
        for(CoreEntity m : moves) {
            if(m.isDirectionalEntity()) {
                l.dc("Direction: " + Calculation.directionToString(m.getBearing()));
            }
            else {
                l.dc(EntityChecker.entityToString(m.getEntity(), EntityChecker.DUMMY) + " " + m.getCoord() + " " + m.getEntity().getPoint() + "\t");
            }
        }
        l.d("]");
        if(!tc.getTankerCoordinate().toString().equalsIgnoreCase(getPosition().toString())) {
            System.out.println("Wrong coordinate");
            System.exit(-10);
        }

        setTankerMoveTowardsEntity();
        tankerClusterManager(getCurrentCell(view), timestep);
        return returnAction(view);
    }

    private void tankerClusterManager(Cell currentCell, long timestep) {
        if(tc.isMovingTowardsCluster() && moves.peekFirst().getEntityHash() == currentCell.getPoint().hashCode()) {
            tc.unsetMovingTowardsCluster();
            DemoFleet.clustermap.unsetTankerMoveTowardsEntity(moves.peekFirst());
            DemoFleet.clustermap.setLastVisitedCluster(moves.peekFirst(), timestep);
        }
    }

    private void setTankerMoveTowardsEntity() {
        for(CoreEntity m : moves) {
            if(!m.isDirectionalEntity()) {
                DemoFleet.mapper.setTankerMoveTowardsEntity(m, tc.getTankerID(), tc.getTankerCoordinate().distanceToCoordinate(m.getCoord()));
            }
        }
    }

    /**
     * Does a scan of the tanker's surrounding view of 40 x 40 + 1 grid blocks and stores each interesting entities
     * into a stack to measure relative closenest to the tanker
     *
     * @param view     Tanker's current view
     * @param timestep The current timestep fo the simulation
     */
    private void spiralScanView(Cell[][] view, long timestep) {
        int fr, lc, lr, fc, i;
        /*
        fr - First row
        lc - Last column
        lr - Last row
        fc - First column
        i - for loop iterator
         */
        int c = 0, x = -20, y = 21;
        fr = fc = 0;
        lc = Threshold.TOTAL_VIEW_RANGE.getThreshold() - 1;
        lr = Threshold.TOTAL_VIEW_RANGE.getThreshold() - 1;

        while (c < Threshold.TOTAL_VIEW_RANGE.getTotalViewGridLength()) {
            // Top row values
            for (i = fc; i <= lc; i++) {
                binEntitiesToStack(view[fr][i], x, --y, timestep);
                c++;
            }
            fr++;
            // Right column values
            for (i = fr; i <= lr; i++) {
                binEntitiesToStack(view[i][lc], ++x, y, timestep);
                c++;
            }
            lc--;
            if (fr < lr) { // Bottom row values
                for (i = lc; i >= fc; i--) {
                    binEntitiesToStack(view[lr][i], x, ++y, timestep);
                    c++;
                }
                lr--;
            }
            if (fc < lc) { // Left column values
                for (i = lr; i >= fr; i--) {
                    binEntitiesToStack(view[i][fc], --x, y, timestep);
                    c++;
                }
                fc++;
            }
        }

        // Add stacks into HashTable to be sent over to the decision function
        entities.put(EntityChecker.FUELPUMP, fuelpump);
        entities.put(EntityChecker.WELL, well);
        entities.put(EntityChecker.STATION, station);
        entities.put(EntityChecker.TASKEDSTATION, taskedStation);
    }

    /**
     * Bin each entity into the right stack
     *
     * @param entity   The entity viewed by the Tanker's view
     * @param timestep The current timestep in the simulation
     */
    private void binEntitiesToStack(Cell entity, int x, int y, long timestep) {
        CoreEntity node = new EntityNode(entity, Coordinates.coordinateShiftBy(tc.getTankerCoordinate(), new Coordinates(x, y), '+'), timestep);
        node.setBearing(Calculation.targetBearing(tc.getTankerCoordinate(), node.getCoord()));
        if (node.getBearing() == Calculation.ONTANKER) {
            node.setFirstVisited(timestep);
        }
        if(!node.getCoord().toString().equalsIgnoreCase(node.getEntity().getPoint().toString())) {
            System.out.println("Wrong entity coordinate: " + node.getCoord() + " " + node.getEntity().getPoint());
            System.exit(-11);
        }
        if (EntityChecker.isFuelPump(entity)) {
            fuelpump.add(node);
            DemoFleet.mapper.addEntity(node);
        } else if (EntityChecker.isStation(entity)) {
            station.add(node);
            if (((Station) entity).getTask() != null) {
                taskedStation.add(node);
            }
            DemoFleet.mapper.addEntity(node);
        } else if (EntityChecker.isWell(entity)) {
            well.add(node);
            DemoFleet.mapper.addEntity(node);
        }
    }

    private FallibleAction returnAction(Cell[][] view) {
        if (moves.peekFirst().isDirectionalEntity()) {
            System.out.println("Directional");
            tc.moveActionTankerDisplace(moves.peekFirst().getBearing());
            return new MoveAction(moves.peekFirst().getBearing());
        }

        Cell c = moves.peekFirst().getEntity();

        if (EntityChecker.getEntityType(c, true) == EntityChecker.getEntityType(getCurrentCell(view), true) &&
                c.getPoint().hashCode() == getCurrentCell(view).getPoint().hashCode()) {
            CoreEntity ts = moves.removeFirst();
            if (EntityChecker.isFuelPump(c) && getFuelLevel() < MAX_FUEL) {
                history.add(ts);
                l.d("MOVES: REFUEL" + " => " + c.getClass() + " @ " + c.getPoint());
                return new RefuelAction();
            } else if (EntityChecker.isWell(c) && getWasteLevel() > 0) {
                history.add(ts);
                l.d("MOVES: DUMP" + " => " + c.getClass() + " @ " + c.getPoint());
                return new DisposeWasteAction();
            } else if (EntityChecker.isStation(c) && getWasteCapacity() > 0) {
                history.add(ts);
                if (((Station) getCurrentCell(view)).getTask() != null) {
                    if (getWasteCapacity() >= ((Station) getCurrentCell(view)).getTask().getWasteRemaining()) {
                        DemoFleet.mapper.removeTaskedStation(ts);
                    }
                    l.d("MOVES: LOAD" + " => " + c.getClass() + " @ " + c.getPoint());
                    return new LoadWasteAction(((Station) getCurrentCell(view)).getTask());
                }
            } else { //Empty cell
                l.d("MOVES: EMPTY CELL" + " => " + c.getClass() + " @ " + c.getPoint());
                history.add(ts);
            }
            DemoFleet.mapper.unsetTankerMoveTowardsEntity(ts);
        }

        if (moves.peekFirst().isDirectionalEntity()) {
            System.out.println("Directional");
            tc.moveActionTankerDisplace(moves.peekFirst().getBearing());
            return new MoveAction(moves.peekFirst().getBearing());
        } else {
            System.out.println("Entity: " + moves.peekFirst().getEntity().getPoint());
            tc.moveTowardsActionTankerDisplace(moves.peekFirst());
            return new MoveTowardsAction(moves.peekFirst().getEntity().getPoint());
        }
    }

    private void cleanup() {
        fuelpump.clear();
        well.clear();
        station.clear();
        taskedStation.clear();
        entities.clear();
    }

}
