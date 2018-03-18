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
    private int tankerID;
    private TankerCoordinator tc;

    // Entity objects
    private Hashtable<Integer, List<CoreEntity>> entities;
    private List<CoreEntity> fuelpump, well, station, taskedStation;
    private Deque<CoreEntity> moves;

    // Mapping objects
    private MapBuilder mapper;
    private MapBuilder history;

    // Behaviour objects
    private Explorer explorer;
    private int explorerDirection;

    public DemoTanker() {
        this(new Random());
    }

    public DemoTanker(Random r) {
        this.r = r;
        l = new Log(true);

        tc = new TankerCoordinator();

        entities = new Hashtable<>();
        fuelpump = new ArrayList<>();
        well = new ArrayList<>();
        station = new ArrayList<>();
        taskedStation = new ArrayList<>();
        moves = new ArrayDeque<>();

        mapper = new MapBuilder();
        history = new MapBuilder();

        explorer = new Explorer(this.r);
        explorerDirection = explorer.getAndUpdateDirection();
    }

    /*
     * The following is a simple demonstration of how to write a
     * tanker. The code below is very stupid and simply moves the
     * tanker randomly until the fuel tank is half full, at which
     * point it returns to a fuel pump to refuel.
     */
    public Action senseAndAct(Cell[][] view, long timestep) {
        tc.setEntityUnderTanker(getCurrentCell(view));
        // TODO: Add to global map
        spiralScanView(view, timestep);
        cleanup();
        // TODO: Things to do
        // If fuel tank is low and not at the fuel pump then move
        // towards the fuel pump
        l.d("Tanker Coordinate: " + tc.getTankerCoordinate().toString());
        l.d("True Tanker Coordinate: " + getPosition().toString());
        if (getFuelLevel() <= 52) {
            if(EntityChecker.isFuelPump(getCurrentCell(view))) {
                return new RefuelAction();
            }
            else {
                return new MoveTowardsAction(FUEL_PUMP_LOCATION);
            }
        } else {
            // Otherwise, move randomly
            int rdir = r.nextInt(8);
            return new MoveAction(rdir);
        }
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
        CoreEntity node = new EntityNode(entity, x, y, timestep);
        node.setBearing(Calculation.targetBearing(tc.getTankerCoordinate(), node.getCoord()));
        if(node.getBearing() == Calculation.ONTANKER) {
            node.setFirstVisited(timestep);
        }
//        if(node.getBearing() == Calculation.ONTANKER) {
//            l.dc(node.toString());
//            l.d("True Coordinate: " + node.getEntity().getPoint().toString());
//            l.d("");
//        }
        mapper.update(node, false, timestep, tc.getEntityUnderTanker().hashCode());
        if(EntityChecker.isFuelPump(entity)) {
            fuelpump.add(node);
        }
        else if(EntityChecker.isStation(entity)) {
            station.add(node);
            if(((Station) entity).getTask() != null) {
                taskedStation.add(node);
            }
        }
        else if(EntityChecker.isWell(entity)) {
            well.add(node);
        }
    }

    private FallibleAction returnAction(Cell[][] view) {
        Cell c = moves.peekFirst().getEntity();

        if(EntityChecker.getEntityType(c, true) == EntityChecker.getEntityType(getCurrentCell(view), true)) {
            l.d("Current: " + getCurrentCell(view).hashCode());
            if(EntityChecker.isFuelPump(c)) {
                if(Explorer.explorerMode) {
                    explorerDirection = explorer.getAndUpdateDirection();
                }
                history.simpleAdd(moves.removeFirst());
                l.d("MOVES: REFUEL" + " => " + c.getClass() + " @ " + c.hashCode());
                return new RefuelAction();
            }
            else if(EntityChecker.isWell(c)) {
                history.simpleAdd(moves.removeFirst());
                l.d("MOVES: DUMP" + " => " + c.getClass() + " @ " + c.hashCode());
                return new DisposeWasteAction();
            }
            else if(EntityChecker.isStation(c) && getWasteCapacity() > 0) {
                history.simpleAdd(moves.removeFirst());
                if(((Station) getCurrentCell(view)).getTask() != null) {
                    l.d("MOVES: LOAD" + " => " + c.getClass() + " @ " + c.hashCode());
                    return new LoadWasteAction(((Station) getCurrentCell(view)).getTask());
                }
            }
            else { //Empty cell
                l.d("MOVES: EMPTY CELL" + " => " + c.getClass() + " @ " + c.hashCode());
                history.simpleAdd(moves.removeFirst());
            }
        }

        return null;
    }

    private void cleanup() {
        fuelpump.clear();
        well.clear();
        station.clear();
        taskedStation.clear();
        entities.clear();
    }

}
