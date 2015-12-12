package logic;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import tools.Arrays;

public class Sequence<T> implements Collection<T>, Cloneable{
    
    public int length;   
    protected Object[] array;    
    
    public Sequence(){
        array = new Object[0];   
        length=array.length;
    }
    
    public Sequence(T ... input){
        array = (Object[])input;
        length = array.length;
    }

    public void add(T ... in){
        array = tools.Arrays.add(array, in);
        length= array.length;
    }
    @Override
    public boolean add(T e) {
        array = Arrays.add(array, e);
        length = array.length;
        return true;
    }
    
    public void addAtBottom(T in){
        Object[] newar = new Object[array.length+1];
        System.arraycopy(array, 0, newar, 1, array.length);
        newar[0] = in;
        array = newar;
        length = newar.length;
    }
    
    public void addAt(T in, int i){
        if (i>array.length) throw new java.lang.IllegalArgumentException();
        
        Object[] newar = new Object[array.length+1];
        System.arraycopy(array, 0, newar, 0, i);
        System.arraycopy(array, i, newar, i+1, array.length-i);
        newar[i] = in;
        array = newar;
        length = array.length;
    }
    
    public void addAbove(T in, T anchor){
        if (!this.contains(anchor)) return;
        int i = this.getIndex(anchor);
        this.addAt(in, i+1);
    }
    
    public void move(int oldpos, int newpos){
        if (newpos>this.length-1 || oldpos>this.length-1) throw new java.lang.AssertionError();
        if (oldpos==newpos) return;
        Object[] newar = new Object[array.length];
        int j = 0;
        for (int i = 0; i < oldpos; i++) {
            if (j==newpos) j++;
            newar[j] = array[i];
            j++;
        }
        for (int i = oldpos+1; i < array.length; i++) {
            if (j==newpos)j++;
            newar[j] = array[i];
            j++;
        }
        newar[newpos] = array[oldpos];
        array = newar;
    }
    
    public void augment(Sequence<T> in){
        array = java.util.Arrays.copyOf(array, array.length+in.array.length);
        System.arraycopy(in.array, 0, array, length, in.array.length);
        length = array.length;
    }
    
    /**
     * Removes all Logically equivalent occurrences of parameter in
     * @param in 
     */
    @Override
    public boolean remove(Object in){   
        boolean out = false;
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(in)) {
                this.removeElementAt(i);
                out = true;
            }
        }     
        length = array.length;
        return out;
    }
    
    public void removeElementAt(int i){
        Object[] newar = new Object[array.length-1];
        System.arraycopy(array, 0, newar, 0, i);
        System.arraycopy(array, i+1, newar, i, newar.length-i);
        this.array = newar;
        length = array.length;
    }
    
    public int getIndex(T in){
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(in)) {
                return i;
            }
        }
        return -1;        
    }
    
    public T get(int n){
        if (n>-1) return (T)array[n];
        n = array.length+n; //(array.length = 5) n:  -1 -> 4 / -5 -> 0
        return (T)array[n];
    }
    
    public T getStrict(int n){
        return (T)array[n];
    }
    
    public T getEquivalent(T in){
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(in)) {
                return (T)array[i];
            }
        }
        return null;
    }
    
    public T getLast(){
        return (T)array[array.length-1];
    }
    
