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
        moves.addFirst(explorer.getAndUpdateDirection(DemoFleet.explorationDirection));
        DemoFleet.explorationDirection.add(moves.peekFirst().getBearing());
    }

    public Action senseAndAct(Cell[][] view, long timestep) {
        l.d("");
        l.d("Timestep: " + timestep + "\t" + "TankerID: " + tc.getTankerID());
        // Init
        tc.checkActionFailed(actionFailed, moves, getCurrentCell(view), this, history.isEmpty() ? null : history.get(history.size() - 1));
        tc.setTankerStatus(this, new EntityNode(getCurrentCell(view), tc.getTankerCoordinate(), timestep),
                moves.isEmpty() ? TankerCoordinator.NOACTION : tc.setCurrentAction(moves.peekFirst()), timestep);
        DemoFleet.history.get(tc.getTankerID()).add(getCurrentCell(view).getPoint().toString());
        if(timestep > 1 && !moves.isEmpty() && moves.peekFirst().isDirectionalEntity() && EntityChecker.isFuelPump(getCurrentCell(view))) {
            moves.removeFirst();
        }

        // Scan stage
        spiralScanView(view, timestep);
        tc.setClosestFuelWell(
                fuelpump.isEmpty() ? null : fuelpump.get(fuelpump.size() - 1),
                well.isEmpty() ? null : well.get(well.size() - 1)
        );
        int clusterStatus = DemoFleet.clustermap.buildCluster(DemoFleet.mapper.getMap());

        // Planning Stage
        explorer.plan(DemoFleet.mapper, DemoFleet.clustermap, entities, moves, tc, timestep);

        // End stage
        if(!moves.isEmpty()) {
            if(!moves.peekFirst().isDirectionalEntity()) {
                moves.peekFirst().setBearing(Calculation.targetBearing(tc.getTankerCoordinate(), moves.peekFirst().getCoord()));
            }
            else {
                DemoFleet.explorationDirection.remove(tc.getTankerID());
                DemoFleet.explorationDirection.add(tc.getTankerID(), moves.peekFirst().getBearing());
            }
        }
        cleanup();
        System.out.println("Tanker coordinate: " + tc.getTankerCoordinate());
        System.out.println("True coordinate: " + getCurrentCell(view).getPoint());
        if(!tc.getTankerCoordinate().toString().equals(getCurrentCell(view).getPoint().toString())) {
            System.out.println("Wrong coordinate");
            System.exit(-1);
        }
        if(!moves.isEmpty()) {
            for(CoreEntity e : moves) {
                if(e.isDirectionalEntity()) {
                    System.out.println("Exploration direction: " + e.getBearing());
                }
                else {
                    System.out.println(EntityChecker.entityToString(e.getEntity(), EntityChecker.DUMMY));
                }
            }
        }

        return returnAction(view);
    }

    /**
     * Does a scan of the tanker's surrounding view of 40 x 40 + 1 grid blocks and stores each interesting entities
     * into a stack to measure relative closenest to the tanker
     * @param view Tanker's current view
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

        while(c < Threshold.TOTAL_VIEW_RANGE.getTotalViewGridLength()) {
            // Top row values
            for(i = fc; i <= lc; i++) {
                binEntitiesToStack(view[fr][i], x, --y, timestep);
                c++;
            }
            fr++;
            // Right column values
            for(i = fr; i <= lr; i++) {
                binEntitiesToStack(view[i][lc], ++x, y, timestep);
                c++;
            }
            lc--;
            if(fr < lr) { // Bottom row values
                for(i = lc; i >= fc; i--) {
                    binEntitiesToStack(view[lr][i], x, ++y, timestep);
                    c++;
                }
                lr--;
            }
            if(fc < lc) { // Left column values
                for(i = lr; i >= fr; i--) {
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
     * @param entity The entity viewed by the Tanker's view
     * @param timestep The current timestep in the simulation
     */
    private void binEntitiesToStack(Cell entity, int x, int y, long timestep) {
        CoreEntity node = new EntityNode(entity, new Coordinates(x, y).coordinateShiftBy(tc.getTankerCoordinate(), '+'), timestep);
        node.setBearing(Calculation.targetBearing(tc.getTankerCoordinate(), node.getCoord()));
        if(node.getBearing() == Calculation.ONTANKER) {
            node.setFirstVisited(timestep);
        }
        if(EntityChecker.isFuelPump(entity)) {
            fuelpump.add(node);
            DemoFleet.mapper.addEntity(node);
        }
        else if(EntityChecker.isStation(entity)) {
            station.add(node);
            if(((Station) entity).getTask() != null) {
                taskedStation.add(node);
            }
            DemoFleet.mapper.addEntity(node);
        }
        else if(EntityChecker.isWell(entity)) {
            well.add(node);
            DemoFleet.mapper.addEntity(node);
        }
    }

    private FallibleAction returnAction(Cell[][] view) {
        if(moves.peekFirst().isDirectionalEntity()) {
            System.out.println("Directional");
            tc.moveActionTankerDisplace(moves.peekFirst().getBearing());
            return new MoveAction(moves.peekFirst().getBearing());
        }

        Cell c = moves.peekFirst().getEntity();

        if(EntityChecker.getEntityType(c, true) == EntityChecker.getEntityType(getCurrentCell(view), true)) {
            CoreEntity ts = moves.removeFirst();
            if(EntityChecker.isFuelPump(c)) {
                history.add(ts);
                l.d("MOVES: REFUEL" + " => " + c.getClass() + " @ " + c.getPoint());
                return new RefuelAction();
            }
            else if(EntityChecker.isWell(c)) {
                history.add(ts);
                l.d("MOVES: DUMP" + " => " + c.getClass() + " @ " + c.getPoint());
                return new DisposeWasteAction();
            }
            else if(EntityChecker.isStation(c) && getWasteCapacity() > 0) {
                history.add(ts);
                if(((Station) getCurrentCell(view)).getTask() != null) {
                    DemoFleet.mapper.removeTaskedStation(ts);
                    l.d("MOVES: LOAD" + " => " + c.getClass() + " @ " + c.getPoint());
                    return new LoadWasteAction(((Station) getCurrentCell(view)).getTask());
                }
            }
            else { //Empty cell
                l.d("MOVES: EMPTY CELL" + " => " + c.getClass() + " @ " + c.getPoint());
                history.add(ts);
            }
        }

        if(moves.peekFirst().isDirectionalEntity()) {
            System.out.println("Directional");
            tc.moveActionTankerDisplace(moves.peekFirst().getBearing());
            return new MoveAction(moves.peekFirst().getBearing());
        }
        else {
            System.out.println("Entity: " + moves.peekFirst().getEntity().getPoint());
            tc.moveTowardsActionTankerDisplace(moves.peekFirst());
            return new MoveTowardsAction(moves.peekFirst().getEntity().getPoint());
        }
    }

    public TankerCoordinator getTC() {
        return tc;
    }

    private void cleanup() {
        fuelpump.clear();
        well.clear();
        station.clear();
        taskedStation.clear();
        entities.clear();
    }

}
