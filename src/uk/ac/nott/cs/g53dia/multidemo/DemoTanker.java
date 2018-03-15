package uk.ac.nott.cs.g53dia.multidemo;
import uk.ac.nott.cs.g53dia.multilibrary.*;
import uk.ac.nott.cs.g53dia.multidemo.CoreEntity;

import java.util.Hashtable;
import java.util.List;
import java.util.Random;

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
    private Hashtable<String, List<CoreEntity>> entities;
    private MapBuilder mapper;
    private List<CoreEntity> fuelpump, well, station, taskedStation;
	
    public DemoTanker() { 
	this(new Random());
    }

    public DemoTanker(Random r) {
	this.r = r;
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
        entities.put("fuel", fuelpump);
        entities.put("well", well);
        entities.put("station", station);
        entities.put("taskedStation", taskedStation);
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

    private void cleanup() {
        fuelpump.clear();
        well.clear();
        station.clear();
        taskedStation.clear();
        entities.clear();
    }

}
