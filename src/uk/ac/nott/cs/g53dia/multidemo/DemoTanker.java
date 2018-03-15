package uk.ac.nott.cs.g53dia.multidemo;
import uk.ac.nott.cs.g53dia.multilibrary.*;
import uk.ac.nott.cs.g53dia.multidemo.CoreEntity;

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
 
    	// If fuel tank is low and not at the fuel pump then move
    	// towards the fuel pump
        if ((getFuelLevel() <= MAX_FUEL/2) && !(getCurrentCell(view) instanceof FuelPump)) {
            return new MoveTowardsAction(FUEL_PUMP_LOCATION);
        } else {
            // Otherwise, move randomly
            return new MoveAction(r.nextInt(8));       	
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
        int c = 0;
        fr = fc = 0;
        lc = Threshold.TOTAL_VIEW_RANGE.getThreshold() - 1;
        lr = Threshold.TOTAL_VIEW_RANGE.getThreshold() - 1;

        while(c < Threshold.TOTAL_VIEW_RANGE.getTotalViewGridLength()) {
            // Top row values
            for(i = fc; i <= lc; i++) {
                binEntitiesToStack(view[fr][i], fr, i, timestep);
                c++;
            }
            fr++;
            // Right column values
            for(i = fr; i <= lr; i++) {
                binEntitiesToStack(view[i][lc], i, lc, timestep);
                c++;
            }
            lc--;
            if(fr < lr) { // Bottom row values
                for(i = lc; i >= fc; i--) {
                    binEntitiesToStack(view[lr][i], lr, i, timestep);
                    c++;
                }
                lr--;
            }
            if(fc < lc) { // Left column values
                for(i = lr; i >= fr; i--) {
                    binEntitiesToStack(view[i][fc], i, fc, timestep);
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
        EntityNode node = new EntityNode(entity, x, y, timestep);
        node.setBearing(Calculation.targetBearing(Coordinates.getTankerCoordinate(), node.getCoord()));
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

        if(EntityChecker.getEntityType(c) == EntityChecker.getEntityType(getCurrentCell(view))) {
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
