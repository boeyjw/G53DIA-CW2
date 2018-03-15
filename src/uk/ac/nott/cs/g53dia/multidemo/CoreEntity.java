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
    private long lastVisited;
    private boolean hasTask;
    private Point position;
    private int bearing;
    private int wasteRemaining;

    CoreEntity(Cell entity, Coordinates coord, long firstVisit, Point position) {
        this.entity = entity;
        this.entityHash = entity.getPoint().hashCode();
        this.coord = coord;
        this.lastVisited = firstVisit;
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

    public boolean isHasTask() {
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

    public boolean reduceWasteRemaining(int wasteCollected) {
        this.wasteRemaining -= wasteCollected;
        if(this.wasteRemaining < 0) {
            this.wasteRemaining = 0;
            return false;
        }
        else {
            return true;
        }
    }
}
