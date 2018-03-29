package uk.ac.nott.cs.g53dia.multidemo;

import uk.ac.nott.cs.g53dia.multilibrary.Cell;
import uk.ac.nott.cs.g53dia.multilibrary.MoveAction;
import uk.ac.nott.cs.g53dia.multilibrary.Station;
import uk.ac.nott.cs.g53dia.multilibrary.Tanker;

import java.util.Deque;
import java.util.List;

public class TankerCoordinator {
    public static int FATAL_ERROR = Integer.MIN_VALUE;
    public static int OUT_OF_FUEL_ERROR = -2;
    public static int NOACTION = -1;
    public static int EXPLORING = 0;
    public static int REFUEL = 1;
    public static int DISPOSE = 2;
    public static int LOAD = 3;
    public static int MOVING_TOWARDS = 4;

    private long timestep;

    // Tanker positioning
    private static final Coordinates tankerViewRangeCoordinate = new Coordinates(Tanker.VIEW_RANGE, Tanker.VIEW_RANGE);
    private Coordinates tankerCoordinate;
    private Coordinates tankerCoordinateBackup;

    // Tanker status
    private int tankerID;
    private CoreEntity entityUnderTanker;
    private int totalWasteCollected;
    private int fuelLevel;
    private int wasteLevel;
    private int posHashcode;
    private int currentAction;
    private boolean movingTowardsCluster;

    // Closest important entities
    private CoreEntity closestObservableFuelpump;
    private CoreEntity closestObservableWell;

    TankerCoordinator(Tanker t, int tankerID) {
        this.timestep = 1;

        tankerCoordinate = new Coordinates(0, 0);
        tankerCoordinateBackup = tankerCoordinate.clone();

        this.tankerID = tankerID;
        this.entityUnderTanker = null;
        this.totalWasteCollected = t.getScore();
        this.fuelLevel = t.getFuelLevel();
        this.wasteLevel = t.getWasteLevel();
        this.posHashcode = t.getPosition().hashCode();
        this.currentAction = NOACTION;
        this.movingTowardsCluster = false;
    }

    public static Coordinates getTankerViewRangeCoordinate() {
        return tankerViewRangeCoordinate;
    }

    public Coordinates getTankerCoordinate() {
        return tankerCoordinate;
    }

    public void moveTowardsActionTankerDisplace(CoreEntity towardsEntity) {
        NumberTuple ddTuple = Calculation.diagonalDistanceTuple(this.tankerCoordinate, towardsEntity.getCoord());
        if (ddTuple.getValue(0) != 0 || ddTuple.getValue(1) != 0) { // Tanker not on entity
            moveActionTankerDisplace(Calculation.targetBearing(this.tankerCoordinate, towardsEntity.getCoord()));
        }
    }

    public void checkActionFailed(boolean actionFailed, Deque<CoreEntity> moves, Cell currentCell, Tanker t, CoreEntity historyTail) {
        if (actionFailed) {
            if (historyTail != null && currentCell.getPoint().hashCode() == historyTail.getEntity().getPoint().hashCode()) {
                System.out.println("Tanker Coordinate backup inner: " + tankerCoordinateBackup);
                switch (EntityChecker.getEntityType(currentCell, false)) {
                    case EntityChecker.FUELPUMP:
                        if (t.getFuelLevel() < Tanker.MAX_WASTE - 1) {
                            moves.addFirst(historyTail);
                            tankerCoordinateBackup = tankerCoordinate = moves.peekFirst().getCoord();
                        }
                        else {
                            tankerCoordinate = tankerCoordinateBackup.clone();
                        }
                        break;
                    case EntityChecker.WELL:
                        if (t.getWasteLevel() != 0) {
                            moves.addFirst(historyTail);
                            tankerCoordinateBackup = tankerCoordinate = moves.peekFirst().getCoord();
                        }
                        else {
                            tankerCoordinate = tankerCoordinateBackup.clone();
                        }
                        break;
                    case EntityChecker.TASKEDSTATION:
                        if(((Station) currentCell).getTask() != null) {
                            moves.addFirst(historyTail);
                            tankerCoordinateBackup = tankerCoordinate = moves.peekFirst().getCoord();
                        }
                        else {
                            tankerCoordinate = tankerCoordinateBackup.clone();
                        }
                        break;
                    default:
                        tankerCoordinate = tankerCoordinateBackup.clone();
                        break;
                }
            }
            else {
                tankerCoordinate = tankerCoordinateBackup.clone();
            }
        }
    }

    public void moveActionTankerDisplace(int moveAction) {
        tankerCoordinateBackup = tankerCoordinate.clone();
        int x = 0, y = 0;
        switch (moveAction) {
            case MoveAction.NORTH:
                y++;
                break;
            case MoveAction.NORTHWEST:
                y++;
                x--;
                break;
            case MoveAction.NORTHEAST:
                y++;
                x++;
                break;
            case MoveAction.SOUTH:
                y--;
                break;
            case MoveAction.SOUTHWEST:
                y--;
                x--;
                break;
            case MoveAction.SOUTHEAST:
                y--;
                x++;
                break;
            case MoveAction.EAST:
                x++;
                break;
            case MoveAction.WEST:
                x--;
                break;
            case Calculation.ONTANKER:
                break;
            default:
                throw new IllegalArgumentException("Invalid direction " + "<" + moveAction + ", " + Calculation.directionToString(moveAction) + ">");
        }

        tankerCoordinate = Coordinates.coordinateShiftBy(tankerCoordinate, new Coordinates(x, y), '+');
    }

