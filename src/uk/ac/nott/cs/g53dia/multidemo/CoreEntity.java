package uk.ac.nott.cs.g53dia.multidemo;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import javafx.beans.binding.StringBinding;
import uk.ac.nott.cs.g53dia.multilibrary.Cell;
import uk.ac.nott.cs.g53dia.multilibrary.Point;
import uk.ac.nott.cs.g53dia.multilibrary.Station;
import uk.ac.nott.cs.g53dia.multilibrary.Tanker;

/**
 * Stored essentially information retrieved by the agent for a particular cell
 */
public abstract class CoreEntity {
    private Cell entity;
    private int entityHash;
    private Coordinates coord;
    private Point position;
    private long firstVisited;
    private long firstSeen;

    protected long lastVisited;
    protected long lastSeen;
    protected boolean hasTask;
    protected int bearing;
    protected int wasteRemaining;
    protected int timesVisited;
    protected int timesSeen;

    CoreEntity(@Nullable Cell entity, @NotNull Coordinates coord, @NotNull long firstSeen) {
        this.entity = entity;
        this.entityHash = entity == null ? Integer.MIN_VALUE : entity.getPoint().hashCode();
        this.coord = coord;
        this.position = entity == null ? null : entity.getPoint();

        this.firstVisited = this.lastVisited = Integer.MIN_VALUE;
        this.firstSeen = this.lastSeen = firstSeen;
        this.hasTask = entity != null && EntityChecker.hasTaskStation(entity);
        this.bearing = Integer.MIN_VALUE;
        this.wasteRemaining = this.hasTask ? ((Station) entity).getTask().getWasteRemaining() : Integer.MIN_VALUE;
        this.timesVisited = 1;
        this.timesSeen = 1;
    }

    CoreEntity(@Nullable Cell entity, @NotNull int x, @NotNull int y, @NotNull long firstSeen) {
        this(entity, new Coordinates(x, y), firstSeen);
    }

    public Cell getEntity() {
        return entity;
    }

    public boolean hasTask() {
        return hasTask;
    }

    public long getLastVisited() {
        return lastVisited;
    }

    public void setLastVisited(long thisVisit) {
        this.lastVisited = thisVisit;
    }

    public int getEntityHash() {
        return entityHash;
    }

    public Coordinates getCoord() {
        return coord;
    }

    public Point getPosition() {
        return position;
    }

    public void setPosition(Point position) {
        this.position = position;
    }

    public int getBearing() {
        return bearing;
    }

    public void setBearing(int bearing) {
        this.bearing = bearing;
    }

    public int getWasteRemaining() {
        return wasteRemaining;
    }

    public void setWasteRemaining(int wasteRemaining) {
        this.wasteRemaining = wasteRemaining;
    }

    public long getFirstVisited() { return firstVisited; }

    public long getFirstSeen() { return firstSeen; }

    public long getLastSeen() { return lastSeen; }

    public void setLastSeen(long lastSeen) { this.lastSeen = lastSeen; }

    public void setFirstVisited(long firstVisited) { this.firstVisited = firstVisited; }

    public void setLastVisitedSeen(long lastVisited) { this.lastSeen = this.lastVisited = lastVisited; }

    public boolean reduceWasteRemaining(int wasteCollected) {
        this.wasteRemaining -= wasteCollected;
        if (this.wasteRemaining < 0) {
            this.wasteRemaining = 0;
            return false;
        } else {
            return true;
        }
    }

    public int getTimesSeen() { return timesSeen; }

    public void incTimesSeen() { timesSeen++; }

    public int getTimesVisited() { return timesVisited; }

    public void incTimesVisited() { timesVisited++; }

    public boolean isDirectionalEntity() { return entity == null && bearing != Integer.MIN_VALUE; }

    public int getDistanceToTanker(Coordinates tankerCoordinate) { return coord.distanceToCoordinate(tankerCoordinate); }

    public boolean canLoadAllWaste(int tankerWasteCapacity) { return hasTask && tankerWasteCapacity + wasteRemaining <= Tanker.MAX_WASTE; }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof CoreEntity)) {
            return false;
        }
        CoreEntity c = (CoreEntity) obj;

        return c.getEntity() == null && entity == null && isDirectionalEntity() == c.isDirectionalEntity() ?
                bearing == c.getBearing() : entity.equals((Cell) ((CoreEntity) obj).getEntity());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Type: ").append(EntityChecker.entityToString(this.getEntity(), EntityChecker.DUMMY)).append("\n");
        sb.append("Hash: ").append(this.entityHash).append("\n");
        sb.append("Coordinate: ").append(this.coord.toString()).append("\n");
        sb.append("Bearing: ").append(Calculation.directionToString(this.bearing)).append("\n");
        sb.append("First Seen: ").append(this.firstSeen).append("\n");
        sb.append("Last Seen: ").append(this.lastSeen).append("\n");
        sb.append("First Visited: ").append(this.firstVisited).append("\n");
        sb.append("Last Visited: ").append(this.lastVisited).append("\n");
        if (this.hasTask) {
            sb.append("Waste Amount: ").append(this.wasteRemaining).append("\n");
        }

        return sb.toString();
    }
}
