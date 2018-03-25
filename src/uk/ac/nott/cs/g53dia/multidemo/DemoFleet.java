package uk.ac.nott.cs.g53dia.multidemo;
import uk.ac.nott.cs.g53dia.multilibrary.*;

import java.util.*;

public class DemoFleet extends Fleet {

    /** 
     * Number of tankers in the fleet
     */
    private static int FLEET_SIZE = 1;

    public static MapBuilder mapper;
    public static Hashtable<Integer, TankerCoordinator> allTankers;
    public static Hashtable<Integer, List<String>> history;

    public DemoFleet() {
        mapper = new MapBuilder();
        allTankers = new Hashtable<>();
        history = new Hashtable<>();

        // Create the tankers
        for (int i = 0; i < FLEET_SIZE; i++) {
            this.add(new DemoTanker(new Random(), i));
            history.put(i, new ArrayList<>());
        }
    }
}
