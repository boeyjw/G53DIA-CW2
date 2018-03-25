package uk.ac.nott.cs.g53dia.multidemo;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Hashtable;
import java.util.List;

public class SinglePlanner extends Planner {
    @Override
    public Deque<CoreEntity> plan(Hashtable<Integer, List<CoreEntity>> map, TankerCoordinator tc, TwoNumberTuple tankerStatus, long timestep) {
        Deque<CoreEntity> moves = new ArrayDeque<>();
        int nextMove = EntityChecker.DUMMY;

        if(!super.acceptableFuelLevel(tankerStatus.getValue(0),
                super.getClosestEntityDistanceTo(map.get(EntityChecker.FUELPUMP), tc.getEntityUnderTanker()))) {
            nextMove = EntityChecker.FUELPUMP;
        }
        else if(!super.acceptableWasteLevel(tankerStatus.getValue(1))) {
            nextMove = EntityChecker.WELL;
        }

        return null;
    }
}
