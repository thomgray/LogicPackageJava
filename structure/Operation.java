package logic.structure;

import logic.Schema;
import logic.Sequence;
import logic.Set;

public class Operation<T,U>{
    char symbol;
    public int arity;
    Schema schema;
    Set<Map<T,U>> mapping;
    
    public Operation(char s, U val, T ... args){
        symbol = s;
        arity = args.length;
        mapping = new Set();
        mapping.add(new Map(val,args));
    }
    
    public Operation(char s, int ar){
        arity = ar;
        symbol = s;
        mapping = new Set();
    }
    
    public Operation(char s, Schema schem){
        symbol = s;
        schema = schem;
    }
    
    public U getValue(T ... args){
        Sequence<T> seq = new Sequence(args);
    
        for (Map<T, U> map : mapping) {
            if (seq.equals(map.arguments)) return map.value;
        }
        return null;
    }
    
    public void set(U val, T ... args){
        if (args.length!=arity) throw new java.lang.IllegalArgumentException("Inconsistent arity");
        mapping.add(new Map(val,args));
    }
}

class Map<T,U>{
    Sequence<T> arguments;
    U value;

    Map(U val, T ... args){
        arguments = new Sequence(args);
        value = val;
    }

    U getValue(T ... args){
        Sequence<T> seq = new Sequence(args);
        if (seq.equals(arguments)) return value;
        else return null;
    }

}