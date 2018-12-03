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


import etpPointCase.*;
import etpClasses.*;
import etpSurfaces.*;

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
	
	/////////////////////////////////////////////
	// ENVIRONMENTAL VARIABLES - INPUT
	/////////////////////////////////////////////

	@Description("Air temperature.")
	@In
	@Unit("K")
	public HashMap<Integer, double[]> inAirTemperature;
	
	@Description("The wind speed.")
	@In
	@Unit("m s-1")
	public HashMap<Integer, double[]> inWindVelocity;
	
	@Description("The air relative humidity.")
	@In
	@Unit("%")
	public HashMap<Integer, double[]> inRelativeHumidity;
	
	@Description("The short wave radiation at the surface.")
	@In
	@Unit("W m-2")
	public HashMap<Integer, double[]> inShortWaveRadiationDirect;
	
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

	@Description("The soilflux.")
	@In
	@Unit("W m-2")
	public HashMap<Integer, double[]> inSoilFlux;
	
	@Description("Leaf area index.")
	@In
	@Unit("m2 m-2")
	public HashMap<Integer, double[]> inLeafAreaIndex;
	
	/////////////////////////////////////////////
	// ENVIRONMENTAL VARIABLES - DEFAULT
	/////////////////////////////////////////////

	@Description("The air temperature default value in case of missing data.")
	@In
	@Unit("K")
	public double defaultAirTemperature = 15.0+273.0;
	  
	@Description("The wind default value in case of missing data.")
	@In
	@Unit("m s-1")
	public double defaultWindVelocity = 0.5;
	
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
	
	@Description("The soilflux default value in case of missing data.")
	@In
	@Unit("W m-2")
	public double defaultSoilFlux = 0.0;
	
	@Description("The leaf area index default value in case of missing data.")
	@In
	@Unit("m2 m-2")
	public double defaultLeafAreaIndex = 1.0;
		
	
	@Description("The short wave radiation highlighting the canopy in sunlight.")
	@In
	@Unit("W m-2")
	public double shortWaveRadiationInSun;
	
	@Description("The short wave radiation highlighting the canopy in shadow.")
	@In
	@Unit("W m-2")
	public double shortWaveRadiationInShadow;
	
	/////////////////////////////////////////////
	// GEOGRAPHIC VARIABLES - DEFAULT
	/////////////////////////////////////////////
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

	@Description("Type of transpiring area")
	@In
	public String typeOfTerrainCover;
	//double residual = 10.0;
	/*double latentHeatFlux;
	double sensibleHeatFlux;
	double netLongWaveRadiation;
	//double leafTemperatureSun = leafTemperature;
	double TranspirationSun;
	double TranspirationShadow;
	int iterator;*/
	

	@Description("It is needed to iterate on the date")
	int step;

	@Description("The first day of the simulation.")
	@In
	public String tStartDate;
	
	@Description("The first day of the simulation.")
	@In
	public int tStep;
	
	public DateTime date;
	double nullValue = -9999.0;
	public int time;
	private HortonMessageHandler msg = HortonMessageHandler.getInstance();
	DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").withZone(DateTimeZone.UTC);
	
	
	/////////////////////////////////////////////
	// ECOLOGICAL VARIABLES - INPUT
	/////////////////////////////////////////////
	
	/////////////////////////////////////////////
	// ECOLOGICAL VARIABLES - DEFAULT
	/////////////////////////////////////////////
	
	/////////////////////////////////////////////
	// OUTPUT
	/////////////////////////////////////////////
	
	
	@Description("The latent heat.")
	@Unit("mm h-1")
	@Out
	public double totalTranspiration; 
	
	@Description("The latent heat.")
	@Unit("mm h-1")
	@Out
	public HashMap<Integer, double[]> outLatentHeatSun;
	
	@Description("The latent heat.")
	@Unit("mm h-1")
	@Out
	public HashMap<Integer, double[]> outLatentHeatShadow;
	
	@Description("The transpirated water.")
	@Unit("mm h-1")
	@Out
	public HashMap<Integer, double[]> outTranspiration;
	
	@Description("The sensible heat.")
	@Unit("W m-2")
	@Out
	public HashMap<Integer, double[]> outSensibleHeatSun;
	
	@Description("The sensible heat.")
	@Unit("W m-2")
	@Out
	public HashMap<Integer, double[]> outSensibleHeatShadow;
	
	@Description("The leaf Temperature.")
	@Unit("K")
	@Out
	public HashMap<Integer, double[]> outLeafTemperatureSun;
	
	@Description("The leaf Temperature.")
	@Unit("K")
	@Out
	public HashMap<Integer, double[]> outLeafTemperatureShadow;
	
	@Description("The solar radiation absorbed by the sunlit canopy.")
	@Unit("W m-2")
	@Out
	public HashMap<Integer, double[]> outRadiationSun;
	
	@Description("The solar radiation absorbed by the shaded canopy.")
	@Unit("W m-2")
	@Out
	public HashMap<Integer, double[]> outRadiationShadow;
	
	@Description("Fraction of highlighted canopy.")
	@Unit("-")
	@Out
	public HashMap<Integer, double[]> outCanopy;
	
	//public double longitude;
	/////////////////////////////////////////////
	// OTHERS - DO
	/////////////////////////////////////////////
	@In
	public HashMap<Integer, double[]> inStressSun;

	@In
	public HashMap<Integer, double[]> inStressSh;
	
	@Description("Switch that defines if it is hourly.")
	@In
	public boolean doHourly = true;
	
	@Description("Choose if you want to print only the latent heat or all the other outputs.")
	@In
	public boolean doFullPrint = true;
	
	//@Description("Choose if you want to use the multi-layer canopy model or a simple transpiring surface.")
	//@In
	//public boolean doMultiLayer = true;

	@In
	String printo;
	// METHODS FROM CLASSES		
	SensibleHeatMethods sensibleHeat 	= new SensibleHeatMethods();
	LatentHeatMethods latentHeat 		= new LatentHeatMethods();
	PressureMethods pressure 			= new PressureMethods(); 
	RadiationMethod radiationMethods 	= new RadiationMethod();
	SolarGeometry solarGeometry 		= new SolarGeometry();

	TranspiringSurface transpiringSurface;
	
	@Execute
	public void process() throws Exception {
		if (doHourly == true) {
			time =tStep*60;

			} else {
			time = 86400;
			}
		DateTime startDateTime = formatter.parseDateTime(tStartDate);
		DateTime date=(doHourly==false)?startDateTime.plusDays(step).plusHours(12):startDateTime.plusMinutes(tStep*step);

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
		int leafStomaSide = propertyOfLeaf.stomaSide;
		double longWaveEmittance = propertyOfLeaf.longWaveEmittance;
		
		outLatentHeatShadow = new HashMap<Integer, double[]>();
		outLatentHeatSun = new HashMap<Integer, double[]>();
		outTranspiration = new HashMap<Integer, double[]>();
		if (doFullPrint == true) {
			outLeafTemperatureSun 	= new HashMap<Integer, double[]>();
			outRadiationSun 	= new HashMap<Integer, double[]>();
			outRadiationShadow 	= new HashMap<Integer, double[]>();
			outSensibleHeatSun 	= new HashMap<Integer, double[]>();
			outSensibleHeatShadow 	= new HashMap<Integer, double[]>();
			outRadiationShadow 	= new HashMap<Integer, double[]>();
			outLeafTemperatureShadow= new HashMap<Integer, double[]>();
			outCanopy 			= new HashMap<Integer, double[]>();
			}
		
		Set<Entry<Integer, double[]>> entrySet = inAirTemperature.entrySet();
		for( Entry<Integer, double[]> entry : entrySet ) {
			Integer basinId = entry.getKey();
			Coordinate coordinate = (Coordinate) stationCoordinates.get(idIterator.next());
			Point [] idPoint=getPoint(coordinate,sourceCRS, targetCRS);
			elevation = coordinate.z;
			longitude = (idPoint[0].getX());
			latitude = Math.toRadians(idPoint[0].getY());
		
			//double solarElevationAngle = solarGeometry.getSolarElevationAngle(date, latitude,longitude, doHourly);
			
			/////////////////////////////////////////////
			// INPUT READER
			/////////////////////////////////////////////
				
			double airTemperature = inAirTemperature.get(basinId)[0]+273.0;
			if (airTemperature == (nullValue+273.0)) {airTemperature = defaultAirTemperature;}		
			double leafTemperature = airTemperature;   	
			
			double leafAreaIndex = defaultLeafAreaIndex;
			if (inLeafAreaIndex != null)				
				leafAreaIndex = inLeafAreaIndex.get(basinId)[0];
			if (leafAreaIndex == nullValue) {leafAreaIndex = defaultLeafAreaIndex;}
			
			// Only if LAI is different from zero the method is computed
			// if LAI is not 0 compute transpiration
			if (leafAreaIndex != 0) {	
							
				double stressSun = inStressSun.get(basinId)[0];
				double stressSh = inStressSh.get(basinId)[0];
				//if (stressSun == nullValue) {shortWaveRadiationDirect = defaultShortWaveRadiationDirect;}
				
				double shortWaveRadiationDirect = inShortWaveRadiationDirect.get(basinId)[0];
				if (shortWaveRadiationDirect == nullValue) {shortWaveRadiationDirect = defaultShortWaveRadiationDirect;}
				
				double shortWaveRadiationDiffuse = inShortWaveRadiationDiffuse.get(basinId)[0];
				if (shortWaveRadiationDiffuse == nullValue) {shortWaveRadiationDiffuse = 0.159*shortWaveRadiationDirect;} 						
				
				double longWaveRadiation = inLongWaveRadiation.get(basinId)[0];
				if (longWaveRadiation == nullValue) {longWaveRadiation = longWaveEmittance * stefanBoltzmannConstant * pow (airTemperature, 4);}//defaultLongWaveRadiation;}	
				
				double windVelocity = defaultWindVelocity;
				if (inWindVelocity != null){windVelocity = inWindVelocity.get(basinId)[0];}
				if (windVelocity == nullValue) {windVelocity = defaultWindVelocity;}
				if (windVelocity == 0.0) {windVelocity = defaultWindVelocity;}			
				
				double atmosphericPressure = 101325;
				if (inAtmosphericPressure != null){atmosphericPressure = inAtmosphericPressure.get(basinId)[0];}
				if (atmosphericPressure == nullValue) {atmosphericPressure = pressure.computePressure(defaultAtmosphericPressure, massAirMolecule, gravityConstant, elevation,boltzmannConstant, airTemperature);;}			
				
				double relativeHumidity = defaultRelativeHumidity;
				if (inRelativeHumidity != null){relativeHumidity = inRelativeHumidity.get(basinId)[0];}
				if (relativeHumidity == nullValue) {relativeHumidity = defaultRelativeHumidity;}				
				
				// Compute the saturation pressure
				double saturationVaporPressure = pressure.computeSaturationVaporPressure(airTemperature, waterMolarMass, latentHeatEvaporation, molarGasConstant);			
				// Compute the actual vapour pressure
				double vaporPressure = relativeHumidity * saturationVaporPressure/100.0;			
				// Compute the delta
				double delta = pressure.computeDelta(airTemperature, waterMolarMass, latentHeatEvaporation, molarGasConstant);			
				// Compute the convective transfer coefficient - hc
				double convectiveTransferCoefficient = sensibleHeat.computeConvectiveTransferCoefficient(airTemperature, windVelocity, leafLength, criticalReynoldsNumber, prandtlNumber);
				// Compute the sensible transfer coefficient - cH
				double sensibleHeatTransferCoefficient = sensibleHeat.computeSensibleHeatTransferCoefficient(convectiveTransferCoefficient, leafSide);
				// Compute the latent transfer coefficient - cE
				double latentHeatTransferCoefficient = latentHeat.computeLatentHeatTransferCoefficient(airTemperature, atmosphericPressure, leafStomaSide, convectiveTransferCoefficient, airSpecificHeat,
						airDensity, molarGasConstant, molarVolume, waterMolarMass, latentHeatEvaporation, poreDensity, poreArea, poreDepth, poreRadius);			

				
				transpiringSurface=CanopyModel.createTheCanopy(typeOfTerrainCover, delta, leafTemperature, airTemperature, stressSun, stressSh,
						latentHeatTransferCoefficient,sensibleHeatTransferCoefficient, vaporPressure, saturationVaporPressure,
						shortWaveRadiationDirect, longWaveRadiation, leafSide,date, latitude,longitude, doHourly, leafAreaIndex);
				
				//if ("MultiLayersCanopy".equals(typeOfTerrainCover)) {		
					//double solarElevationAngle = solarGeometry.getSolarElevationAngle(date, latitude,longitude, doHourly);
				//	solarElevationAngle = ((solarElevationAngle>0)?solarElevationAngle:0);
					
					// compute the fraction of canopy in sunlight if a multi-layer model is adopted, otherwise the transpiring area is equal to 1
				//	double leafInSunlit	=((solarElevationAngle>0)?radiationMethods.computeSunlitLeafAreaIndex(leafAreaIndex, solarElevationAngle):0);

					// compute the shortwave radiation absorbed by the shaded canopy
					//double shortWaveRadiationInSun=(solarElevationAngle>0)?radiationMethods.computeAbsordebRadiationSunlit(leafAreaIndex, solarElevationAngle, shortWaveRadiationDirect, shortWaveRadiationDiffuse):0;
					//double leafInShadow	=leafAreaIndex - leafInSunlit;

					//double shortWaveRadiationInShadow	=(solarElevationAngle>0)?radiationMethods.computeAbsordebRadiationShadow(leafAreaIndex, solarElevationAngle, shortWaveRadiationDirect, shortWaveRadiationDiffuse):0;
				//	System.out.println("****	"+ leafInSunlit );//+"----	" +shortWaveRadiationInShadow);
			//		}
							//computeAbsordebRadiationSun(leafAreaIndex, solarElevationAngle, shortWaveRadiationDirect,shortWaveRadiationDiffuse):0;

				//	double shortWaveRadiationInSun=(solarElevationAngle>0)?radiationMethods.computeAbsordebRadiationShadow(leafAreaIndex, solarElevationAngle, shortWaveRadiationDirect,shortWaveRadiationDiffuse):0;
					//System.out.println("8888	"+ solarElevationAngle);
					//System.out.println("++++	"+ shortWaveRadiationDirect);
					//System.out.println("////	"+ (shortWaveRadiationDirect+shortWaveRadiationDiffuse));
					//System.out.println("++++	"+ shortWaveRadiationInShadow);
						
					// compute the shortwave radiation absorbed by the shaded canopy
					//double shortWaveRadiationInSun=(solarElevationAngle>0)?radiationMethods.computeAbsordebRadiationShadow(leafAreaIndex, solarElevationAngle, shortWaveRadiationDirect,shortWaveRadiationDiffuse):0;
					
				
			 	transpiringSurface.setDelta(delta);
				transpiringSurface.setAirTemperature(airTemperature);
				transpiringSurface.setSurfaceTemperature(leafTemperature);
				
				transpiringSurface.setStressSun(stressSun);
				transpiringSurface.setStressSh(stressSh);

				transpiringSurface.setLatentHeatTransferCoefficient(latentHeatTransferCoefficient);
				transpiringSurface.setSensibleHeatTransferCoefficient(sensibleHeatTransferCoefficient);
				
				transpiringSurface.setVaporPressure( vaporPressure);
				transpiringSurface.setSaturationVaporPressure(saturationVaporPressure);
				
				transpiringSurface.setDirectShortWave(shortWaveRadiationDirect);
				transpiringSurface.setDiffuseShortWave(shortWaveRadiationDiffuse);
				transpiringSurface.setLongWaveRadiation( longWaveRadiation);
				
				transpiringSurface.setSide(leafSide);
				transpiringSurface.setLeafAreaIndex( leafAreaIndex);
				
				transpiringSurface.setDate(date);
				transpiringSurface.setDoHourly(doHourly);
				transpiringSurface.setTimeStep(time);
				transpiringSurface.setLatitude(latitude);
				transpiringSurface.setLongitude(longitude);

				//transpiringSurface.setleafTemperature = 100.0;
				double leafInSunlight	= transpiringSurface.irradiatedSurface();
				double leafInShadow		= transpiringSurface.shadedSurface();

				double radiationCanopyInLight = transpiringSurface.incidentSolarRadiation();
				double radiationCanopyInShadow = transpiringSurface.shadedSolarRadiation();

			//	System.out.println(sensibleHeatFlux3);
				double leafTemperatureSun 		= transpiringSurface.computeSurfaceTemperatureIrradiatedSurface();		
				double leafTemperatureShadow	= transpiringSurface.computeSurfaceTemperatureShadedSurface();

				double latentHeatFluxLight 	= transpiringSurface.computeLatentHeatIrradiatedSurface();
				double latentHeatFluxShadow	= transpiringSurface.computeLatentHeatFluxShadedSurface();
				
				double sensibleHeatFluxLight	= transpiringSurface.computeSensibleHeatFluxIrradiatedSurface();
				double sensibleHeatFluxShadow	= transpiringSurface.computeSensibleHeatFluxShadedSurface();

				//System.out.println(sensibleHeatFlux3);

			//	double Temp = transpiringSurface.computeSurfaceTemperatureIrradiatedSurface();
			//	double latentHeatFlux2 	= transpiringSurface.computeLatentHeatFluxShadedSurface();
			//	double sensibleHeatFlux3 = transpiringSurface.computeSensibleHeatFluxShadedSurface();
				//double gibbo = transpiringSurface.franco(leafAreaIndex, solarElevationAngle, shortWaveRadiationDirect, shortWaveRadiationDiffuse);
				
			/*	System.out.println("\n");
				System.out.println("Date		"+date);
				//System.out.println("Temp		"+leafTemperatureSun+"	"+leafTemperatureShadow+"	"+airTemperature);
				//System.out.println("Lai		"+leafInSunlight+"	"+leafInShadow);
				System.out.println("El		"+latentHeatFluxLight+"	"+latentHeatFluxShadow);
				System.out.println("Hl		"+sensibleHeatFluxLight+"	"+sensibleHeatFluxShadow);
				System.out.println("\n");*/

				/*System.out.println("LAI1	"+leafInSunlit);
				System.out.println("Air		"+airTemperature);			
				System.out.println("temp second	"+leafTemperature);

				System.out.println("LAI3	"+latentHeatFlux);
				System.out.println("LAI4	"+sensibleHeatFlux);*/
				//double leafTemperatureSun = leafTemperature;
				// Find the leaf temperature in sunlight and compute the new fluxes
				//double sensibleHeatFlux	= leafInSunlit*sensibleHeat.computeSensibleHeatFlux(sensibleHeatTransferCoefficient, leafTemperatureSun, airTemperature);
				//double latentHeatFlux		= leafInSunlit*latentHeat.computeLatentHeatFlux(delta, leafTemperatureSun, airTemperature, latentHeatTransferCoefficient, sensibleHeatTransferCoefficient, vaporPressure, saturationVaporPressure);
				//double netLongWaveRadiation= leafInSunlit*radiationMethods.computeLongWaveRadiationBalance(leafSide, longWaveEmittance, airTemperature, leafTemperatureSun, stefanBoltzmannConstant);
				
				
				/*if (solarElevationAngle>0) {
					// compute the shortwave radiation absorbed by the sunlit canopy
					double shortWaveRadiationInSun =(doMultiLayer==true)?((solarElevationAngle>0)?radiationMethods.computeAbsordebRadiationSunlit(leafAreaIndex, solarElevationAngle, shortWaveRadiationDirect,shortWaveRadiationDiffuse):0):shortWaveRadiationDirect;
					
					// compute the fraction of canopy in sunlight if a multi-layer model is adopted, otherwise the transpiring area is equal to 1
					double leafInSunlit	=(doMultiLayer==true)?((solarElevationAngle>0)?radiationMethods.computeSunlitLeafAreaIndex(leafAreaIndex, solarElevationAngle):0):1;
					
					double leafTemperatureSun = leafTemperature;
					// compute the equilibrium leaf temperature
					leafTemperatureSun	= computeLeafTemperature(leafInSunlit, leafSide, longWaveEmittance, sensibleHeatTransferCoefficient,latentHeatTransferCoefficient,airTemperature,shortWaveRadiationInSun,longWaveRadiation,vaporPressure, saturationVaporPressure,delta);
					// Find the leaf temperature in sunlight and compute the new fluxes
					double sensibleHeatFlux	= leafInSunlit*sensibleHeat.computeSensibleHeatFlux(sensibleHeatTransferCoefficient, leafTemperatureSun, airTemperature);
					double latentHeatFlux		= leafInSunlit*latentHeat.computeLatentHeatFlux(delta, leafTemperatureSun, airTemperature, latentHeatTransferCoefficient, sensibleHeatTransferCoefficient, vaporPressure, saturationVaporPressure);
					//double netLongWaveRadiation= leafInSunlit*radiationMethods.computeLongWaveRadiationBalance(leafSide, longWaveEmittance, airTemperature, leafTemperatureSun, stefanBoltzmannConstant);
					
					// compute the shortwave radiation absorbed by the shaded canopy
					double shortWaveRadiationInShadow=(solarElevationAngle>0)?radiationMethods.computeAbsordebRadiationShadow(leafAreaIndex, solarElevationAngle, shortWaveRadiationDirect,shortWaveRadiationDiffuse):0;
					double latentHeatFluxShade = 0;
					double sensibleHeatFluxShade = 0;
					//double netLongWaveRadiationShade = 0;
					double leafTemperatureShade = leafTemperature;

					if (doMultiLayer == true){
						double leafInShade = 1 - leafInSunlit;
						leafTemperatureShade = computeLeafTemperature(leafInSunlit, leafSide, longWaveEmittance,sensibleHeatTransferCoefficient,latentHeatTransferCoefficient,airTemperature,shortWaveRadiationInShadow,longWaveRadiation,vaporPressure, saturationVaporPressure,delta);
						// Find the leaf temperature in sunlight and compute the new fluxes
						sensibleHeatFluxShade = leafInShade*sensibleHeat.computeSensibleHeatFlux(sensibleHeatTransferCoefficient, leafTemperatureShade, airTemperature);
						latentHeatFluxShade = leafInShade*latentHeat.computeLatentHeatFlux(delta, leafTemperatureShade, airTemperature, latentHeatTransferCoefficient, sensibleHeatTransferCoefficient, vaporPressure, saturationVaporPressure);
						//netLongWaveRadiationShade = leafInShade*radiationMethods.computeLongWaveRadiationBalance(leafSide, longWaveEmittance, airTemperature, leafTemperatureShade, stefanBoltzmannConstant);
						}								
					}
				else {
					// compute the shortwave radiation absorbed by the sunlit canopy
					//double shortWaveRadiationInSun =(doMultiLayer==true)?((solarElevationAngle>0)?radiationMethods.computeAbsordebRadiationSunlit(leafAreaIndex, solarElevationAngle, shortWaveRadiationDirect,shortWaveRadiationDiffuse):0):shortWaveRadiationDirect;
					
					// compute the fraction of canopy in sunlight if a multi-layer model is adopted, otherwise the transpiring area is equal to 1
					//double leafInSunlit	=(doMultiLayer==true)?((solarElevationAngle>0)?radiationMethods.computeSunlitLeafAreaIndex(leafAreaIndex, solarElevationAngle):0):1;
					
					//double leafTemperatureSun = leafTemperature;
					// compute the equilibrium leaf temperature
					//leafTemperatureSun	= computeLeafTemperature(leafInSunlit, leafSide, longWaveEmittance, sensibleHeatTransferCoefficient,latentHeatTransferCoefficient,airTemperature,shortWaveRadiationInSun,longWaveRadiation,vaporPressure, saturationVaporPressure,delta);
					// Find the leaf temperature in sunlight and compute the new fluxes
					//double sensibleHeatFlux	= leafInSunlit*sensibleHeat.computeSensibleHeatFlux(sensibleHeatTransferCoefficient, leafTemperatureSun, airTemperature);
					//double latentHeatFlux		= leafInSunlit*latentHeat.computeLatentHeatFlux(delta, leafTemperatureSun, airTemperature, latentHeatTransferCoefficient, sensibleHeatTransferCoefficient, vaporPressure, saturationVaporPressure);
					//double netLongWaveRadiation= leafInSunlit*radiationMethods.computeLongWaveRadiationBalance(leafSide, longWaveEmittance, airTemperature, leafTemperatureSun, stefanBoltzmannConstant);
					
					// compute the shortwave radiation absorbed by the shaded canopy
					double shortWaveRadiationInShadow=(solarElevationAngle>0)?radiationMethods.computeAbsordebRadiationShadow(leafAreaIndex, solarElevationAngle, shortWaveRadiationDirect,shortWaveRadiationDiffuse):0;
					double latentHeatFluxShade = 0;
					double sensibleHeatFluxShade = 0;
					//double netLongWaveRadiationShade = 0;
					double leafTemperatureShade = leafTemperature;

					//if (doMultiLayer == true){
						double leafInShade = 1.0; //- leafInSunlit;
						leafTemperatureShade = computeLeafTemperature(leafInShade, leafSide, longWaveEmittance,sensibleHeatTransferCoefficient,latentHeatTransferCoefficient,airTemperature,shortWaveRadiationInShadow,longWaveRadiation,vaporPressure, saturationVaporPressure,delta);
						// Find the leaf temperature in sunlight and compute the new fluxes
						sensibleHeatFluxShade = leafInShade*sensibleHeat.computeSensibleHeatFlux(sensibleHeatTransferCoefficient, leafTemperatureShade, airTemperature);
						latentHeatFluxShade = leafInShade*latentHeat.computeLatentHeatFlux(delta, leafTemperatureShade, airTemperature, latentHeatTransferCoefficient, sensibleHeatTransferCoefficient, vaporPressure, saturationVaporPressure);
						//netLongWaveRadiationShade = leafInShade*radiationMethods.computeLongWaveRadiationBalance(leafSide, longWaveEmittance, airTemperature, leafTemperatureShade, stefanBoltzmannConstant);
					//	}								
					}
				}
				//TranspirationShadow = latentHeat.computeLatentHeatFlux(delta, leafTemperatureShade, airTemperature, latentHeatTransferCoefficient, sensibleHeatTransferCoefficient, vaporPressure, saturationVaporPressure);
				*/
			double latentHeatSun = latentHeatFluxLight;//latentHeatFluxLight;//+latentHeatFluxShadow;	

			double latentHeatShadow = latentHeatFluxShadow;//latentHeatFluxLight;//+latentHeatFluxShadow;	
			totalTranspiration = (latentHeatFluxLight+latentHeatFluxShadow)*(time/latentHeatEvaporation);
			//double totalSensibleHeat = sensibleHeatFluxLight+sensibleHeatFluxShadow;
			//double outputRadiationCanopyInSun = radiationCanopyInLight;
			//double outputRadiationCanopyInShadow = radiationCanopyInShadow;
			if (doFullPrint == true) {				
				storeResultFull((Integer)basinId, latentHeatSun, latentHeatShadow, totalTranspiration, sensibleHeatFluxLight,sensibleHeatFluxShadow,
						leafTemperatureSun, leafTemperatureShadow,radiationCanopyInLight, radiationCanopyInShadow,leafInSunlight);
				}
			else {
				storeResult((Integer)basinId,latentHeatSun, latentHeatShadow, totalTranspiration);
				}
			}
			else {
				if (doFullPrint == true) {storeResultFull((Integer)basinId,0,0,0,0,0,0,0,0,0,0);}
				else {storeResult((Integer)basinId,0,0,0);}
				}
			}
		step++;	
			}
		
		
	
	/*private double computeLeafTemperature(
			double leafInSunlit,
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
		double leafTemperature1 = (shortWaveRadiation/leafInSunlit + sensibleHeatTransferCoefficient*airTemperature +
				latentHeatTransferCoefficient*(delta*airTemperature + vaporPressure - saturationVaporPressure) + 
				side * longWaveRadiation * 4 );
		double leafTemperature2 =(1/(sensibleHeatTransferCoefficient + latentHeatTransferCoefficient * delta +	
				side * longWaveRadiation/airTemperature * 4));
		double leafTemperature = leafTemperature1*leafTemperature2;
		return leafTemperature;	
	}*/
	
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
	//public double irradiatedSurface() {
		//double areaInSunlight = 1;
		//return areaInSunlight;
	//}
	/*public double irradiatedSurface(String type, double delta, double leafTemperature, double airTemperature, double latentHeatTransferCoefficient,
			double sensibleHeatTransferCoefficient, double vaporPressure, double saturationVaporPressure,
			double shortWaveRadiation, double longWaveRadiation, double side) {

		transpiringSurface=CanopyModel.createTheCanopy(type, delta, leafTemperature, airTemperature, latentHeatTransferCoefficient,
				sensibleHeatTransferCoefficient, vaporPressure, saturationVaporPressure,
				shortWaveRadiation, longWaveRadiation, side);
		double result=transpiringSurface.irradiatedSurface();

		return result;
	}
	
	public double computeLatentHeatExchange(String type, double delta, double leafTemperature, double airTemperature, double latentHeatTransferCoefficient,
			double sensibleHeatTransferCoefficient, double vaporPressure, double saturationVaporPressure,
			double shortWaveRadiation, double longWaveRadiation, double side) {

		transpiringSurface=CanopyModel.createTheCanopy(type, delta, leafTemperature, airTemperature, latentHeatTransferCoefficient,
				sensibleHeatTransferCoefficient, vaporPressure, saturationVaporPressure,
				shortWaveRadiation, longWaveRadiation, side);
		double result=transpiringSurface.computeLatentHeatFlux();

		return result;
	}
	
	public double computeSensibleHeatExchange(String type, double delta, double leafTemperature, double airTemperature, double latentHeatTransferCoefficient,

			double sensibleHeatTransferCoefficient, double vaporPressure, double saturationVaporPressure,
			double shortWaveRadiation, double longWaveRadiation, double side) {

		transpiringSurface=CanopyModel.createTheCanopy(type, delta, leafTemperature, airTemperature, latentHeatTransferCoefficient,
				sensibleHeatTransferCoefficient, vaporPressure, saturationVaporPressure,
				shortWaveRadiation, longWaveRadiation, side);
		double result=transpiringSurface.computeSensibleHeatFlux();

		return result;
	}
	
	public double computeTemperature(String type, double delta, double leafTemperature, double airTemperature, double latentHeatTransferCoefficient,
			double sensibleHeatTransferCoefficient, double vaporPressure, double saturationVaporPressure,
			double shortWaveRadiation, double longWaveRadiation, double side) {

		transpiringSurface=CanopyModel.createTheCanopy(type, delta, leafTemperature, airTemperature, latentHeatTransferCoefficient,
				sensibleHeatTransferCoefficient, vaporPressure, saturationVaporPressure,
				shortWaveRadiation, longWaveRadiation, side);
		double result=transpiringSurface.computeSurfaceTemperature();

		return result;
	}*/
	
	private void storeResultFull(int ID,double latentHeatSun, double latentHeatShadow,double totalTranspiration, 
			double sensibleHeatFluxLight, double sensibleHeatFluxShadow,
			double leafTemperatureSun, double leafTemperatureShadow, 
			double radiationCanopyInLight, double radiationCanopyInShadow, 
			double leafInSunlight) 
			throws SchemaException {
		
	//	outLatentHeatShadow = new HashMap<Integer, double[]>();
	//	outLatentHeatSun = new HashMap<Integer, double[]>();
		outLatentHeatSun.put(		ID, new double[]{latentHeatSun});
		outLatentHeatShadow.put(		ID, new double[]{latentHeatShadow});
		outTranspiration.put(	ID, new double[]{totalTranspiration});
		
		outSensibleHeatSun.put(	ID, new double[]{sensibleHeatFluxLight});
		outSensibleHeatShadow.put(	ID, new double[]{sensibleHeatFluxShadow});

		outLeafTemperatureSun.put(	ID, new double[]{leafTemperatureSun});
		outLeafTemperatureShadow.put(	ID, new double[]{leafTemperatureShadow});

		outRadiationSun.put(	ID, new double[]{radiationCanopyInLight});
		outRadiationShadow.put(	ID, new double[]{radiationCanopyInShadow});
		outCanopy.put(			ID, new double[]{leafInSunlight});
		}
	private void storeResult(int ID,double latentHeatSun, double latentHeatShadow,double totalTranspiration) 
			throws SchemaException {
		outLatentHeatSun.put(		ID, new double[]{latentHeatSun});
		outLatentHeatShadow.put(		ID, new double[]{latentHeatShadow});
		outTranspiration.put(	ID, new double[]{totalTranspiration});
		}
	
	private Point[] getPoint(Coordinate coordinate, CoordinateReferenceSystem sourceCRS, CoordinateReferenceSystem targetCRS)
			throws Exception{
		Point[] point = new Point[] { GeometryUtilities.gf().createPoint(coordinate) };
		CrsUtilities.reproject(sourceCRS, targetCRS, point);
		return point;
	}
		
}
