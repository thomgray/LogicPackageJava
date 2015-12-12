package logic.formula;

import logic.Syntax;

public class Function extends Term{

    public Function(String in, int i) {
        super(in, i);
        form = Term.parseTerm(in, i);
        String[] args = new String[0];
        if (form.length()>1) {
            args = getStringArgs(form.substring(2, form.length()-1), 0);
        }
        
        
        for (int j = 0; j < args.length; j++) {
            Term t = Term.newTerm(args[j], 0);
            composition.placeAbove(this, t.composition.getClone());
        }
        
    }
    public Function(String in){
        this(in,0);
    }
    
    private String[] getStringArgs(String in, int i) {
        String[] out = new String[0];
        int x=0;
        do {
            String s = Term.parseTerm(in, i+x);
            out = tools.Arrays.add(out, s);
            x+=s.length();
           
            if (in.length()<x+1) break;
            if (!Syntax.isFunction(in.charAt(x)) && !Syntax.isVariable(in.charAt(x))) break;
        } while (true);
        return out;
    }
    
    @Override
    public Function clone(){
        return new Function(form);
    }
}