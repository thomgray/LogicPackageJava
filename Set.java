package logic;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import tools.Arrays;

public class Set<T> implements Collection<T>, Comparable<Set>, Cloneable{
    
    public int cardinality;       
    Object[] elements = new Object[0];
    
    public Set(){
    }
    
    public Set(T ... elements){        
        this.elements = (Object[])elements;        
        this.removeDuplicates();
    }
    
    /**
     * Adds parameter element ONLY if it is not already contained in the Set
     * @param in 
     */
    @Override
    public boolean add(T in){
        if (getElement(in)!=null) return false;
        
        elements = java.util.Arrays.copyOf(elements, elements.length+1);
        elements[elements.length-1] = in;
        cardinality=elements.length;
        return true;
    }
    
    public void add(T ... in){
        for (T t : in) {
            this.add(t);
        }
    }
    
    /**
     * Return the runtime element logically equivalent to the parameter argument. 
     * Null if not contained
     */
    public T getElement(T in){
        for (int i = 0; i < elements.length; i++) {
            if (elements[i].equals(in)) {
                return (T)elements[i];
            }
        }
        return null;
    }

    /**
     * @deprecated 
     * use remove
     * @param in 
     * 
     */
    public void removeElement(T in){
        Object[] newels = new Object[0];
        for (int i = 0; i < elements.length; i++) {
            if (elements[i].equals(in)) 
                newels = tools.Arrays.add(newels, elements[i]);
            
        }
        this.elements = newels;
        cardinality = elements.length;
    }
    
    public void replace(T oldt, T newt){
        for (int i = 0; i < elements.length; i++) {
            if (elements[i].equals(newt)) {
                elements[i] = newt;
                return;
            }
        }
    }
    
    public T[] getArray(Class T){
        return (T[])java.util.Arrays.copyOf(elements, cardinality, T);
    }
    
    public static <T> Set<T> union(Set<T> A, Set<T> B){
        Object[] newels = new Object[A.elements.length+B.elements.length];
        System.arraycopy(A.elements, 0, newels, 0, A.cardinality);
        System.arraycopy(B.elements, 0, newels, A.cardinality, B.cardinality);
        
        Set<T> out = new Set<T>();
        out.elements = newels;
        out.removeDuplicates();
        out.cardinality = out.elements.length;
        return out;      
    }
    
    public void union(Set<T> A){
        Object[] newels = new Object[this.elements.length+A.elements.length];
        System.arraycopy(this.elements, 0, newels, 0, this.cardinality);
        System.arraycopy(A.elements, 0, newels, this.cardinality, A.cardinality);
        
        elements = newels;
        this.removeDuplicates();
        this.cardinality = elements.length;
    }
    
    public static <T> Set<T> intersection(Set<T> A, Set<T> B){
        int n=0;
        boolean[] temp = new boolean[A.elements.length];
        for (int i = 0; i < A.elements.length; i++) {
            for (int j = 0; j < B.elements.length; j++) {
                if (A.elements[i].equals(B.elements[j])) {
                    temp[i] = true;
                    n++;
                    break;
                }
            }
        }
        
        Object[] newels = new Object[n];
        n=0;
        for (int i = 0; i < A.elements.length; i++) {
            if (temp[i]) {
                newels[n] = A.elements[i];
                n++;
            }
        }
        
        Set<T> out = new Set();
        out.elements = newels;
        out.cardinality = newels.length;
        return out;       
    }
    
    public Set<T> relativeComplement(Set<T> coset){
        Set<T> out = new Set();
        for (T t : coset) {
            if (!this.contains(t)) out.add(t);
        }
        return out;
    }
    
    public Set<Set<T>> powerSet(){
        int x = (int)Math.pow(2, cardinality);
        boolean[][] map = new boolean[cardinality][x];
        for (int i = 0; i < cardinality; i++) {
            boolean val = true;
            int m = 0;
            for (int j = 0; j < x; j++) {
                map[i][j] = val;
                m++;
                if (m==Math.pow(2, cardinality-i-1)){
                    val = !val;
                    m=0;
                }
            }
        }
        
        Set<Set<T>> out = new Set<>();
        
        for (int i = 0; i < x; i++) {
            Set<T> el = new Set<>();
            for (int j = 0; j < cardinality; j++) {
                if (map[j][i]) {
                    el.add((T)elements[j]);
                }
            }
            out.add(el);
        }
        out.sortAscending();
        return out;
    }
    
