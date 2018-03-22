package uk.ac.nott.cs.g53dia.multidemo;

/**
 *
 */
public interface NumberTuple {
    char PLUS = '+';
    char MINUS = '-';
    char MULTIPLY = '*';
    char DIVIDE = '/';

    /**
     * Get the value from the {@link NumberTuple} based on its position
     * @param valuePosition The position in which the value is at
     * @return Single int value from the tuple
     */
    int getValue(int valuePosition);

    /**
     * Gets the minimum value of the tuple pair
     * @return Minimum value of the tuple pair
     */
    int getMin();
    /**
     * Gets maximum value of the tuple pair
     * @return Maximum value of the tuple pair
     */
    int getMax();

    /**
     * Allows application of simple arithmetic operations (+, -, x, /) over the tuples by a single value
     * or another {@link NumberTuple} instance
     * @param value Single value to operate on
     * @param otherNumberTuple The other tuple value to operate, ignored if null
     * @param operation The arithmetic operation to do using constants in {@link NumberTuple}
     * @return A copy of the source tuple after applying arithmetic operation
     */
    NumberTuple simpleOperation(int value, NumberTuple otherNumberTuple, char operation);
}
