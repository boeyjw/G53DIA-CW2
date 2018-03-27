package uk.ac.nott.cs.g53dia.multidemo;

import java.util.Deque;
import java.util.Hashtable;
import java.util.List;

public class SinglePlanner extends Planner {
    @Override
    public Deque<CoreEntity> plan(MapBuilder map, ClusterMapBuilder clustermap,
                                  Hashtable<Integer, List<CoreEntity>> entities, Deque<CoreEntity> moves,
                                  TankerCoordinator tc, long timestep) {


        return null;
    }
}
