package prosperoClasses;

import static java.lang.Math.PI;
import static java.lang.Math.pow;

public class Leaf {
	
	public double length = 0.25;
	public int side = 2;
	public int stomaSide = 1;

	public double area = PI*pow(length/2,2);
	
	public double poreRadius = 22 * pow(10,-6);
	public double poreDensity = 35 * pow(10,6);
	public double poreArea = pow(poreRadius,2)*PI;
	public double poreDepth= 2.5 * pow(10,-5);
	
	public double shortWaveAbsorption = 0.8;	
	public double shortWaveReflectance = 0.2;	
	public double shortWaveTransmittance = 0;
	
	public double longWaveAbsorption = 0.8;	
	public double longWaveReflectance = 0.2;	
	public double longWaveTransmittance = 0;
	public double longWaveEmittance = 0.95;

}
