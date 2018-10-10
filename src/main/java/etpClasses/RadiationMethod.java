package etpClasses;

import static java.lang.Math.pow;
import static java.lang.Math.exp;

public class RadiationMethod {
	double diffuseExtinctionCoefficient = 0.719;
	
	double leafScatteringCoefficient = 0.15;
	double canopyReflectionCoefficientDiffuse = 0.036;
	
	public double computeLongWaveRadiationBalance(double leafSide, double longWaveEmittance, double airTemperature, double leafTemperature, double stefanBoltzmannConstant) {
		// Compute the net long wave radiation i.e. the incoming minus outgoing [J m-2 s-1]
		double longWaveRadiation = 4 * leafSide * longWaveEmittance * stefanBoltzmannConstant * (((pow (airTemperature, 3))*leafTemperature - (pow (airTemperature, 4))));
		return longWaveRadiation;	
	}
	
	public double computeAbsordebRadiationSunlit (double leafAreaIndex, double solarElevationAngle, double shortWaveRadiationDirect,double shortWaveRadiationDiffuse) {
    	//System.out.println("LAI	" +leafAreaIndex);

    	//System.out.println("solarElevationAngle		"+solarElevationAngle);

    	//System.out.println("shortWaveRadiationDirect	"+shortWaveRadiationDirect);

    	//System.out.println("shortWaveRadiationDiffuse	"+shortWaveRadiationDiffuse);

    	
    	double directExtinctionCoefficientInCanopy = 0.5/solarElevationAngle;
    	//System.out.println("directExtinctionCoefficientInCanopy		"+directExtinctionCoefficientInCanopy);
		double scatteredExtinctionCoefficient = 0.46/solarElevationAngle;
    	//System.out.println("scatteredExtinctionCoefficient		"+scatteredExtinctionCoefficient);

		double canopyReflectionCoefficientBeam = 1-exp((-2*0.041*directExtinctionCoefficientInCanopy)/(1+directExtinctionCoefficientInCanopy));
    	//System.out.println("canopyReflectionCoefficientBeam		"+canopyReflectionCoefficientBeam);

	    double directAbsorbedRadiation = shortWaveRadiationDirect*(1-leafScatteringCoefficient)*(1-exp(-directExtinctionCoefficientInCanopy*leafAreaIndex));
    	//System.out.println("directAbsorbedRadiation				"+directAbsorbedRadiation);

	    double diffuseAbsorbedRadiation = shortWaveRadiationDiffuse*(1-canopyReflectionCoefficientDiffuse)*
	    		(1-exp(-(diffuseExtinctionCoefficient+directExtinctionCoefficientInCanopy)*leafAreaIndex))*
	    		(diffuseExtinctionCoefficient/(diffuseExtinctionCoefficient+directExtinctionCoefficientInCanopy));
    	//System.out.println("diffuseAbsorbedRadiation			"+diffuseAbsorbedRadiation);

	    double scatteredAbsorbedRadiation = shortWaveRadiationDirect*((1-canopyReflectionCoefficientBeam)*
	    		(1-exp(-(directExtinctionCoefficientInCanopy+scatteredExtinctionCoefficient)*leafAreaIndex))*
	    		(scatteredExtinctionCoefficient/(directExtinctionCoefficientInCanopy+scatteredExtinctionCoefficient))-
	    		(1-leafScatteringCoefficient)*(1-exp(-2*directExtinctionCoefficientInCanopy*leafAreaIndex))/2);
    	//System.out.println("scatteredAbsorbedRadiation			"+scatteredAbsorbedRadiation);

	    double absordebRadiationSunlit = directAbsorbedRadiation + diffuseAbsorbedRadiation + scatteredAbsorbedRadiation;
    	//System.out.println("absordebRadiationSunlit			"+diffuseAbsorbedRadiation);

	    return absordebRadiationSunlit;
	    }
	public double computeAbsordebRadiationShadow (double leafAreaIndex, double solarElevationAngle, double shortWaveRadiationDirect,double shortWaveRadiationDiffuse) {
    	double directExtinctionCoefficientInCanopy = 0.5/solarElevationAngle;
		double scatteredExtinctionCoefficient = 0.46/solarElevationAngle;
		double canopyReflectionCoefficientBeam = 1-exp((-2*0.041*directExtinctionCoefficientInCanopy)/(1+directExtinctionCoefficientInCanopy));		
		double diffuseAbsorbedRadiationShadow = shortWaveRadiationDiffuse*(	1-canopyReflectionCoefficientBeam)*
				(1-exp(-diffuseExtinctionCoefficient*leafAreaIndex)-(1-exp(-(diffuseExtinctionCoefficient+directExtinctionCoefficientInCanopy)*leafAreaIndex))*
				(diffuseExtinctionCoefficient/(diffuseExtinctionCoefficient+directExtinctionCoefficientInCanopy)));
		double scatteredAbsorbedRadiationShadow = shortWaveRadiationDirect*((1-canopyReflectionCoefficientBeam)*(1-exp(-scatteredExtinctionCoefficient*leafAreaIndex)-		    		
				(1-exp(-(scatteredExtinctionCoefficient+directExtinctionCoefficientInCanopy)*leafAreaIndex))*
		    	(scatteredExtinctionCoefficient/(scatteredExtinctionCoefficient+directExtinctionCoefficientInCanopy))) - 		
		    	(1-leafScatteringCoefficient)*(1-exp(-directExtinctionCoefficientInCanopy*leafAreaIndex)-
		    	(1-exp(-2*directExtinctionCoefficientInCanopy*leafAreaIndex))/2));
		double absordebRadiationShadow = scatteredAbsorbedRadiationShadow + diffuseAbsorbedRadiationShadow;
	    return absordebRadiationShadow;
	    }	
	public double computeSunlitLeafAreaIndex (double leafAreaIndex, double solarElevationAngle) {
    	double directExtinctionCoefficientInCanopy = 0.5/solarElevationAngle;
    	double sunlitLeafAreaIndex = (1-exp(-directExtinctionCoefficientInCanopy*leafAreaIndex))/directExtinctionCoefficientInCanopy;
    	return sunlitLeafAreaIndex;
	}
}