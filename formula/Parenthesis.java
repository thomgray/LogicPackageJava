package logic.formula;

public class Parenthesis extends Element{

    public Parenthesis(String in, int i) {
        super(in, i);
    }
    public Parenthesis(char in){
        this(String.valueOf(in),0);
    }

    @Override
    public Parenthesis clone(){
        return new Parenthesis(symbol);
    }
}