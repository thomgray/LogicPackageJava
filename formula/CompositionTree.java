package logic.formula;

import java.util.Iterator;
import logic.Tree;

public class CompositionTree extends Tree<Term>{
    
    public CompositionTree(Term in){
        super.add(in);
        for (Iterator<Term> it = this.iterateChildren(in);it.hasNext();) {
            Term t =it.next();
            this.placeAbove(in, t);
        }
    }
    
    @Override
    public void placeAbove(Term anchor, Term in){
        super.placeAbove(anchor, in);
        for (Iterator<Term> it = this.iterateChildren(in);it.hasNext();) {
            Term t = it.next();
            this.placeAbove(in, t);
        }
    }
    
    @Override
    public void replace(Term oldt, Term newt){
        super.replace(oldt, newt.composition);
        Term par = this.getParent(newt);
        if (par!=null) recalculate(par);
    }

    private void recalculate(Term par) {
        String s = par.symbol+"(";
        int i =0;
        for (Iterator<Term> it = this.iterateChildren(par);it.hasNext();) {
            Term kid = it.next();
            s+=kid.form;
        }
        s+=")";
        Term out = Term.newTerm(s, 0);
        this.replace(par, out);
    }
    
}