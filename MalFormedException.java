package logic;

public class MalFormedException extends RuntimeException{
    public MalFormedException(String message){
        super(message);
    }
    
    public MalFormedException(){
        super();
    }
    
    public MalFormedException(String form, int logic){
        super("Formula '"+form+"' is not well formed in "+Logical.literal(logic));
    }
    
}