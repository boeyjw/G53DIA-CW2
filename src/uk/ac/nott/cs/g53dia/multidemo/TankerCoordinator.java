package uk.ac.nott.cs.g53dia.multidemo;

import uk.ac.nott.cs.g53dia.multilibrary.MoveAction;
import uk.ac.nott.cs.g53dia.multilibrary.Tanker;

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
    }

    public static Coordinates getTankerViewRangeCoordinate() {
        return tankerViewRangeCoordinate;
    }

    public Coordinates getTankerCoordinate() {
        return tankerCoordinate;
    }

    public void moveTowardsActionTankerDisplace(CoreEntity towardsEntity) {
        NumberTuple ddTuple = Calculation.diagonalDistanceTuple(this.tankerCoordinate, towardsEntity.getCoord());
        if (ddTuple.getValue(0) != 0 || ddTuple.getValue(1) != 0) { // Tanker on entity
            moveActionTankerDisplace(towardsEntity.getBearing());
        }
    }

    public void checkActionFailed(boolean actionFailed) {
        if (actionFailed) {
            tankerCoordinate = tankerCoordinateBackup.clone();
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

        tankerCoordinate.coordinateShiftBy(new Coordinates(x, y), '+');
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
        if(fuelpump != null) {
            this.closestObservableFuelpump = fuelpump;
        }
        if(well != null) {
            this.closestObservableWell = well;
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
        sb.append("Tanker ID: ");
        sb.append(tankerID);
        sb.append("\n");
        sb.append("Tanker Coordinate: ");
        sb.append(tankerCoordinate.toString());
        sb.append("\n");
        sb.append("Entity Under Tanker: ");
        sb.append(EntityChecker.entityToString(entityUnderTanker.getEntity(), Integer.MIN_VALUE));
        sb.append("\n");
        sb.append("Waste Collected: ");
        sb.append(totalWasteCollected);
        sb.append("\n");

        return sb.toString();
    }

}
