package etp;

import static java.lang.Math.exp;
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
		double waterMolarMass = 0.018;
		double stefanBoltzmannConstant = 5.670373 * pow(10,-8);
		double emissivity = 1.0;
		double latentHeatEvaporation = 2.45 * pow(10,6);
		double molarGasConstant = 8.314472;
		double airTemperature = 273.15-4.5;
		System.out.println("airTemperature " + airTemperature);
//		System.out.println("airTemperature2 " + 1/273.0);
//		System.out.println("airTemperature3 " + 1/airTemperature);
		double longWaveRadiation1 = 4 * 2 * emissivity * stefanBoltzmannConstant;
		System.out.println("longWaveRadiation1 " +longWaveRadiation1);
		double longWaveRadiation2 = (pow (airTemperature, 4));
		System.out.println("longWaveRadiation2 " +longWaveRadiation2);
		double longWaveRadiation = 2 * emissivity * stefanBoltzmannConstant * (pow (airTemperature, 4));
		System.out.println("longWaveRadiation " +longWaveRadiation);
		double saturationVaporPressure = 611 * exp((waterMolarMass*latentHeatEvaporation/molarGasConstant)*(1/273.0-1/airTemperature));
		System.out.println("saturationVaporPressure " +saturationVaporPressure);

//		private double computeDelta (double airTemperature) {.
		double numerator = 611 * waterMolarMass * latentHeatEvaporation;
		double exponential = exp((waterMolarMass * latentHeatEvaporation / molarGasConstant)*((1/273.0)-(1/airTemperature)));
		double denominator = (molarGasConstant * pow(airTemperature,2));
		double delta = numerator * exponential / denominator;
//			double numerator = 611 * waterMolarMass * latentHeatEvaporation;
//			double a = ((1/273)-(1/airTemperature));
//			double exponential = ((waterMolarMass * latentHeatEvaporation / molarGasConstant));
//			double denominator = (molarGasConstant * pow(airTemperature,2));
			//double delta = numerator * exponential / denominator;
			System.out.println("a ");
			System.out.println("exponential " + exponential);
			System.out.println("exponential ");
			System.out.println("numerator " + numerator);
			
			System.out.println("denominator " + denominator);
//			System.out.println("delta " + exp(waterMolarMass * latentHeatEvaporation) );
			System.out.println("delta " + delta ); 

			//		}
		//OmsSchymanskiOrET os = new OmsSchymanskiOrET();
		//os.
//		double airTemperature = 300.0;
//		double windVelocity = 5/3.6;
//		double atmosphericPressure = 1000;
//		double leafLength = 0.05;
//		int leafSide = 2;
//
//		System.out.println(leaf.emissivity);
		System.out.println("af");
//		SensibleHeatTransferCoefficient cH = new SensibleHeatTransferCoefficient();
//		LatentHeatTransferCoefficient cE = new LatentHeatTransferCoefficient();
//		double gec = cH.computeConvectiveTransferCoefficient(airTemperature, windVelocity, leafLength);
//		double mimmo = cH.computeSensibleHeatTransferCoefficient(gec, leafSide);
//		double frenchi = cE.computeLatentHeatTransferCoefficient(airTemperature, atmosphericPressure, leafSide, gec);
//		System.out.println(gec);
//		System.out.println(mimmo);
//		System.out.println(frenchi);
//		stoma.airTemperature = airTemperature; 
//		stoma.atmosphericPressure = 1; 
		 

		//System.out.println(stoma.airTemperature);

		//double stima = stoma.getStomatalConductance(airTemperature);
		//System.out.println("stima" + stima);
//		
//		HeatTransferCoefficient acca = new HeatTransferCoefficient();
//		acca.airTemperature = airTemperature; 
//		//acca.windVelocity = windVelocity;
////		double nn0 = acca.computeThermalConductivity(airTemperature);
////		
////		System.out.println("nn0// " + nn0);
//
//		double nn = acca.kinematicViscosity;
//		double nn7 = acca.computeKinematicViscosity(airTemperature);
//		double nn1 = acca.reynoldsNumber;
//		double nn8 = acca.computeReynoldsNumber(windVelocity, acca.leafLength, nn7);
//		double nn2 = acca.nusseltNumber;
//		double nn3 = acca.thermalConductivity;
//		double nn4 = acca.heatTransferCoefficient;
//		
//		
//		System.out.println("nn// " + nn);
//		System.out.println("nn7// " + nn7);
//		System.out.println("nn1// " + nn1);
//		System.out.println("nn8// " + nn8);
//		System.out.println("nn2// " + nn2);
//		System.out.println("nn3// " + nn3);
//		System.out.println("nn4// " + nn4);
		//double po = acca.computeThermalConductivity(airTemperature);

			}

}
