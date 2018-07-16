package etpPointCase;
import static java.lang.Math.pow;
//import static org.jgrasstools.gears.libs.modules.JGTConstants.isNovalue;
//import static org.jgrasstools.gears.libs.modules.JGTConstants.isNovalue;
import static java.lang.Math.abs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;

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

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.SchemaException;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.jgrasstools.gears.libs.modules.JGTModel;
import org.jgrasstools.gears.utils.CrsUtilities;
import org.jgrasstools.gears.utils.geometry.GeometryUtilities;
import org.jgrasstools.hortonmachine.i18n.HortonMessageHandler;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.referencing.crs.CoordinateReferenceSystem;


import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import etpClasses.*;

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
public class OmsTranspiration extends JGTModel implements Parameters {
	
	// ENVIRONMENTAL VARIABLES
	@Description("Air temperature.")
	@In
	@Unit("K")
	public HashMap<Integer, double[]> inAirTemperature;
	
	@Description("The air temperature default value in case of missing data.")
	@In
	@Unit("K")
	public double defaultAirTemperature = 15.0+273.0;
	  
	@Description("The wind speed.")
	@In
	@Unit("m s-1")
	public HashMap<Integer, double[]> inWindVelocity;
	
	@Description("The wind default value in case of missing data.")
	@In
	@Unit("m s-1")
	public double defaultWindVelocity = 2.0;
	
	@Description("The air relative humidity.")
	@In
	@Unit("%")
	public HashMap<Integer, double[]> inRelativeHumidity;
	
	@Description("The humidity default value in case of missing data.")
	@In
	@Unit("%")
	public double defaultRelativeHumidity = 70.0;
	
	@Description("The short wave radiation at the surface.")
	@In
	@Unit("W m-2")
	public HashMap<Integer, double[]> inShortWaveRadiationDirect;
	
	@Description("The short wave radiation default value in case of missing data.")
	@In
	@Unit("W m-2")
	public double defaultShortWaveRadiationDirect = 0.0;
	
	@Description("The short wave radiation at the surface.")
	@In
	@Unit("W m-2")
	public HashMap<Integer, double[]> inShortWaveRadiationDiffuse;
	
	@Description("The long wave radiation at the surface.")
	@In
	@Unit("W m-2")
	public HashMap<Integer, double[]> inLongWaveRadiation;

	@Description("The atmospheric pressure.")
	@In
	@Unit("Pa")
	public HashMap<Integer, double[]> inAtmosphericPressure;
	
	@Description("The atmospheric pressure default value in case of missing data.")
	@In
	@Unit("Pa")
	public double defaultAtmosphericPressure = 101325.0;
	
	@Description("The soilflux.")
	@In
	@Unit("W m-2")
	public HashMap<Integer, double[]> inSoilFlux;
	
	@Description("The soilflux default value in case of missing data.")
	@In
	@Unit("W m-2")
	public double defaultSoilFlux = 0.0;
	
	// GEOGRAPHIC VARIABLES
	@Description("The elevation of the centroid.")
	@In
	@Unit("m")
	public String centroidElevation;
	
	@Description("The elevation of the centroid.")
	@In
	@Unit("m")
	public double elevation;
	
	@Description("The latitude of the centroid.")
	@In
	@Unit("°")
	public double latitude;
	
	@Description("The longitude of the centroid.")
	@In
	@Unit("°")
	public double longitude;
	
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
	
	// ECOLOGICAL VARIABLES
	
	@Description("Leaf area index.")
	@In
	@Unit("m2 m-2")
	public HashMap<Integer, double[]> inLeafAreaIndex;
	
	@Description("The leaf area index default value in case of missing data.")
	@In
	@Unit("m2 m-2")
	public double defaultLeafAreaIndex = 1.0;
	
	// OUTPUT
	@Description("The reference evapotranspiration.")
	@Unit("mm day-1")
	@Out
	
	public HashMap<Integer, double[]> outTranspiration;
	@Description("The reference evapotranspiration.")
	@Unit("mm day-1")
	@Out
	public HashMap<Integer, double[]> outLeafTemperature;
	
	// OTHERS
	
