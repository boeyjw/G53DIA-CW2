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
        int x = this.x;
        int y = this.y;

        switch (operation) {
            case PLUS:
                x += (otherNumberTuple == null ? value : otherNumberTuple.getValue(0));
                y += (otherNumberTuple == null ? value : otherNumberTuple.getValue(1));
                break;
            case MINUS:
                x -= (otherNumberTuple == null ? value : otherNumberTuple.getValue(0));
                y -= (otherNumberTuple == null ? value : otherNumberTuple.getValue(1));
                break;
            case MULTIPLY:
                x *= (otherNumberTuple == null ? value : otherNumberTuple.getValue(0));
                y *= (otherNumberTuple == null ? value : otherNumberTuple.getValue(1));
                break;
            case DIVIDE:
                x /= (otherNumberTuple == null ? value : otherNumberTuple.getValue(0));
                y /= (otherNumberTuple == null ? value : otherNumberTuple.getValue(1));
                break;
            default:
                throw new IllegalArgumentException("Operation out of scope");
        }

        return new TwoNumberTuple(x, y);
    }
}
