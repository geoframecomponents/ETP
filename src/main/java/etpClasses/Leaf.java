package etpClasses;

import static java.lang.Math.PI;
import static java.lang.Math.pow;

public class Leaf {
	
	
	/*double poreRadius = 18.5 * pow(10,-6);
	double poreArea = pow(poreRadius,2)*PI;
	double poreDepth = 3 * pow(10,-5);
	double poreDensity = 32.75 * pow(10,6);
	double leafLength = 0.05;
	int leafSide = 2;
	// Shortwave property
	double shortWaveAbsorption = 0.8;	
	double shortWaveReflectance = 0.2;	
	double shortWaveTransmittance = 0;
	// Longwave property
	double longWaveAbsorption = 0.6;	
	double longWaveReflectance = 0.4;	
	double longWaveTransmittance = 0;
	double longWaveEmittance = 1;*/
	
	
	public double length = 0.05;
	public int side = 2;
	public double area = PI*pow(length/2,2);
	//public double temperature;
	
	public double poreRadius = 18.5 * pow(10,-6);
	public double poreDensity = 32.75 * pow(10,6);
	public double poreArea = pow(poreRadius,2)*PI;
	public double poreDepth= 3 * pow(10,-5);
	
	public double shortWaveAbsorption = 0.8;	
	public double shortWaveReflectance = 0.2;	
	public double shortWaveTransmittance = 0;
	
	public double longWaveAbsorption = 0.8;	
	public double longWaveReflectance = 0.2;	
	public double longWaveTransmittance = 0;
	public double longWaveEmittance = 1;

}
