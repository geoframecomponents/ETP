package prospero;

//import static java.lang.Math.pow;
//import static org.jgrasstools.gears.libs.modules.JGTConstants.isNovalue;
//import static org.jgrasstools.gears.libs.modules.JGTConstants.isNovalue;
//import static java.lang.Math.abs;


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

/*
* This file is part of JGrasstools (http://www.jgrasstools.org)
* (C) HydroloGIS - www.hydrologis.com
*
* JGrasstools is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
@Description("Calculates evapotranspiration at hourly/daily timestep using Schimanski & Or formula")
@Author(name = "Michele Bottazzi", contact = "michele.bottazzi@gmail.com")
@Keywords("Evapotranspiration, Hydrology")
@Label("")
@Name("")
@Status(Status.CERTIFIED)
@License("General Public License Version 3 (GPLv3)")

public class OmsProsperoStress extends JGTModel implements Parameters{

	
	/////////////////////////////////////////////
	// ENVIRONMENTAL VARIABLES - INPUT
	/////////////////////////////////////////////

	@Description("Air temperature.")
	@In
	@Unit("K")
	public HashMap<Integer, double[]> inAirTemperature;
	
	@Description("The air relative humidity.")
	@In
	@Unit("%")
	public HashMap<Integer, double[]> inRelativeHumidity;
	
	@Description("The short wave radiation at the surface.")
	@In
	@Unit("W m-2")
	public HashMap<Integer, double[]> inShortWaveRadiationDirect;
	
	@Description("The atmospheric pressure.")
	@In
	@Unit("Pa")
	public HashMap<Integer, double[]> inAtmosphericPressure;
	
	@Description("The short wave radiation at the surface.")
	@In
	@Unit("-")
	public HashMap<Integer, double[]> inSoilMosture;
	
	
	
	/////////////////////////////////////////////
	// ENVIRONMENTAL VARIABLES - DEFAULT
	/////////////////////////////////////////////

	@Description("The air temperature default value in case of missing data.")
	@In
	@Unit("K")
	public double defaultAirTemperature = 15.0+273.0;
	  
	@Description("The humidity default value in case of missing data.")
	@In
	@Unit("%")
	public double defaultRelativeHumidity = 70.0;
		
	@Description("The short wave radiation default value in case of missing data.")
	@In
	@Unit("W m-2")
	public double defaultShortWaveRadiationDirect = 0.0;
	
	@Description("The atmospheric pressure default value in case of missing data.")
	@In
	@Unit("Pa")
	public double defaultAtmosphericPressure = 101325.0;
	
	@Description("The soil mosture default value in case of missing data.")
	@In
	@Unit("-")
	public double defaultSoilMosture = 0.3;
		
	@Description("The shape file with the station measuremnts")
	@In
	public SimpleFeatureCollection inCentroids;

	@Description("The name of the field containing the ID of the station in the shape file")
	@In
	public String idCentroids;

	@Description(" The vetor containing the id of the station")
	Object []basinId;
	
	@Description("List of the latitudes of the station ")
	ArrayList <Double> latitudeStation= new ArrayList <Double>();
	
	@Description("The map of the Digital Elevation Model")
	@In
	public GridCoverage2D inDem;

	@Description("the linked HashMap with the coordinate of the stations")
	LinkedHashMap<Integer, Coordinate> stationCoordinates;
	
	@Description("Final target CRS")
	CoordinateReferenceSystem targetCRS = DefaultGeographicCRS.WGS84;

	@Description("Type of transpiring area")
	@In
	public String typeOfTerrainCover;

	

	@Description("It is needed to iterate on the date")
	int step;

	@Description("The first day of the simulation.")
	@In
	public String tStartDate;
	
	@Description("The first day of the simulation.")
	@In
	public int temporalStep;
	
	
	@In	public double alpha;
	@In public double theta;
	//@In public double d;
	@In public double VPD0;

	@In	public double T0;
	@In public double Tl;
	@In public double Th;
	
	//@In	public double b5;
	@In public double f;
	@In public double thetaW;
	@In public double thetaC;
//	@In double b10;
	
	public DateTime date;
	double nullValue = -9999.0;
	public int time;
	private HortonMessageHandler msg = HortonMessageHandler.getInstance();
	DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").withZone(DateTimeZone.UTC);
	


	@Description("The total stress.")
	@Unit("-")
	@Out
//	public HashMap<Integer, double[]> outStressResistance;
	public HashMap<Integer, double[]> outStressResistance= new HashMap<Integer, double[]>() ;
	
	/////////////////////////////////////////////
	// OTHERS - DO
	/////////////////////////////////////////////
	
	@Description("Switch that defines if it is hourly.")
	@In
	public boolean doHourly = true;
	
	@Description("Choose if you want to print only the latent heat or all the other outputs.")
	@In
	public boolean doFullPrint = true;
	
	//@Description("Choose if you want to use the multi-layer canopy model or a simple transpiring surface.")
	//@In
	//public boolean doMultiLayer = true;

	@Description("The elevation of the centroid.")
	@In
	@Unit("m")
	public double elevation;
	
	@Out
	double outStress;

	@Execute
	public void process() throws Exception {		
		outStressResistance = new HashMap<Integer, double[]>();
		//stationCoordinates = getCoordinate(0,inCentroids, idCentroids);
		//Iterator<Integer> idIterator = stationCoordinates.keySet().iterator();
		
		Set<Entry<Integer, double[]>> entrySet = inAirTemperature.entrySet();
		for( Entry<Integer, double[]> entry : entrySet ) {
			Integer basinId = entry.getKey();
		//	Coordinate coordinate = (Coordinate) stationCoordinates.get(idIterator.next());
			//double elevation = coordinate.z;
						
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
		}

	
	private double computeRadiationStress(double shortWaveRadiationMicroMol, double alpha, double theta) {
		double first = (alpha*shortWaveRadiationMicroMol)+1;
		double sqr1 = Math.pow(first, 2);
		double sqr2 = - 4*theta*alpha*shortWaveRadiationMicroMol;
		double sqr = sqr1+sqr2;
		double result = (1/(2*theta))*(alpha*shortWaveRadiationMicroMol+1-Math.sqrt((sqr))) ;
		return result;	
	}
	
	private double computeTemperatureStress(double airTemperature, double b, double c, double Tl, double Th) {
		double result = b* (airTemperature - Tl)* Math.pow((Th-airTemperature),c);
		//System.out.println("the second result    "+result);

		return result;	
	}
	
	private double computeVapourPressureStress(double vapourPressureDeficit, double VPD0) {
		double result = Math.exp(-vapourPressureDeficit/VPD0);
		return result;	
	}
	private double computeWaterStress(double soilMosture, double b6, double thetaW, double thetaC) {
		double beta;
		if (soilMosture < thetaW) {
			beta = 0;		
			}
		else if(soilMosture > thetaW && soilMosture < thetaC) {
			beta = (soilMosture - thetaW)/(thetaC - thetaW);
			}
		else {
			beta = 1;
			}
		double result = (1 - Math.exp(-b6*beta))/(1 - Math.exp(-b6));
		//System.out.println("Beta is:   "+beta);	
		return result;	
	}
	private double computeDewPointTemperature(double airTemperature, double relativeHumidity) {
		double dewPointTemperature = airTemperature - (100-(relativeHumidity*100))/5;
		return dewPointTemperature;
	}
	private double computeVapourPressureDewPoint(double airTemperature) {
		double t = 1-(373.15/(airTemperature+273.15));// - (100-(relativeHumidity*100))/5;
		double expo = Math.exp(13.3185 * t - 1.976 * Math.pow(t,2) - 0.6445 * Math.pow(t,3) - 0.1229 * Math.pow(t,4));
		return expo;
	}
	
	
	private void storeResult(int ID,double output) //, double latentHeatShadow,double totalTranspiration) 
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





