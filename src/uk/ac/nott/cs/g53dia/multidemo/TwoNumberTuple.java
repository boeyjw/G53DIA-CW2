package uk.ac.nott.cs.g53dia.multidemo;

/**
 * Represent a 2-tuple of (int x, int y)
 */
public class TwoNumberTuple implements NumberTuple {
    protected int x;
    protected int y;

    public TwoNumberTuple(int x, int y) {
        this.x = x;
        this.y = y;
    }


    @Override
    public int getValue(int valuePosition) {
        if(valuePosition == 0)
            return x;
        else if(valuePosition == 1)
            return y;
        throw new IndexOutOfBoundsException("Tuple value out of range");
    }

    @Override
    public int getMin() {
        return Math.min(x, y);
    }

    @Override
    public int getMax() {
        return Math.max(x, y);
    }

    @Override
    public NumberTuple simpleOperation(int value, NumberTuple otherNumberTuple, String operation) {
        int x = Integer.MIN_VALUE;
        int y = Integer.MIN_VALUE;

        switch (operation) {
            case PLUS:
                x = this.x + (otherNumberTuple == null ? value : otherNumberTuple.getValue(0));
                y = this.y + (otherNumberTuple == null ? value : otherNumberTuple.getValue(1));
                break;
            case MINUS:
                x = this.x - (otherNumberTuple == null ? value : otherNumberTuple.getValue(0));
                y = this.y - (otherNumberTuple == null ? value : otherNumberTuple.getValue(1));
                break;
            case MULTIPLY:
                x = this.x * (otherNumberTuple == null ? value : otherNumberTuple.getValue(0));
                y = this.y * (otherNumberTuple == null ? value : otherNumberTuple.getValue(1));
                break;
            case DIVIDE:
                x = this.x / (otherNumberTuple == null ? value : otherNumberTuple.getValue(0));
                y = this.y / (otherNumberTuple == null ? value : otherNumberTuple.getValue(1));
                break;
            default:
                throw new IllegalArgumentException("Operation out of scope");
        }

        return new TwoNumberTuple(x, y);
    }
}
