package logic.formula;

import logic.Syntax;

public class Variable extends Term{

    public Variable(String in, int i) {
        super(in,i);
        if (!Syntax.isVariable(in.charAt(i))) throw new java.lang.IllegalArgumentException();
    }
    public Variable(char in){
        this(String.valueOf(in),0);
    }
    
    @Override
    public Variable clone(){
        return new Variable(this.symbol);
    }
}