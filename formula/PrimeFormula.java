package logic.formula;

import java.util.HashMap;

public abstract class PrimeFormula extends Element{

    static char F1 = 'α';
    static HashMap<String,Character> primeMap = new HashMap();
    
    public Term[] arguments;
    
    public PrimeFormula(String in, int i) {
        super(in, i);
    }
    
    PrimeFormula(){}
    
    
    
    static char getPrimeSymbol(String in) {
        if (primeMap.containsKey(in)) {
            return primeMap.get(in);
        }
        primeMap.put(in, F1);
        char c = F1;
        F1++;
        return c;
    }
    
    public void replaceTerm(Term oldt, Term newt){
        if (this.isSentence()) return;
        for (int i=0; i<arguments.length;i++) {
            for (Term t : arguments[i].composition) {
                if (t.equals(oldt)) {
                    arguments[i].composition.replace(t, newt);
                    arguments[i]= arguments[i].composition.get(0);
                }
            }
        }
        reconstruct();
    }
    
    public Equality castToEquality(){
        return (Equality)this;
    }
    public Sentence castToSentence(){
        return (Sentence)this;
    }
    public Relation castToRelation(){
        return (Relation) this;
    }
    
    String getForm(){
        String f = String.valueOf(symbol);
        for (Term t : arguments) {
            f+=t.form;
        }
        return f;
    }
    
    private void reconstruct(){
        form = getForm();
        atomicForm = String.valueOf(getPrimeSymbol(form));
    }
    
}