//    public T itemFromEnd(int n){
//        return (T)array[array.length-1-n];
//    }
    
    public void overWrite(T in, int index){
        array[index] = in;
    }
    
    /**
     * Returns a subsequence of this sequence from (including) the {@code int index} parameter position
     * @param i
     * @return 
     */
    public Sequence<T> subSequence(int index){
        Object[] outar = new Object[array.length-index];
        System.arraycopy(array, index, outar, 0, outar.length);
        Sequence<T> out = new Sequence();
        out.array = outar;
        out.length = out.array.length;
        return out;
    }
    /**
     * Returns a subsequence of this sequence from (including) the {@code int index} parameter position
     * and ending (excluding) the {@code int end} parameter position
     * <ul><li>{@code this.subSequence(0,this.length)}</ul> return a shallow copy of the original
     *
     * @param index
     * @param i
     * @return 
     */
    public Sequence<T> subSequence(int index, int end){
        Object[] outar = new Object[end-index];
        System.arraycopy(array, index, outar, 0, outar.length);
        Sequence<T> out = new Sequence();
        out.array = outar;
        out.length = out.array.length;
        return out;
    }
    
    public Sequence<T> subSequence(Schema<T> s){
        Sequence<T> out = new Sequence();
        for (Object o : array) {
            if (s.condition((T)o)) out.add((T)o);
        }
        return out;
    }
    
    public Sequence<T> cut(int begin, int end){        
        Sequence<T> out = this.subSequence(begin, end);
        Object[] newar = new Object[array.length-end+begin];
        int j=0;
        for (int i = 0; i < array.length; i++) {
            if (i<begin || i>=end) {
                newar[j] = array[i];
                j++;
            }
        }
        this.array = newar; this.length = newar.length;
        return out;
    }
    
    public void reverseOrder(){
        int c = array.length;
        Object[] newar = new Object[c];
        for (int i = 0; i < array.length; i++) {
            newar[c-i-1] = array[i];
        }
        array = newar;
    }
    
    public void order(){
        if (!checkSortable()) return;
        Object[] newdat = new Object[0];
        while (array.length>0) {
            int index = 0;
            double runningmin = getSortMetric(array[index]);
            for (int i = 1; i < array.length; i++) {
                if (this.getSortMetric(array[i])<runningmin){
                    runningmin = getSortMetric(array[i]);
                    index = i;
                }
            }
            newdat = tools.Arrays.add(newdat, array[index]);
            array = tools.Arrays.remove(array, index);
        }
        array = newdat;
        length = newdat.length;
    }
    
    public void orderReverse(){
        this.order();
        this.reverseOrder();
    }
    
    public void order(SortMetric<T> in){
        Object[] newdat = new Object[0];
        while (array.length>0) {
            int index= 0;
            double runningmin = in.getSortMetric((T)array[index]);
            for (int i = 1; i < array.length; i++) {
                double d = in.getSortMetric((T)array[i]);
                if (d<runningmin){
                    runningmin = d;
                    index = i;
                }
            }
            newdat = tools.Arrays.add(newdat,array[index]);
            array  = tools.Arrays.remove(array, index);
        }
        array = newdat;
        length = newdat.length;
    }
    
    public void orderReverse(SortMetric in){
        this.order(in);
        this.reverseOrder();
    }
    private boolean checkSortable(){
        Class c = ((T)array[0]).getClass();
        if (Sortable.class.isAssignableFrom(c)) return true;
        else if (c.equals(Integer.class) || c.equals(Short.class) || c.equals(Double.class) ||
                c.equals(Float.class) || c.equals(Long.class) || c.equals(Byte.class) || c.equals(String.class)) {
            return true;
        }else return false;
    }
    private double getSortMetric(Object o){
        if (o instanceof Sortable){
            return ((Sortable)o).getSortMetric();
        }else if (o instanceof Integer) {
            return (Integer)o;
        }else if (o instanceof Double) {
            return (Double)o;
        }else if (o instanceof Long) {
            return (Long)o;
        }else if (o instanceof Short) {
            return (Short)o;
        }else if (o instanceof Float) {
            return (Float)o;
        }else if (o instanceof Byte) {
            return (Byte)o;
        }else if (o instanceof String) {
            return ((String)o).length();
        }else throw new java.lang.AssertionError();
    }
    /**
     * Returns a subsequence of this Sequence, whose elements are a logical copy of this sequence's elements. <p>
     * This method is only supported for Logicals, non-logical sequences return shallow copies.
     * @param index
     * @return 
     */
    public Sequence<T> subSequenceCopy(int index){
        Object[] outar = new Object[array.length-index];
        int n=0;
        for (int i = index; i < array.length; i++) {
            if (array[i] instanceof Logical) {
                Logical l = (Logical) array[i];
                Logical newl = l.getDuplicate();
                outar[n] = (T)newl;                
            }else{
                outar[n] = array[i];
            }
            n++;
        }
        Sequence<T> out = new Sequence();
        out.array = outar;
        out.length = out.array.length;
        return out;
    }
    /**
     * Returns a subsequence of this Sequence, whose elements are a logical copy of this sequence's elements. <p>
     * This method is only supported for Logicals, non-logical sequences return shallow copies.
     * @param index
     * @return 
     */
    public Sequence<T> subSequenceCopy(int index, int end){
        Object[] outar = new Object[end-index];
        int n=0;
        for (int i = index; i < end; i++) {
            if (array[i] instanceof Logical) {
                Logical l = (Logical) array[i];
                Logical newl = l.getDuplicate();
                outar[n] = (T)newl;                
            }else{
                outar[n] = array[i];
            }
            n++;
        }
        Sequence<T> out = new Sequence();
        out.array = outar;
        out.length = out.array.length;
        return out;
    }
    
    public Sequence<T> subSequenceCopy(Schema<T> s){
        Sequence<T> out = new Sequence<>();
        for (Object o : array) {
            if (o instanceof Logical){
                Logical l = (Logical)o;
                out.add((T)l.getDuplicate());
            }else{
                out.add((T)o);
            }
        }
        return out;
    }
    
    public T[] toArray(Class<?> c){
        T[] out = (T[])Array.newInstance(c, this.length);
        for (int i = 0; i < out.length; i++) {
            out[i] = this.get(i);
        }
        return out;
    }
    
    /**
     * Returns a new sequence equivalent to A extended with B;
     */
    public static <T> Sequence<T> merge(Sequence<T> A, Sequence<T> B){
        Object[] ar = new Object[A.array.length+B.array.length];
        System.arraycopy(A.array, 0, ar, 0, A.array.length);
        System.arraycopy(B.array, 0, ar, A.array.length, B.array.length);
        Sequence<T> out = new Sequence();
        out.array = ar;
        out.length = out.array.length;
        return out;
    }
    
    @Override
    public Iterator<T> iterator(){
        return new Iterator<T>() {                                    
            int cursor = -1;
            boolean backward = false;
            
            @Override
            public boolean hasNext() {
                if (cursor<array.length-1) return true;
                else return false;
            }
            

            @Override
            public T next() {
                cursor++;
                return (T) array[cursor];                
            }
            
            public T previous(){
                cursor--;
                return (T) array[cursor];
            }
            
            public boolean iterate(){
                cursor++;
                return cursor<array.length;
            }
            
            public boolean iterateBackward(){
                backward = true;
                cursor++;
                return cursor<array.length;
            }
            
            public T get(){
                if (backward) return (T)array[array.length-cursor];
                return (T)array[cursor];
            }
        };
    }
    
    public Iterator<T> iterator(Schema<T> schema){
        return new Iterator<T>() {
            Schema<T> s= schema;
            int i=-2;
            @Override
            public boolean hasNext() {
                here:
                if (i==-2){
                    for (int j = 0; j < length; j++) {
                        if (s.condition(get(j))){
                            i=j;
                            break here;
                        }
                    }
                    i=-1;
                }
                
                return i!=-1;
            }

            @Override
            public T next() {
                T out = get(i);
                int m = i;
                for (int j = i+1; j < length; j++) {
                    if (s.condition(get(j))){
                        i=j; break;
                    }
                }
                if (i==m) i=-1;
                return out;
            }
        };
    }
    
    @Override
    public String toString(){
        String out = "⟨";
        for (Iterator<T> it = this.iterator();it.hasNext();) {
            T t = it.next();
            if (t==null)out+="null";
            else out+=t.toString();
            if (!it.hasNext()) break;
            out+=", ";
        }
        out+="⟩";        
        return out; 
    }

    @Override
    public int size() {
        return array.length;
    }

    @Override
    public boolean isEmpty() {
        return (array.length==0);
    }

    @Override
    public boolean contains(Object o) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(o))return true;
        }
        return false;
    }

    @Override
    public Object[] toArray() {
        return array.clone();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        T[] out = (T[])Array.newInstance(a.getClass().getComponentType(), array.length);
        System.arraycopy(array, 0, out, 0, array.length);
        return out;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object o : c) {
            boolean b = false;
            for (int i = 0; i < array.length; i++) {
                if (array[i].equals(o)){
                    b=true;
                    break;
                }
            }
            if (!b) return false;
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        boolean out = false;
        for (T t : c) {
            if (this.add(t)) out = true;
        }
        return out;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean out = false;
        for (int i = 0; i < array.length; i++) {
            for (Object o : c) {
                if (array[i].equals(o)){
                    array = Arrays.remove(array, i);
                    out = true;
                }
            }
        }
        length = array.length;
        return out;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        Object[] newar = new Object[0];
        for (int i = 0; i < array.length; i++) {
            for (Object o : c) {
                if (array[i].equals(o)){
                    newar = Arrays.add(newar, array[i]);
                    break;
                }
            }
        }
        boolean out = (array.length==newar.length);
        array = newar;
        length = array.length;
        return out;
    }

    @Override
    public void clear() {
        array = new Object[]{};
        length = 0;
    }

    @Override
    public Sequence<T> clone(){
        Sequence<T> out;
        try{
        out = (Sequence<T>)super.clone();
        out.array = out.array.clone();
        return out;
        }catch (java.lang.CloneNotSupportedException ex) {
            System.out.println("fail");
        }
        return null;
    }
    
}