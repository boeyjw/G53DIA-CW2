package uk.ac.nott.cs.g53dia.multidemo;

import uk.ac.nott.cs.g53dia.multilibrary.Cell;
import uk.ac.nott.cs.g53dia.multilibrary.Point;
import uk.ac.nott.cs.g53dia.multilibrary.Station;

public class TankerEntity extends CoreEntity {
    private int tankerID;
    private int totalWasteCollected;

    TankerEntity(Cell entity, int x, int y, long firstSeen, int tankerID, int totalWasteCollected) {
        super(entity, x, y, firstSeen);
        this.tankerID = tankerID;
        this.totalWasteCollected = totalWasteCollected;
    }

    public int getTankerID() {
        return tankerID;
    }

    public int getTotalWasteCollected() {
        return totalWasteCollected;
    }

    public void setTotalWasteCollected(int totalWasteCollected) {
        this.totalWasteCollected = totalWasteCollected;
    }
}
