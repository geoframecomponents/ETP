package etp;
//import java.util.*;
//import static java.lang.Math.exp;
import static java.lang.Math.pow;

//import java.util.HashMap;
//import java.util.Set;
//import java.util.Map.Entry;
//
//import oms3.annotations.Description;
//import oms3.annotations.Execute;
//import oms3.annotations.Out;
//import oms3.annotations.Unit;

//import static java.lang.Math.pow;

public class tester {

	public static void main(String[] args) {
		int i; 
		int numberOfLayers = 7;
		//int i; 
		double leafAbsorption = 0.8;
		double leafTransmittance = 0.1;
		int A[] = new int[numberOfLayers]; //caricamento
		//while(A.hasMoreElements())
	       //  System.out.print(A.nextElement() + " ");
		//System.out.print(A+"     ");
		//for(i=numberOfLayers;i==0;i--) {
			//absorbedRadiation[i]=(shortWaveRadiation*);
		for(i=0;i<A.length;i++) {
			double aaa = pow(leafTransmittance,i);
			double bbb = aaa * leafAbsorption;
			
			System.out.print(" "+aaa );
			System.out.print(" "+bbb );}
		
		//for(i=0;i<numberOfLayers;i++)
		//A[i]=i;
		//stampa
		//for(i=0;i<numberOfLayers;i++)
			//System.out.print(" "+A[i] ); //}
		//for(i=0;i<A.length;i++)
			//System.out.print(" "+A[i] ); //}
		//int b = A[]*3;
	}

}
