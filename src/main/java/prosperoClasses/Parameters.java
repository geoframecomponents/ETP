package prosperoClasses;

import static java.lang.Math.pow;

public interface Parameters {
	
	double criticalReynoldsNumber = 3000; 	//fixed
	double prandtlNumber = 0.71; 			// fixed
	
	
	double airSpecificHeat = 1010;
	double airDensity = 1.2690;
	double molarGasConstant = 8.314472;
	double molarVolume = 0.023;
	double waterMolarMass = 0.018;
	double latentHeatEvaporation = 2.45*pow(10,6);
	double stefanBoltzmannConstant = 5.670373 * pow(10,-8);
	double boltzmannConstant = 1.38066*pow(10,-23); 
	double gravityConstant = 9.80665;
	double massAirMolecule = 29*1.66054*pow(10,-27); 

	
	
}
