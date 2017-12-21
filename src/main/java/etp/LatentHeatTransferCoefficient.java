package etp;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import static java.lang.Math.PI;

public class LatentHeatTransferCoefficient {
	double airSpecificHeat = 1010;
	double airDensity = 1.2690;
	double molarGasConstant = 8.314472;
	double molarVolume = 0.023;
	double poreRadius = 18.5 * pow(10,-6);
	double poreDensity = 32.75 * pow(10,6);
	double poreArea = pow(poreRadius,2)*PI;
	double poreDepth= 25 * pow(10,-6);
	double constantTerm= 1/(4*poreRadius) - 1/(PI * (1/sqrt(poreDensity)));
	double waterMolarMass = 0.018;
	double latentHeatEvaporation = 2.45*pow(10,6);
	

	public double computeLatentHeatTransferCoefficient (double airTemperature, double atmosphericPressure, int leafSide,double convectiveTransferCoefficient) {
		double thermalDiffusivity = 1.32 * pow(10,-7) * airTemperature - 1.73 * pow(10,-5);
		double binaryDiffusionCoefficient = 1.49 * pow(10,-7) * airTemperature - 1.96 * pow(10,-5);
		double ratio = binaryDiffusionCoefficient/molarVolume;
		double lewisNumber = thermalDiffusivity/binaryDiffusionCoefficient;
		double throatResistance = poreDepth/(poreArea*ratio*poreDensity);
		double vapourResistance = constantTerm * 1/(ratio * poreDensity);
		double molarStomatalConductance = 1/(throatResistance + vapourResistance);
		double stomatalConductance = molarStomatalConductance * atmosphericPressure / (molarGasConstant * airTemperature) ;
		double boundaryLayerConductance = leafSide*convectiveTransferCoefficient/(airSpecificHeat*airDensity*pow(lewisNumber,0.66));
		double totalConductance = 1/ ((1/stomatalConductance) + (1/boundaryLayerConductance));
		double molarTotalConductance = totalConductance/40;
		double latentHeatTransferCoefficient = waterMolarMass * latentHeatEvaporation * molarTotalConductance / atmosphericPressure;
		return latentHeatTransferCoefficient;	
		}
	}