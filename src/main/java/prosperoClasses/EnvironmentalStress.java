//package prosperoMain;

//import static java.lang.Math.pow;
//import static org.jgrasstools.gears.libs.modules.JGTConstants.isNovalue;
//import static org.jgrasstools.gears.libs.modules.JGTConstants.isNovalue;
//import static java.lang.Math.abs;

package prosperoClasses;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.lang.Math;
import oms3.annotations.Author;
import oms3.annotations.Description;
import oms3.annotations.Execute;
import oms3.annotations.In;
import oms3.annotations.Keywords;
import oms3.annotations.Label;
import oms3.annotations.License;
import oms3.annotations.Name;
import oms3.annotations.Out;
import oms3.annotations.Status;
import oms3.annotations.Unit;
import prosperoClasses.*;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.SchemaException;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.jgrasstools.gears.libs.modules.JGTModel;
//import org.jgrasstools.gears.utils.CrsUtilities;
//import org.jgrasstools.gears.utils.geometry.GeometryUtilities;
import org.jgrasstools.hortonmachine.i18n.HortonMessageHandler;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.referencing.crs.CoordinateReferenceSystem;


import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
//import com.vividsolutions.jts.geom.Point;

public class EnvironmentalStress {
	
	/*@In public double alpha;
	@In public double theta;
	
	@In public double Th;
	@In public double Tl;
	@In public double T0;
	
	@In public double VPD0;
	
	@In public double f;
	@In public double thetaW;
	@In public double thetaC;*/
	
	public double computeRadiationStress(double shortWaveRadiation, double alpha, double theta) {
//		double shortWaveRadiationMicroMol=(shortWaveRadiation>0)?shortWaveRadiation/2.11:0;
		double shortWaveRadiationMicroMol=(shortWaveRadiation);
		double first = (alpha*shortWaveRadiationMicroMol)+1;

		double sqr1 = Math.pow(first, 2);
		double sqr2 = - 4*theta*alpha*shortWaveRadiationMicroMol;
		double sqr = sqr1+sqr2;
		double result = (1/(2*theta))*(alpha*shortWaveRadiationMicroMol+1-Math.sqrt((sqr))) ;
		return result;	
	}
	
	public double computeTemperatureStress(double airTemperature, double Tl, double Th, double T0) {
		airTemperature = airTemperature -273;
		double c = (Th-T0)/(T0-Tl);
		double b = 1/((T0-Tl)*Math.pow((Th-T0),c));
		double result = b* (airTemperature - Tl)* Math.pow((Th-airTemperature),c);
		//System.out.println("the second result    "+result);
		
		return result;	
	}
	
	public double computeVapourPressureStress(double vapourPressureDeficit, double VPD0) {
		double result = Math.exp(-vapourPressureDeficit/VPD0);
		return result;	
	}
	public double computeFAOWaterStress(double soilMoisture, double waterFieldCapacity, 
			double waterWiltingPoint, double rootsDepth, double depletionFraction) {
		double totalAvailableWater = 1000*(waterFieldCapacity - waterWiltingPoint)*rootsDepth;
	    double readilyAvailableWater = totalAvailableWater * depletionFraction;
	    double rootZoneDepletation = 1000 * (waterFieldCapacity - soilMoisture) * rootsDepth;
	    double waterStressCoefficient=(rootZoneDepletation<readilyAvailableWater)? 1:(totalAvailableWater - rootZoneDepletation) / (totalAvailableWater - readilyAvailableWater);
		return waterStressCoefficient;	
	}
	
	
	
