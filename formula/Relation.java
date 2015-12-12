package logic.formula;

import logic.Sequence;
import logic.Syntax;

public class Relation extends PrimeFormula{
    String[] stringArgs = new String[0];
    
    public Relation(String in, int i) {
        super(in, i);
        
        try{
            getStringArgs(in,i);
        }catch (java.lang.StringIndexOutOfBoundsException e){
            throw new logic.MalFormedException();
        }
        
        for (int j = 0; j < stringArgs.length; j++) {
            form+=stringArgs[j];
        }
        atomicForm = String.valueOf(getPrimeSymbol(form));
        
        arguments = new Term[stringArgs.length];
        for (int j = 0; j < stringArgs.length; j++) {
            arguments[j] = Term.newTerm(stringArgs[j], 0);
        }
        
    }
    public Relation(String in){
        this(in,0);
    }

    private void getStringArgs(String in, int i) {
        int x=1;
        do {
            String s = Term.parseTerm(in, i+x);
            stringArgs = tools.Arrays.add(stringArgs, s);
           
            x+=s.length();
           
            if (in.length()<i+x+1) break;
            if (!Syntax.isFunction(in.charAt(i+x)) && !Syntax.isVariable(in.charAt(i+x))) break;
        } while (true);
        
    }
    
    public Sequence<Term> getArguments(){
        return new Sequence<>((Term[])arguments);
    }
    
    
    @Override
    public Relation clone(){
        return new Relation(form,0);
    }
    
}