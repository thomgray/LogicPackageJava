package logic.formula;

import logic.Formula;
import logic.Logical;
import logic.MalFormedException;
import logic.Syntax;

public abstract class Element implements Cloneable{
    public String form;
    public String atomicForm;
    public char symbol;

    public Element(String in, int i){
        symbol = in.charAt(i);
        form = String.valueOf(symbol);
        atomicForm = form;
    }
    
    Element(){}
    
    public boolean isSentence(){
        return (this instanceof Sentence);
    }
    public boolean isConnective(){
        return (this instanceof Connective);
    }
    public boolean isParenthesis(){
        return (this instanceof Parenthesis);
    }
    public boolean isRelation(){
        return (this instanceof Relation);
    }
    public boolean isEquality(){
        return (this instanceof Equality);
    }
    public boolean isPrimeFormula(){
        return (this.isRelation() || this.isEquality() || this.isSentence());
    }
    public boolean isFunction(){
        return (this instanceof Function);
    }
    public boolean isVariable(){
        return(this instanceof Variable);
    }
    public boolean isTerm(){
        return(this instanceof Term);
    }
    public boolean isQuantifier(){
        return (this instanceof Quantifier);
    }
    public boolean isQuantifier(char q){
        return this.symbolIs(q);
    }
    
    public Element newElement(String in, int i, int log){
        char c = in.charAt(i);
        
        if (Syntax.isConnective(c)) {
            return new Connective(in,i);
        }else if (c=='('||c==')') {
            return new Parenthesis(in,i);
        }
        
        if (log==Logical.SENTENTIAL_LOGIC) {
            if (Syntax.isSentence(c)) {
                return new Sentence(in,i);
            }
        }else if (log==Logical.PREDICATE_LOGIC_FO) {
            if (Syntax.isFunction(c)) {
                return new Function(in,i);
            }else if (Syntax.isQuantifier(c)) {
                return new Quantifier(in,i);
            }else if (Syntax.isRelation(c)) {
                return new Relation(in,i);
            }else if (Syntax.isVariable(c)) {
                return new Variable(in,i);
            }else if (c=='=') {
                return new Equality(in,i);
            }
        }
        throw new logic.MalFormedException();
    }
    
    public Equality castToEquality(){
        return (Equality)this;
    }
    public Relation castToRelation(){
        return (Relation)this;
    }
    public Sentence castToSentence(){
        return (Sentence)this;
    }
    
    public boolean symbolIs(char x){
        return x==this.symbol;
    }
    
    @Override
    public String toString(){
       return this.form; 
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Element){
            return (this.form.equals(((Element)obj).form));
        }else if ((obj instanceof Formula) && (this instanceof PrimeFormula)) {
            return (this.form.equals(((Formula)obj).form));
        }else return false;
    }

    @Override
    public abstract Element clone();
    
}