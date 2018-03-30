package uk.ac.nott.cs.g53dia.multidemo;

import uk.ac.nott.cs.g53dia.multilibrary.Fleet;

import java.util.*;

public class DemoFleet extends Fleet {

    /** 
     * Number of tankers in the fleet
     */
    private static int FLEET_SIZE = 4;

    // Global map
    public static MapBuilder mapper;
    // Exploit map
    public static ClusterMapBuilder clustermap;
    // All tanker status
    public static Hashtable<Integer, TankerCoordinator> allTankers;
    // All tanker history of moves
    public static Hashtable<Integer, List<String>> history;
    // All tanker exploration direction
    public static Hashtable<Integer, Integer> explorationDirection;
    // Multiagent planning
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