	@Description("Switch that defines if it is hourly.")
	@In
	public boolean doHourly = true;

	@Description("It is needed to iterate on the date")
	int step;

	@Description("The first day of the simulation.")
	@In
	public String tStartDate;
	
	double nullValue = -9999.0;
	public int time;
	private HortonMessageHandler msg = HortonMessageHandler.getInstance();
	DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").withZone(DateTimeZone.UTC);

	// METHODS FROM CLASSES		
	SensibleHeatMethods sensibleHeat = new SensibleHeatMethods();
	LatentHeatMethods latentHeat = new LatentHeatMethods();
	PressureMethods pressure = new PressureMethods(); 
	RadiationMethods radiationMethods = new RadiationMethods();
	SolarGeometry solarGeometry = new SolarGeometry();
	@Execute
	public void process() throws Exception {
		if (doHourly == true) {
			time =3600;
			} else {
			time = 86400;
			}
		DateTime startDateTime = formatter.parseDateTime(tStartDate);
		DateTime date=(doHourly==false)?startDateTime.plusDays(step).plusHours(12):startDateTime.plusHours(step);	
		stationCoordinates = getCoordinate(0,inCentroids, idCentroids);
		Iterator<Integer> idIterator = stationCoordinates.keySet().iterator();
		CoordinateReferenceSystem sourceCRS = inDem.getCoordinateReferenceSystem2D();
		
		Leaf propertyOfLeaf = new Leaf();
		double poreRadius = propertyOfLeaf.poreRadius;
		double poreArea = propertyOfLeaf.poreArea;
		double poreDepth = propertyOfLeaf.poreDepth;
		double poreDensity = propertyOfLeaf.poreDensity;
		double leafLength = propertyOfLeaf.length;
		int leafSide = propertyOfLeaf.side;
		// Shortwave property
	
		// Longwave property
		//double longWaveAbsorption = propertyOfLeaf.longWaveAbsorption;	
		//double longWaveReflectance = propertyOfLeaf.longWaveReflectance;	
		//double longWaveTransmittance = propertyOfLeaf.longWaveTransmittance;
		double longWaveEmittance = propertyOfLeaf.longWaveEmittance;
		
		outTranspiration = new HashMap<Integer, double[]>();
		outLeafTemperature = new HashMap<Integer, double[]>();
		Set<Entry<Integer, double[]>> entrySet = inAirTemperature.entrySet();
		for( Entry<Integer, double[]> entry : entrySet ) {
			Integer basinId = entry.getKey();
			Coordinate coordinate = (Coordinate) stationCoordinates.get(idIterator.next());
			Point [] idPoint=getPoint(coordinate,sourceCRS, targetCRS);
			elevation = coordinate.z;
			double longitude = (idPoint[0].getX());
			double latitude = Math.toRadians(idPoint[0].getY());

			double solarElevationAngle = solarGeometry.getSolarElevationAngle(date, latitude,longitude, doHourly);		
				
			double airTemperature = inAirTemperature.get(basinId)[0]+273.0;
			if (airTemperature == (nullValue+273.0)) {airTemperature = defaultAirTemperature;}		
			double leafTemperature = airTemperature;   	
			
			//double relativeHumidity = inRelativeHumidity.get(basinId)[0];
			//if (relativeHumidity == nullValue) {relativeHumidity = defaultRelativeHumidity;}
			
			double shortWaveRadiationDirect = inShortWaveRadiationDirect.get(basinId)[0];
			if (shortWaveRadiationDirect == nullValue) {shortWaveRadiationDirect = defaultShortWaveRadiationDirect;}   
			
			double shortWaveRadiationDiffuse = inShortWaveRadiationDiffuse.get(basinId)[0];
			if (shortWaveRadiationDiffuse == nullValue) {shortWaveRadiationDiffuse = 0.159*shortWaveRadiationDirect;}   
						
			double longWaveRadiation = inLongWaveRadiation.get(basinId)[0];
			if (longWaveRadiation == nullValue) {longWaveRadiation = longWaveEmittance * stefanBoltzmannConstant * pow (airTemperature, 4);}//defaultLongWaveRadiation;}
			
			double windVelocity = defaultWindVelocity;
			if (inWindVelocity != null)				
				windVelocity = inWindVelocity.get(basinId)[0];
			if (windVelocity == nullValue) {windVelocity = defaultWindVelocity;}
			
			double atmosphericPressure = 101325;
			if (inAtmosphericPressure != null)				
				atmosphericPressure = inAtmosphericPressure.get(basinId)[0];
			if (atmosphericPressure == nullValue) {atmosphericPressure = pressure.computePressure(defaultAtmosphericPressure, massAirMolecule, gravityConstant, elevation,boltzmannConstant, airTemperature);;}
			
			double relativeHumidity = defaultRelativeHumidity;
			if (inRelativeHumidity != null)				
				relativeHumidity = inRelativeHumidity.get(basinId)[0];
			if (relativeHumidity == nullValue) {relativeHumidity = defaultRelativeHumidity;}

			
			double leafAreaIndex = defaultLeafAreaIndex;
			if (inLeafAreaIndex != null)				
				leafAreaIndex = inLeafAreaIndex.get(basinId)[0];
			if (leafAreaIndex == nullValue) {leafAreaIndex = defaultLeafAreaIndex;}
			
			double saturationVaporPressure = pressure.computeSaturationVaporPressure(airTemperature, waterMolarMass, latentHeatEvaporation, molarGasConstant);
			double vaporPressure = relativeHumidity * saturationVaporPressure/100.0;
			double delta = pressure.computeDelta(airTemperature, waterMolarMass, latentHeatEvaporation, molarGasConstant);
			
			double convectiveTransferCoefficient = sensibleHeat.computeConvectiveTransferCoefficient(airTemperature, windVelocity, leafLength, criticalReynoldsNumber, prandtlNumber);
			double sensibleHeatTransferCoefficient = sensibleHeat.computeSensibleHeatTransferCoefficient(convectiveTransferCoefficient, leafSide);
			double latentHeatTransferCoefficient = latentHeat.computeLatentHeatTransferCoefficient(airTemperature, atmosphericPressure, leafSide, convectiveTransferCoefficient, airSpecificHeat, 
					airDensity, molarGasConstant, molarVolume, waterMolarMass, latentHeatEvaporation, poreDensity, poreArea, poreDepth, poreRadius);
			
			if (leafAreaIndex != 0) {	
					
			double shortWaveRadiationInSun=radiationMethods.computeAbsordebRadiationSunlit(leafAreaIndex, solarElevationAngle, shortWaveRadiationDirect,shortWaveRadiationDiffuse);
			
			double residual = 10.0;
			double latentHeatFlux = 0;
			double sensibleHeatFlux = 0;
			double netLongWaveRadiation = 0;
			double leafTemperatureSun = leafTemperature;
			double TranspirationSun = 0;
			double TranspirationShadow = 0;
			int iterator = 0;
			double leafInSunlit=(solarElevationAngle>0)?radiationMethods.computeSunlitLeafAreaIndex(leafAreaIndex, solarElevationAngle):0;
			if (solarElevationAngle>0) {	

			while(abs(residual) > 1 && iterator <= 2) 
				{
				sensibleHeatFlux = sensibleHeat.computeSensibleHeatFlux(sensibleHeatTransferCoefficient, leafTemperatureSun, airTemperature);
				latentHeatFlux = latentHeat.computeLatentHeatFlux(delta, leafTemperatureSun, airTemperature, latentHeatTransferCoefficient, sensibleHeatTransferCoefficient, vaporPressure, saturationVaporPressure);
				netLongWaveRadiation = radiationMethods.computeLongWaveRadiationBalance(leafSide, longWaveEmittance, airTemperature, leafTemperatureSun, stefanBoltzmannConstant);
				residual = (shortWaveRadiationInSun - netLongWaveRadiation) - sensibleHeatFlux - latentHeatFlux;
				leafTemperatureSun = computeLeafTemperature(leafSide, longWaveEmittance, sensibleHeatTransferCoefficient,latentHeatTransferCoefficient,airTemperature,shortWaveRadiationInSun,longWaveRadiation,vaporPressure, saturationVaporPressure,delta);
				iterator++;
				}
			TranspirationSun = latentHeat.computeLatentHeatFlux(delta, leafTemperatureSun, airTemperature, latentHeatTransferCoefficient, sensibleHeatTransferCoefficient, vaporPressure, saturationVaporPressure);
			}
			
			double shortWaveRadiationInShadow=(solarElevationAngle>0)?radiationMethods.computeAbsordebRadiationShadow(leafAreaIndex, solarElevationAngle, shortWaveRadiationDirect,shortWaveRadiationDiffuse):0;
			double residualSh = 10.0;
			double latentHeatFluxSh = 0;
			double sensibleHeatFluxSh = 0;
			double netLongWaveRadiationSh = 0;
			double leafTemperatureSh = leafTemperature;
			iterator = 0;
			
			while(abs(residualSh) > 1 && iterator <= 2) 
				{
				sensibleHeatFluxSh = sensibleHeat.computeSensibleHeatFlux(sensibleHeatTransferCoefficient, leafTemperatureSh, airTemperature);
				latentHeatFluxSh = latentHeat.computeLatentHeatFlux(delta, leafTemperatureSh, airTemperature, latentHeatTransferCoefficient, sensibleHeatTransferCoefficient, vaporPressure, saturationVaporPressure);
				netLongWaveRadiationSh = radiationMethods.computeLongWaveRadiationBalance(leafSide, longWaveEmittance, airTemperature, leafTemperatureSh, stefanBoltzmannConstant);
				residualSh = (shortWaveRadiationInShadow- netLongWaveRadiationSh) - sensibleHeatFluxSh - latentHeatFluxSh;
				leafTemperatureSh = computeLeafTemperature(leafSide, longWaveEmittance,sensibleHeatTransferCoefficient,latentHeatTransferCoefficient,airTemperature,shortWaveRadiationInShadow,longWaveRadiation,vaporPressure, saturationVaporPressure,delta);
				iterator++;
				}
			TranspirationShadow = latentHeat.computeLatentHeatFlux(delta, leafTemperatureSh, airTemperature, latentHeatTransferCoefficient, sensibleHeatTransferCoefficient, vaporPressure, saturationVaporPressure);
			
			double TotalTranspiration = ((leafInSunlit*TranspirationSun) + (TranspirationShadow*(leafAreaIndex-leafInSunlit)))*(time/latentHeatEvaporation);
			
			storeResult((Integer)basinId,TotalTranspiration,leafTemperatureSun);
			}
			else {
				storeResult((Integer)basinId,0,0);
				}
			}
		step++;	

		}
	