    public Set<T> subSet(Schema in){
        Set<T> out = new Set();
        for (int i = 0; i < elements.length; i++) {
            if (in.condition(elements[i])) {
                out.add((T)elements[i]);
            }
        }
        return out;
    }
    
    /**
     * Return a subset of this set including the first parameter {@code int i} elements under the parameter sort metric
     * @param met
     * @param i
     * @return 
     */
    public Set<T> subset(SortMetric met, int i){
        Set<T> out = new Set();
        Set<T> source = this.clone();
        source.sortAscending(met);
        for (int j = 0; j < i; j++) {
            out.add((T)source.elements[j]);
        }
        return out;
    }
    
    public static <T> Set<T> fromSchema(Schema in, Set<T> domain){
        return domain.subSet(in);
    }
    
    @Override
    public boolean contains(Object in){        
        for (int i = 0; i < elements.length; i++) {
            if (elements[i].equals(in)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean isSubset(Set<T> in){
        for (T t : this) {
            if (!in.contains(t)) {
                return false;
            }
        }
        return true;
    }
    
    public boolean isSubsetProper(Set<T> in){
        for (T t : this) {
            if (!in.contains(t)) {
                return false;
            }
        }
        return !in.isSubset(this);
    }
      
    public void reverseOrder(){
        Object[] newels = new Object[elements.length];
        for (int i = 0; i < elements.length; i++) {
            newels[newels.length-i-1] = elements[i];
        }
        elements = newels;        
    }
    
    public void sortAscending(){
        for (int i = 0; i < elements.length; i++) {
            if (!checkSortable(elements[i])) return;                            
        }
        Set<T> mirror = new Set();
        while (elements.length>0) {
            double min= calculateSortMetric(elements[0]);
            int n=0;
            for (int i = 1; i < elements.length; i++) {
                double x = calculateSortMetric(elements[i]);
                if (x<min) {
                    n=i; min = x;
                }
            }
            mirror.add((T)elements[n]);
            this.removeElement((T)elements[n]);
        }
        this.elements=mirror.elements;
        this.cardinality = elements.length;
    }
    
    public void sortAscending(SortMetric<T> in){
        Set<T> mirror = new Set();
        while (this.elements.length>0) {
            T minT=(T)this.elements[0];
            double min= in.getSortMetric(minT);
            for (T t : this) {
                double x = in.getSortMetric(t);
                if (x<min){
                    min = x;
                    minT = t;
                }
            }
            mirror.add(minT);
            this.removeElement(minT);
        }
        this.elements = mirror.elements;
        this.cardinality = elements.length;
    }
    
    public void sortDescending(){
        sortAscending();
        reverseOrder();        
    }
    
    public void sortDescending(SortMetric<T> in){
        sortAscending(in);
        reverseOrder();
    }
    
    private double calculateSortMetric(Object in){
        if (in instanceof Logical) {
            return ((Logical)in).getSortMetric();
        }else if (in instanceof Integer) {
            return ((Integer)in).doubleValue();
        }else if (in instanceof Double) {
            return (Double)in;
        }else if (in instanceof Float) {
            return ((Float)in).doubleValue();
        }else if (in instanceof Long) {
            return ((Long)in).doubleValue();
        }else if (in instanceof Short) {
            return ((Short)in).doubleValue();
        }else if (in instanceof Byte) {
            return ((Byte)in).doubleValue();
        }else if (in instanceof String) {
            return (double)((String)in).length();
        }else{
            throw new java.lang.UnsupportedOperationException();
        }
        
    }
    
    private boolean checkSortable(Object in){
        if (in instanceof Sortable) {
            return true;
        }else if (in instanceof Integer) {
            return true;
        }else if (in instanceof Double) {
            return true;
        }else if (in instanceof Float) {
            return true;
        }else if (in instanceof Long) {
            return true;
        }else if (in instanceof Short) {
            return true;
        }else if (in instanceof Byte) {
            return true;
        }else if (in instanceof String){
            return true;
        }else{
            return false;
        }
    }
    
    private void removeDuplicates() {          
        boolean[] temp = new boolean[elements.length];
        int n = 0;
        for (int i = 0; i < elements.length; i++) {
            boolean include = true;
            for (int j = 0; j < i; j++) {
                if (elements[i].equals(elements[j])) {
                    include = false;
                    break;
                }
            }            
            if(include) n++;
            temp[i] = include;
        }
        Object[] newels = new Object[n];
        n=0;
        for (int i = 0; i < elements.length; i++) {
            if (temp[i]) {
                newels[n] = elements[i];
                n++;
            }
        }
        elements = newels;
        cardinality = elements.length;
    }
    
    public Sequence<T> toSequence(){
        Sequence<T> out = new Sequence();
        for (T t : this) {
            out.add(t);
        }
        return out;
    }


    @Override
    public String toString(){
        if (cardinality ==0) return "∅";
        String out = "{";
        for (Iterator<T> it = this.iterator();it.hasNext();) {
            out+=it.next().toString();
            if (!it.hasNext()) break;
            out+=", ";
        }
        out+="}";
        return out;
    }

    @Override
    public Iterator<T> iterator(){
        T t;
        if (this.elements.length>0) t = (T)elements[0];
        else t=null;
        return new Iterator<T>() {
            int i = 0;
            Object[] clone = elements.clone();
            @Override
            public boolean hasNext() {
                return i<elements.length;                             
            }

            @Override
            public T next() {
                T out = (T)elements[i];
                i++;
                return out;
            }
            
        };
    }
    
    public Iterator<T> iterator(Schema<T> schema){
        return new Iterator<T>() {
            int c = -2;
            Schema<T> s = schema;        
            @Override
            public boolean hasNext() {
                here:
                if (c==-2){
                    for (int i = 0; i < elements.length; i++) {
                        if (s.condition((T)elements[i])){
                            c=i; break here;
                        }
                    }
                    c=-1;
                }
                return c!=-1;
            }

            @Override
            public T next() {
                T out = (T)elements[c];
                int m = c;
                for (int i = m+1; i < elements.length; i++) {
                    if (s.condition((T)elements[i])){
                        c=i; break;
                    }
                }
                if (c==m) c=-1;
                return out;
            }
        };
    }

    @Override
    public int size() {
        return elements.length;
    }

    @Override
    public boolean isEmpty() {
        return (elements.length==0);
    }


    @Override
    public Object[] toArray() {
        return elements.clone();
    }

    public T[] toArray(Class c){
        T[] out = (T[]) Array.newInstance(c, elements.length);
        System.arraycopy(elements, 0, out, 0, elements.length);
        return out;
    }
    
    @Override
    public <T> T[] toArray(T[] a) {
        T[] out = (T[])Array.newInstance(a.getClass().getComponentType(), elements.length);
        System.arraycopy(elements, 0, out, 0, elements.length);
        return out;
    }


    @Override
    public boolean remove(Object o) {
        boolean out = false;
        for (int i = 0; i < elements.length; i++) {
            if (elements[i].equals(o)){
                elements = Arrays.remove(elements, i);
                out = true;
            }
        }
        cardinality = elements.length;
        return out;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object o : c) {
            boolean containsC = false;
            for (int i = 0; i < elements.length; i++) {
                if (elements[i].equals(o)){
                    containsC = true;
                    break;
                }
            }
            if (!containsC) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        boolean out = false;
        for (T t : c) {
            if (this.add(t)) out = true;
        }
        cardinality = elements.length;
        return out;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean out = false;
        for (Object o : c) {
            for (int i = 0; i < elements.length; i++) {
                if (elements[i].equals(o)) {
                    elements= Arrays.remove(elements, i);
                    out = true;
                    break;
                }
            }
        }
        this.cardinality = elements.length;
        return out;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        Object[] newar = new Object[]{};
        for (int i = 0; i < elements.length; i++) {
            for (Object o : c) {
                if (elements[i].equals(o)){
                    newar = Arrays.add(newar, elements[i]);
                    break;
                }  
            }

        }
        boolean out = (elements.length==newar.length);
        elements = newar;
        cardinality = elements.length;
        return out;
    }

    @Override
    public void clear() {
        elements = new Object[]{};
        this.cardinality = 0;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Set)) return false;
        Set s = (Set)obj;
        return (s.containsAll(this)&&this.containsAll(s));
    }
    
    @Override
    protected Set<T> clone(){
        Set<T> out = new Set();
        for (T t : this) {
            out.add(t);
        }
        return out;
    }

    /**
     * 0 return is not equivalent to .equals = true
     * @param o
     * @return 
     */
    @Override
    public int compareTo(Set o) {
        if (this.equals(o)) return 0;
        if (this.size()>o.size()) return -1;
        else if (this.size()<o.size()) return 1;
        else return 0;
    }
    

}