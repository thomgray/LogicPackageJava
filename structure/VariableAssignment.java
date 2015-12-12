package logic.structure;

import logic.formula.Variable;

public class VariableAssignment<T> extends Operation<Variable,T>{

    public VariableAssignment(char s, T val, Variable v) {
        super(s, val, v);
    }
    
    public VariableAssignment(char s){
        super(s,1);
    }
    
}