	private double computeLeafTemperature(
			double side,
			double emissivity,
			double sensibleHeatTransferCoefficient,
			double latentHeatTransferCoefficient, 
			double airTemperature, 
			double shortWaveRadiation,
			double longWaveRadiation,
			double vaporPressure,
			double saturationVaporPressure,
			double delta) {
		double leafTemperature1 = (shortWaveRadiation + sensibleHeatTransferCoefficient*airTemperature +
				latentHeatTransferCoefficient*(delta*airTemperature + vaporPressure - saturationVaporPressure) + 
				side * longWaveRadiation * 4 );
		double leafTemperature2 =(1/(sensibleHeatTransferCoefficient + latentHeatTransferCoefficient * delta +	
				side * longWaveRadiation/airTemperature * 4));
		double leafTemperature = leafTemperature1*leafTemperature2;
		return leafTemperature;	
	}
	
	private LinkedHashMap<Integer, Coordinate> getCoordinate(int nStaz,
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
	}
	private void storeResult(int ID,double TotalTranspiration, double leafTemperatureSun) 
			throws SchemaException {

		outTranspiration.put(ID, new double[]{TotalTranspiration});
		outLeafTemperature.put(ID, new double[]{leafTemperatureSun});
	}
	
	private Point[] getPoint(Coordinate coordinate, CoordinateReferenceSystem sourceCRS, CoordinateReferenceSystem targetCRS)
			throws Exception{
		Point[] point = new Point[] { GeometryUtilities.gf().createPoint(coordinate) };
		CrsUtilities.reproject(sourceCRS, targetCRS, point);
		return point;
	}
		
}
