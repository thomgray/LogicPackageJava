package logic.formula;

public class Sentence extends PrimeFormula{

    public Sentence(String in, int i) {
        super(in, i);
    }
    public Sentence(char in){
        this(String.valueOf(in),0);
    }
    
    @Override
    public Sentence clone(){
        return new Sentence(symbol);
    }
}