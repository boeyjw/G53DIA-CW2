package uk.ac.nott.cs.g53dia.multidemo;

import uk.ac.nott.cs.g53dia.multilibrary.Cell;

/**
 * Stores the entity in a node used for planning
 */
public class EntityNode extends CoreEntity {
    public static final int MAXIMUM_PENALTY = 99;

    private int gscore;
    private int hscore;
    private int fscore;
    private double weight;
    private boolean visited;
    private int fuelConsumption;

    private EntityNode parent;

    public EntityNode(Cell entity, Coordinates coord, long firstVisited) {
        super(entity, coord, firstVisited);
        this.weight = 1; // No weight
        this.gscore = 0;
        this.hscore = 0;
        this.fscore = 0;
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
        this.fscore = (int) Math.abs(gscore + (hscore * weight));
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
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
