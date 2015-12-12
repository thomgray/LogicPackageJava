package logic.structure;

import logic.Sequence;
import logic.Set;

public class Relation<T> extends Set<Sequence<T>>{
    public int arity;
    public char symbol;
    
    public Relation(char s, T ... args){
        symbol = s;
        arity = args.length;
        this.add(new Sequence(args));
    }
    
    public void addElement(T ... args){
        if (args.length!=arity) throw new java.lang.IllegalArgumentException("Relation is of arity "+arity);
        this.add(new Sequence(args));
    }
    
}