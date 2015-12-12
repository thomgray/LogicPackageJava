package logic.formula;

import java.util.Iterator;
import logic.Set;
import logic.Syntax;
import logic.Tree;
import logic.Tree.TreeIterator;

public abstract class Term extends Element{
    public CompositionTree composition;
    Term[] arguments;

    /**
     *
     * @param in
     * @param i
     */
    public Term(String in, int i){
        super(in,i);
        composition = new CompositionTree(this);
    }
    
    
    protected static String parseTerm(String in, int i){
        
        if (Syntax.isVariable(in.charAt(i))) return String.valueOf(in.charAt(i));
        else if (!Syntax.isFunction(in.charAt(i))) return null;
        if (in.length()<i+2 || in.charAt(i+1)!='(') return String.valueOf(in.charAt(i));
        
        int lr=0, m=i;
        try{
            do {
                m++;
               if (in.charAt(m)=='(') lr++;
                else if (in.charAt(m)==')') lr--; 
            } while (lr>0);
        }catch (java.lang.StringIndexOutOfBoundsException e){
            return null;
        }
        //m = index of final )
        return in.substring(i, m+1);
    }
    
    static Term newTerm(String in, int i){
        if (Syntax.isFunction(in.charAt(i))) return new Function(in,i);
        else if (Syntax.isVariable(in.charAt(i))) return new Variable(in,i);
        else throw new java.lang.AssertionError();
    }
    
    
    public Term getApproachingTermThroughEquivalence(Term master, Equality eq){
        Term t = (Term)this.clone();
        Term eq1 = eq.getLeftArgument(), eq2 = eq.getRightArgument();
        CompositionTree ttree = t.composition, mtree = master.composition;
        for (TreeIterator it = (TreeIterator)ttree.iterateForward();it.hasNext();) {
            Term tterm = (Term)it.next();
            int[] ad = ttree.getAddress(tterm);
            Term mterm = mtree.get(ad);
            if (tterm.equals(mterm)){
                it.skipThisNode();
            }
            else if(eq.equates(tterm, mterm)){
                ttree.replace(tterm, (Term) mterm.clone());
                continue;
            }else if(tterm.symbol==mterm.symbol) continue;
            else{
                it.skipThisNode();
            }
        }
        return t.composition.get(0);
    }
    
    public boolean isEquivalentToThisReplacingTforX(Term in, Term t, Term x){
        CompositionTree ttree = this.composition, mtree = in.composition;
        //Term t=t;
        for (TreeIterator it = (TreeIterator)ttree.iterateForward();it.hasNext();){
            Term tterm = (Term)it.next();
            int[] ad = ttree.getAddress(tterm);
            Term mterm = mtree.get(ad);
            if (tterm.equals(mterm)){
                it.skipThisNode();
            }else if(mterm.equals(x)){
                if (t==null){
                    t=tterm;
                    it.skipThisNode();
                }
                if (tterm.equals(t)) it.skipThisNode();
                else return false;
            }else if(tterm.symbol==mterm.symbol) continue;
            else return false;
        }
        return true;
    }

}