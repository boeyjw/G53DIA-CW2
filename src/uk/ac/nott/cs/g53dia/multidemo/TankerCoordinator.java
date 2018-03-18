package uk.ac.nott.cs.g53dia.multidemo;

import uk.ac.nott.cs.g53dia.multilibrary.Cell;
import uk.ac.nott.cs.g53dia.multilibrary.MoveAction;
import uk.ac.nott.cs.g53dia.multilibrary.Tanker;

public class TankerCoordinator {
    private static final Coordinates tankerViewRangeCoordinate = new Coordinates(Tanker.VIEW_RANGE, Tanker.VIEW_RANGE);
    private Coordinates tankerCoordinate;
    private Cell entityUnderTanker;

    TankerCoordinator() {
        tankerCoordinate = new Coordinates(0, 0);
        this.entityUnderTanker = null;
    }

    public static Coordinates getTankerViewRangeCoordinate() {
        return tankerViewRangeCoordinate;
    }

    public Coordinates getTankerCoordinate() {
        return tankerCoordinate;
    }

    public void moveTowardsActionTankerDisplace(CoreEntity towardsEntity, boolean actionFailed) {
        if(actionFailed) {
            return;
        }

        NumberTuple ddTuple = Calculation.diagonalDistanceTuple(this.tankerCoordinate, towardsEntity.getCoord());
        if(ddTuple.getValue(0) != 0 || ddTuple.getValue(1) != 0) { // Tanker on entity
            moveActionTankerDisplace(towardsEntity.getBearing());
        }
    }

    public void moveActionTankerDisplace(int moveAction, boolean actionFailed) {
        if(actionFailed) {
            return;
        }
        moveActionTankerDisplace(moveAction);
    }

    public void moveActionTankerDisplace(int moveAction) {
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
            default:
                throw new IllegalArgumentException("Invalid direction" + "<" + moveAction + ">");
        }

        tankerCoordinate.coordinateShiftBy(new Coordinates(x, y), "+");
    }

    public Cell getEntityUnderTanker() {
        return entityUnderTanker;
    }

    public void setEntityUnderTanker(Cell entityUnderTanker) {
        this.entityUnderTanker = entityUnderTanker;
    }
}
