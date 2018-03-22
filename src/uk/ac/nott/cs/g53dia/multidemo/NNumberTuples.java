package uk.ac.nott.cs.g53dia.multidemo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class NNumberTuples implements NumberTuple {
    private List<Integer> val;

    NNumberTuples(Integer[] val) {
        this(val, val.length);
    }

    NNumberTuples(Integer[] val, int size) {
        if(val.length != size) {
            throw new ArrayIndexOutOfBoundsException("Value array length of " + val.length + " does not match specified size " + size);
        }
        this.val = new ArrayList<>(size);
        this.val.addAll(Arrays.asList(val));
    }

    @Override
    public int getValue(int valuePosition) {
        if(val.size() < valuePosition) {
            throw new ArrayIndexOutOfBoundsException("Value tuple index is only sized " + val.size() + " but requested index " + valuePosition);
        }
        return val.get(valuePosition);
    }

    @Override
    public int getMin() {
        return Collections.min(val);
    }

    @Override
    public int getMax() {
        return Collections.max(val);
    }

    @Override
    public NumberTuple simpleOperation(int value, NumberTuple otherNumberTuple, char operation) {
        return null;
    }

    public int getSize() {
        return val.size();
    }
}
