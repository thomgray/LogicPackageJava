package logic;

import java.util.Iterator;

public class ConstructionTree extends Tree<Formula>{
    public ConstructionTree(Formula in){
        this.add(in);
//        if (in.construction!=null){
//            for (Iterator<Formula> it = this.iterateChildren(in);it.hasNext();) {
//                this.placeAbove(in, it.next());
//            }
//        }
    }
    
    @Override
    public void placeAbove(Formula anchor, Formula in){
        super.placeAbove(anchor, in);
        ConstructionTree cons = in.getConstruction();
        for (Iterator<Formula> it = cons.iterateChildren(in); it.hasNext();) {
            Formula f = it.next();
            this.placeAbove(in, f);
        }
    }
    
    @Override
    public void replace(Formula oldf, Formula newf){
        super.replace(oldf, newf.getConstruction());
        Formula par = this.getParent(newf);
        if (par!=null) this.reCalculate(par);
    }
    
    private void reCalculate(Formula in){
        if (in.connective.isBiconditional()) {
            replace(in, Formula.getBiconditional(getChild(in, 0), getChild(in,1)));
        }else if (in.connective.isConditional()) {
            replace(in, Formula.getConditional(getChild(in, 0), getChild(in,1)));
        }else if (in.connective.isConjunction()) {
            replace(in, Formula.getConjunction(getChild(in, 0), getChild(in,1)));
        }else if (in.connective.isDisjunction()) {
            replace(in, Formula.getDisjunction(getChild(in, 0), getChild(in,1)));
        }else if (in.connective.isNegation()) {
            replace(in, Formula.getNegationStrict(getChild(in, 0)));
        }else if (in.connective.isExistentialQuantifier() || in.connective.isUniveralQuantifier()) {
            char v = in.connective.castToQuantifier().boundVariable.symbol;
            char Q = in.connective.symbol;
            replace(in, Formula.getQuantification(getChild(in, 0), Q, v));
        }
    }
    
   
}