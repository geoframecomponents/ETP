package etpClasses;

import static java.lang.Math.pow;
import static java.lang.Math.sin;

import org.joda.time.DateTime;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.exp;

public class Radiations2 implements RadiationMethods{
	
	double diffuseExtinctionCoefficient = 0.719;
	double leafScatteringCoefficient = 0.15;
	double canopyReflectionCoefficientDiffuse = 0.036;
	
	double leafSide;
	double longWaveEmittance; 
	double airTemperature; 
	double leafTemperature;
	double stefanBoltzmannConstant;
	
	double leafAreaIndex;
	double solarElevationAngle; 
	double shortWaveRadiationDirect;
	double shortWaveRadiationDiffuse;
	
	DateTime date;
	double latitude; 
	double longitude; 
	boolean doHourly;
	//double leafAreaIndex, double solarElevationAngle, 
	//double shortWaveRadiationDirect,double shortWaveRadiationDiffuse) {
	
	
	
	public Radiations2 (double diffuseExtinctionCoefficient,	double leafScatteringCoefficient,double canopyReflectionCoefficientDiffuse,
			double leafSide, double longWaveEmittance, double airTemperature, double leafTemperature, double stefanBoltzmannConstant,
			double leafAreaIndex, double solarElevationAngle, double shortWaveRadiationDirect, double shortWaveRadiationDiffuse){
		
		this.diffuseExtinctionCoefficient = diffuseExtinctionCoefficient;
		this.leafScatteringCoefficient = leafScatteringCoefficient;
		this.canopyReflectionCoefficientDiffuse = canopyReflectionCoefficientDiffuse;
		this.leafSide = leafSide;
		this.longWaveEmittance = longWaveEmittance; 
		this.airTemperature = airTemperature;
		this.leafTemperature = leafTemperature; 
		this.stefanBoltzmannConstant = stefanBoltzmannConstant;
		this.leafAreaIndex = leafAreaIndex;
		this.solarElevationAngle = solarElevationAngle;
		this.shortWaveRadiationDirect = shortWaveRadiationDirect; 
		this.shortWaveRadiationDiffuse = shortWaveRadiationDiffuse;
		
		this.date = date;
		this.latitude = latitude; 
		this.longitude = longitude; 
		this.doHourly = doHourly;
		}
	
	
	@Override

	
	public double computeLongWaveRadiationBalance() {
		// Compute the net long wave radiation i.e. the incoming minus outgoing [J m-2 s-1]
		double longWaveRadiation = 4 * leafSide * longWaveEmittance * stefanBoltzmannConstant * (((pow (airTemperature, 3))*leafTemperature - (pow (airTemperature, 4))));
		return longWaveRadiation;	
	}
	public double getSolarElevationAngle() {
		// from Iqbal, M. (2012). An introduction to solar radiation.
		// Latitude is in radiant
		// Longitude is in degrees
		int dayOfTheYear = date.getDayOfYear();
		double dayAngle = 2*PI*(dayOfTheYear-1)/365;
		double equationOfTime = 0.017 + 0.4281*cos(dayAngle) - 7.351*sin(dayAngle) - 3.349*cos(2*dayAngle) - 9.7331*sin(2*dayAngle);
		double solarNoon = 12 + (4*(15-longitude)-equationOfTime)/60;
		double hour=(doHourly==true)? (double)date.getMillisOfDay() / (1000 * 60 * 60):12.5;
		double hourAngleOfSun = PI*(hour - solarNoon)/12;
		double solarDeclinationAngle = -23.4*PI*cos(2*PI*(dayOfTheYear+10)/365)/180;
		double solarElevationAngle = sin(latitude)*sin(solarDeclinationAngle)+cos(latitude)*cos(solarDeclinationAngle)*cos(hourAngleOfSun);
		return solarElevationAngle;

	}
	
	public double computeAbsordebRadiationSunlit () {
    	double directExtinctionCoefficientInCanopy = 0.5/solarElevationAngle;
		double scatteredExtinctionCoefficient = 0.46/solarElevationAngle;
		double canopyReflectionCoefficientBeam = 1-exp((-2*0.041*directExtinctionCoefficientInCanopy)/(1+directExtinctionCoefficientInCanopy));
	    double directAbsorbedRadiation = shortWaveRadiationDirect*(1-leafScatteringCoefficient)*(1-exp(-directExtinctionCoefficientInCanopy*leafAreaIndex));
	    double diffuseAbsorbedRadiation = shortWaveRadiationDiffuse*(1-canopyReflectionCoefficientDiffuse)*
	    		(1-exp(-(diffuseExtinctionCoefficient+directExtinctionCoefficientInCanopy)*leafAreaIndex))*
	    		(diffuseExtinctionCoefficient/(diffuseExtinctionCoefficient+directExtinctionCoefficientInCanopy));
	    double scatteredAbsorbedRadiation = shortWaveRadiationDirect*((1-canopyReflectionCoefficientBeam)*
	    		(1-exp(-(directExtinctionCoefficientInCanopy+scatteredExtinctionCoefficient)*leafAreaIndex))*
	    		(scatteredExtinctionCoefficient/(directExtinctionCoefficientInCanopy+scatteredExtinctionCoefficient))-
	    		(1-leafScatteringCoefficient)*(1-exp(-2*directExtinctionCoefficientInCanopy*leafAreaIndex))/2);
	    double absordebRadiationSunlit = directAbsorbedRadiation + diffuseAbsorbedRadiation + scatteredAbsorbedRadiation;
	    return absordebRadiationSunlit;
	    }
	public double computeAbsordebRadiationShadow () {
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
	public double computeSunlitLeafAreaIndex () {
    	double directExtinctionCoefficientInCanopy = 0.5/solarElevationAngle;
    	double sunlitLeafAreaIndex = (1-exp(-directExtinctionCoefficientInCanopy*leafAreaIndex))/directExtinctionCoefficientInCanopy;
    	return sunlitLeafAreaIndex;
	}
}