package uk.ac.nott.cs.g53dia.multidemo;

import uk.ac.nott.cs.g53dia.multilibrary.Fleet;

import java.util.*;

public class DemoFleet extends Fleet {

    /** 
     * Number of tankers in the fleet
     */
    private static int FLEET_SIZE = 2;

    public static MapBuilder mapper;
    public static ClusterMapBuilder clustermap;
    public static Hashtable<Integer, TankerCoordinator> allTankers;
    public static Hashtable<Integer, List<String>> history;
    public static Hashtable<Integer, Integer> explorationDirection;
    public static Planner multiplanner;

    public DemoFleet() {
        mapper = new MapBuilder();
        clustermap = new ClusterMapBuilder();
        allTankers = new Hashtable<>();
        history = new Hashtable<>();
        explorationDirection = new Hashtable<>();
        multiplanner = new Multiplanner();

        // Create the tankers
        for (int i = 0; i < FLEET_SIZE; i++) {
            this.add(new DemoTanker(new Random(), i));
            history.put(i, new ArrayList<>());
        }
    }
}