    public CoreEntity getEntityUnderTanker() {
        return entityUnderTanker;
    }

    public int getTankerID() {
        return tankerID;
    }

    public int getTotalWasteCollected() {
        return totalWasteCollected;
    }

    public void setTankerStatus(Tanker t, CoreEntity entityUnderTanker, int currentAction, long timestep) {
        this.timestep = timestep;
        this.entityUnderTanker = entityUnderTanker;
        entityUnderTanker.setBearing(Calculation.ONTANKER);
        fuelLevel = t.getFuelLevel();
        wasteLevel = t.getWasteLevel();
        posHashcode = t.getPosition().hashCode();
        this.totalWasteCollected = t.getScore();
        this.currentAction = currentAction;
    }

    public int setCurrentAction(CoreEntity currentMove) {
        int ca = FATAL_ERROR;
        if (currentMove.isDirectionalEntity()) {
            ca = EXPLORING;
        } else if (entityUnderTanker.getEntity().getPoint().hashCode() == currentMove.getEntity().getPoint().hashCode()) {
            switch (EntityChecker.getEntityType(currentMove.getEntity(), false)) {
                case EntityChecker.FUELPUMP:
                    ca = REFUEL;
                    break;
                case EntityChecker.TASKEDSTATION:
                    ca = LOAD;
                    break;
                case EntityChecker.WELL:
                    ca = DISPOSE;
                    break;
                default:
                    ca = FATAL_ERROR;
                    break;
            }
        } else if (tankerCoordinate.distanceToCoordinate(currentMove.getCoord()) < fuelLevel + Threshold.REFUEL_ERROR_MARGIN.getThreshold()) {
            ca = OUT_OF_FUEL_ERROR;
        }

        return ca;
    }

    public void setClosestFuelWell(CoreEntity fuelpump, CoreEntity well) {
        CoreEntity tmp;
        if(fuelpump == null) {
            tmp = getClosest(EntityChecker.FUELPUMP);
            if(tmp != null && tankerCoordinate.distanceToCoordinate(this.closestObservableFuelpump.getCoord()) > tankerCoordinate.distanceToCoordinate(tmp.getCoord())) {
                this.closestObservableFuelpump = tmp;
            }
        }
        else {
            this.closestObservableFuelpump = fuelpump;
        }
        if(well == null) {
            tmp = getClosest(EntityChecker.WELL);
            if(tmp != null && this.closestObservableWell != null &&
                    tankerCoordinate.distanceToCoordinate(this.closestObservableWell.getCoord()) > tankerCoordinate.distanceToCoordinate(tmp.getCoord())) {
                this.closestObservableWell = tmp;
            }
        }
        else {
            this.closestObservableWell = well;
        }
        if (this.closestObservableWell != null) {
            if (this.closestObservableWell.getCoord().distanceToCoordinate(this.closestObservableFuelpump.getCoord()) > 20) {
                this.closestObservableWell = null;
            }
        }
    }

    private CoreEntity getClosest(int entityType) {
        int ind = -1;
        int argmin = -1;
        int min = Integer.MAX_VALUE;

        for (CoreEntity c : DemoFleet.mapper.getEntityMap(entityType)) {
            ind++;
            int dist = tankerCoordinate.distanceToCoordinate(c.getCoord());
            if (dist < min) {
                min = dist;
                argmin = ind;
            }
        }

        CoreEntity closestEntity = argmin == -1 ? null : DemoFleet.mapper.getEntityMap(entityType).get(ind);
        if(closestEntity == null) {
            return null;
        }
        else {
            closestEntity.setBearing(Calculation.targetBearing(tankerCoordinate, closestEntity.getCoord()));
            return closestEntity;
        }
    }

    public int getFuelLevel() {
        return fuelLevel;
    }

    public int getWasteLevel() {
        return wasteLevel;
    }

    public int getPosHashcode() {
        return posHashcode;
    }

    public int getCurrentAction() {
        return currentAction;
    }

    public CoreEntity getClosestObservableFuelpump() {
        return closestObservableFuelpump;
    }

    public CoreEntity getClosestObservableWell() {
        return closestObservableWell;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Tanker ID: ").append(tankerID).append("\n");
        sb.append("Tanker Coordinate: ").append(tankerCoordinate.toString()).append("\n");
        sb.append("Entity Under Tanker: ").append(EntityChecker.entityToString(entityUnderTanker.getEntity(), Integer.MIN_VALUE)).append("\n");
        sb.append("Fuel Level: ").append(fuelLevel).append("\n");
        sb.append("Waste Level: ").append(wasteLevel).append("\n");
        sb.append("Waste Collected: ").append(totalWasteCollected).append("\n");
        sb.append("Current Action: ").append(currentAction).append("\n");

        return sb.toString();
    }

    public boolean isMovingTowardsCluster() {
        return movingTowardsCluster;
    }

    public void setMovingTowardsCluster() {
        this.movingTowardsCluster = true;
    }

    public void unsetMovingTowardsCluster() {
        this.movingTowardsCluster = false;
    }}