	public double computeWaterStress(double soilMoisture, double f, double thetaW, double thetaC) {
		double beta;
		if (soilMoisture < thetaW) {
			beta = 0;		
			}
		else if(soilMoisture > thetaW && soilMoisture < thetaC) {
			beta = (soilMoisture - thetaW)/(thetaC - thetaW);
			}
		else {
			beta = 1;
			}
		double result = (1 - Math.exp(-f*beta))/(1 - Math.exp(-f));
		//System.out.println("Beta is:   "+beta);	
		return result;	
	}
/*	private double computeDewPointTemperature(double airTemperature, double relativeHumidity) {
		double dewPointTemperature = airTemperature - (100-(relativeHumidity*100))/5;
		return dewPointTemperature;
	}
	private double computeVapourPressureDewPoint(double airTemperature) {
		double t = 1-(373.15/(airTemperature+273.15));// - (100-(relativeHumidity*100))/5;
		double expo = Math.exp(13.3185 * t - 1.976 * Math.pow(t,2) - 0.6445 * Math.pow(t,3) - 0.1229 * Math.pow(t,4));
		return expo;
	}*/

	
	/*	outStressResistance = new HashMap<Integer, double[]>();
	
						
			double airTemperature = inAirTemperature.get(basinId)[0];
			if (airTemperature == (nullValue)) {airTemperature = defaultAirTemperature;}
			if (airTemperature > 200) {airTemperature = airTemperature-273;}		

				
			double shortWaveRadiationDirect = inShortWaveRadiationDirect.get(basinId)[0];
			if (shortWaveRadiationDirect == nullValue) {shortWaveRadiationDirect = defaultShortWaveRadiationDirect;}
			
			double soilMosture = defaultSoilMosture;
			if (inSoilMosture != null){soilMosture = inSoilMosture.get(basinId)[0];}
			if (soilMosture == (nullValue)) {soilMosture = defaultSoilMosture;}
			
			double relativeHumidity = defaultRelativeHumidity;
			if (inRelativeHumidity != null){relativeHumidity = inRelativeHumidity.get(basinId)[0];}
			if (relativeHumidity == nullValue) {relativeHumidity = defaultRelativeHumidity;}				
			
			PressureMethods pressure = new PressureMethods(); 
			
			double atmosphericPressure = 101325;
			if (inAtmosphericPressure != null){atmosphericPressure = inAtmosphericPressure.get(basinId)[0];}
			if (atmosphericPressure == nullValue) {atmosphericPressure = pressure.computePressure(defaultAtmosphericPressure, massAirMolecule, gravityConstant, elevation,boltzmannConstant, airTemperature);;}			
		
			double saturationVaporPressure = pressure.computeSaturationVaporPressure(airTemperature+273, waterMolarMass, latentHeatEvaporation, molarGasConstant);			
			double vaporPressure = relativeHumidity * saturationVaporPressure/100.0;
			double vaporPressureDew = computeVapourPressureDewPoint(airTemperature);		
			double vapourPressureDeficit = (vaporPressure - vaporPressureDew)/1000;
			double shortWaveRadiationMicroMol=(shortWaveRadiationDirect>0)?shortWaveRadiationDirect/2.11:0;

			double radiationStress = computeRadiationStress(shortWaveRadiationMicroMol, alpha, theta);//, b10);
			double c = (Th-T0)/(T0-Tl);
			double b = 1/((T0-Tl)*Math.pow((Th-T0),c));
			double dewPointTemperature  = computeDewPointTemperature(airTemperature, relativeHumidity);
			double temperatureStress = computeTemperatureStress(airTemperature, b, c, Tl,  Th);
			double vapourPressureStress = computeVapourPressureStress(vapourPressureDeficit,VPD0);
			double waterStress = computeWaterStress(soilMosture, f, thetaW, thetaC);
			
		
			outStress = radiationStress * waterStress;// * temperatureStress * vapourPressureStress ;
			storeResult((Integer)basinId,outStress);
			
			}
		}*/

	

	
	
	
	
	/*private void storeResult(int ID,double output) //, double latentHeatShadow,double totalTranspiration) 
			throws SchemaException {
		outStressResistance.put(		ID, new double[]{output});
	//	outLatentHeatShadow.put(		ID, new double[]{latentHeatShadow});
	//	outTranspiration.put(	ID, new double[]{totalTranspiration});
		}
	
	/*private Point[] getPoint(Coordinate coordinate, CoordinateReferenceSystem sourceCRS, CoordinateReferenceSystem targetCRS)
			throws Exception{
		Point[] point = new Point[] { GeometryUtilities.gf().createPoint(coordinate) };
		CrsUtilities.reproject(sourceCRS, targetCRS, point);
		return point;
	}*/
	/*private LinkedHashMap<Integer, Coordinate> getCoordinate(int nStaz,
			SimpleFeatureCollection collection, String idField)
					throws Exception {
		LinkedHashMap<Integer, Coordinate> id2CoordinatesMcovarianceMatrix = new LinkedHashMap<Integer, Coordinate>();
		FeatureIterator<SimpleFeature> iterator = collection.features();
		Coordinate coordinate = null;
		try {
			while (iterator.hasNext()) {
				SimpleFeature feature = iterator.next();
				int name = ((Number) feature.getAttribute(idField)).intValue();
				coordinate = ((Geometry) feature.getDefaultGeometry())
						.getCentroid().getCoordinate();
				double z = 0;
				if (centroidElevation != null) {
					try {
						z = ((Number) feature.getAttribute(centroidElevation))
								.doubleValue();
					} catch (NullPointerException e) {
						pm.errorMessage(msg.message("kriging.noPointZ"));
						throw new Exception(msg.message("kriging.noPointZ"));
					}
				}
				coordinate.z = z;
				id2CoordinatesMcovarianceMatrix.put(name, coordinate);
			}
		} finally {
			iterator.close();
		}

		return id2CoordinatesMcovarianceMatrix;
	}*/
		
}

