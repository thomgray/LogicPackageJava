package logic;

import java.util.Comparator;

public abstract class SortMetric<T> implements Comparator<T>{
    
    public abstract double getSortMetric(T in);

    @Override
    abstract public int compare(T o1, T o2);
}