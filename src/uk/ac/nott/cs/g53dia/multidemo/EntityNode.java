package uk.ac.nott.cs.g53dia.multidemo;

import uk.ac.nott.cs.g53dia.multilibrary.Cell;
import uk.ac.nott.cs.g53dia.multilibrary.Point;

/**
 * Stores the entity in a node used for planning
 */
public class EntityNode extends CoreEntity {
    private int gscore;
    private int hscore;
    private int fscore;
    private float weight;
    private boolean visited;
    private int fuelConsumption;

    private EntityNode parent;

    public EntityNode(Cell entity, Coordinates coord, long firstVisited) {
        super(entity, coord, firstVisited);
        this.weight = 1; // No weight
        this.gscore = Integer.MAX_VALUE;
        this.hscore = Integer.MAX_VALUE;
        this.fscore = Integer.MAX_VALUE;
        this.fuelConsumption = 0;
        this.parent = null;
        this.visited = false;
    }

    public EntityNode(Cell entity, int x, int y, long firstVisited) {
        this(entity, new Coordinates(x, y), firstVisited);
    }

    EntityNode(int bearing) {
        super(bearing);
    }

    public int getGscore() {
        return gscore;
    }

    public void setGscore(int gscore) {
        this.gscore = gscore;
    }

    public int getHscore() {
        return hscore;
    }

    public void setHscore(int hscore) {
        this.hscore = hscore;
    }

    public int getFscore() {
        return fscore;
    }

    public void calculateFscore() {
        this.fscore = (int) Math.abs((hscore + gscore) * weight);
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public EntityNode getParent() {
        return parent;
    }

    public void setParent(EntityNode parent) {
        this.parent = parent;
    }

    public int getFuelConsumption() {
        return fuelConsumption;
    }

    public void setFuelConsumption(int fuelConsumption) {
        this.fuelConsumption = fuelConsumption;
    }

    public boolean isVisited() {
        return visited;
    }

    public void setVisited() {
        this.visited = true;
    }
}
