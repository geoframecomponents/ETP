package etpPointCase;

public class ContextET {
	
//	public String etMethod;

    private Evapotranspiration eT;
    public  ContextET(Evapotranspiration eT){
       this.eT=eT;
    }
    public int execute_Evapotranspiration( ){
        return eT.doET( );
    }

}