package uk.ac.nott.cs.g53dia.multidemo;

import javafx.beans.binding.StringBinding;
import uk.ac.nott.cs.g53dia.multilibrary.Cell;
import uk.ac.nott.cs.g53dia.multilibrary.Point;
import uk.ac.nott.cs.g53dia.multilibrary.Station;

/**
 * Stored essentially information retrieved by the agent for a particular cell
 */
public abstract class CoreEntity {
    private Cell entity;
    private int entityHash;
    private Coordinates coord;
    private long firstVisited;
    private long lastVisited;
    private long firstSeen;
    private long lastSeen;
    private boolean hasTask;
    private Point position;
    private int bearing;
    private int wasteRemaining;

    CoreEntity(Cell entity, Coordinates coord, long firstVisit, Point position) {
        this.entity = entity;
        this.entityHash = entity.getPoint().hashCode();
        this.coord = coord;
        this.firstVisited = this.lastVisited = Integer.MIN_VALUE;
        this.firstSeen = this.lastSeen = firstVisit;
        this.hasTask = EntityChecker.isStation(entity) && ((Station) entity).getTask() != null;
        this.position = position;
        this.bearing = Integer.MIN_VALUE;
        this.wasteRemaining = this.hasTask ? ((Station) entity).getTask().getWasteRemaining() : Integer.MIN_VALUE;
    }

    CoreEntity(Cell entity, int x, int y, long firstVisit) {
        this(entity, new Coordinates(x, y), firstVisit, null);
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
