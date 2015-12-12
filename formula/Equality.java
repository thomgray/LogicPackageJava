package logic.formula;

import logic.Syntax;
import logic.Tree;

public class Equality extends PrimeFormula{
    private String[] stringArgs = new String[2];

    public Equality(String in, int i) {
        super(in, i);
        arguments = new Term[2];
        stringArgs[1] = getRightTerm(in,i+1);
        stringArgs[0] = getLeftTerm(in,i-1);
        
        for (int j = 0; j < arguments.length; j++) {
            if (Syntax.isFunction(stringArgs[j].charAt(0))) {
                arguments[j] = new Function(stringArgs[j],0);
            }else{
                arguments[j] = new Variable(stringArgs[j],0);
            }
        }
        
        form = getForm();
        atomicForm = String.valueOf(getPrimeSymbol(form));
    }
    
    public Equality(Term t1, Term t2){
        arguments = new Term[]{(Term)t1.clone(),(Term)t2.clone()};
        form = arguments[0].form+"="+arguments[1].form;
        stringArgs[0] = arguments[0].form;
        stringArgs[1] = arguments[1].form;
        atomicForm = String.valueOf(getPrimeSymbol(form));
    }
    
    private String getRightTerm(String in, int i){
        String out = Term.parseTerm(in,i);
        
        if (out == null) throw new logic.MalFormedException();
        else return out;
    }
    
    private String getLeftTerm(String in, int i){
        if (Syntax.isVariable(in.charAt(i))||Syntax.isFunction(in.charAt(i))) 
            return String.valueOf(in.charAt(i));
        else if (in.charAt(i)!=')') throw new logic.MalFormedException();
        int lr=0,m=i;
        try{
            do {
                if (in.charAt(m)=='(') lr++;
                else if (in.charAt(m)==')') lr--;
                
                m--;
            } while (lr<0);
            
        }catch (java.lang.StringIndexOutOfBoundsException e){
            throw new logic.MalFormedException();
        }
        //m== index of function symbol f: f(...)
        return in.substring(m, i+1);
    }
    
    public Term getLeftArgument(){
        return arguments[0];
    }
    public Term getRightArgument(){
        return arguments[1];
    }
    public Term getCorrespondingArgument(Term in){
        if (arguments[0].equals(in)) return arguments[1];
        else if (arguments[1].equals(in)) return arguments[0];
        else return null;
    }
    
    public static Equality getEquality(Term t1, Term t2){
        String s = t1.form+"="+t2.form;
        return new Equality(s, t1.form.length());
    }
    
    public boolean equates(Term t1, Term t2){
        if (arguments[0].equals(t1) && arguments[1].equals(t2)) {
            return true;
        }else if (arguments[0].equals(t2) && arguments[1].equals(t1)) {
            return true;
        }else return false;
    }
    
    public boolean equates(Term t){
        return (arguments[0].equals(t) || arguments[1].equals(t));
    }
    
    @Override
    String getForm(){
        return arguments[0].form+"="+arguments[1].form;
    }
    
    
    @Override
    public Equality clone(){
        return new Equality((Term)arguments[0].clone(), (Term)arguments[1].clone());
    }
}