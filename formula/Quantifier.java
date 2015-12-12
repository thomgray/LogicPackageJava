package logic.formula;

import logic.Syntax;

public class Quantifier extends Connective{
    public static final char UNIVERSAL =  '∀', EXISTENTIAL = '∃';
    
    public Variable boundVariable;

    public Quantifier(String in, int i) {
        super(in, i);
        if(in.length()<=i+1 || !Syntax.isVariable(in.charAt(i+1))) 
            throw new logic.MalFormedException();
        
        boundVariable = new Variable(in,i+1);
        form = form+boundVariable.form;
        atomicForm = form;
    }
    
    @Override
    public Quantifier clone(){
        return new Quantifier(form,0);
    }
}