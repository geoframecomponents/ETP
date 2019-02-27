package etpRasterCase;
import etpClasses.*;

import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.util.LinkedHashMap;
import static java.lang.Math.abs;
import static java.lang.Math.pow;

import javax.media.jai.iterator.RandomIterFactory;
import javax.media.jai.iterator.WritableRandomIter;
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
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.jgrasstools.gears.libs.modules.JGTModel;
import org.jgrasstools.gears.utils.CrsUtilities;
import org.jgrasstools.gears.utils.RegionMap;
import org.jgrasstools.gears.utils.coverage.CoverageUtilities;
import org.jgrasstools.gears.utils.geometry.GeometryUtilities;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;

/*
 * GNU GPL v3 License
 *
 * Copyright 2017 Michele Bottazzi
 *
 * This program is free software: you can redistribute it and/or modify
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

@Description("ET raster.")
@Author(name = "Michele Bottazzi")
@Keywords("Evapotranspiration")
@Label("")
@Name("")
@Status()
@License("General Public License Version 3 (GPLv3)")
@SuppressWarnings("nls")
public class OmsTranspirationRaster extends JGTModel implements Parameters {
	
	// ENVIRONMENTAL VARIABLES
	@Description("The map of the interpolated temperature.")
	@In
	public GridCoverage2D inAirTemperatureGrid;
	@Description("The double value of the air temperature")
	double airTemperature;
	private static final double defaultAirTemperature = 15.0+273.0;
	
	@Description("sw.")
	@In
	public GridCoverage2D inShortWaveRadiationDirectGrid;
	@Description("SW")
	double shortWaveRadiationDirect;
	private static final double defaultShortWaveRadiationDirect = 0.0;

	@Description("sw.")
	@In
	public GridCoverage2D inShortWaveRadiationDiffuseGrid;
	@Description("SW")
	double shortWaveRadiationDiffuse;
	
	
	@Description("lw.")
	@In
	public GridCoverage2D inLongWaveRadiationGrid;
	@Description("lwe")
	double longWaveRadiation;
	
	@Description("The map of the interpolated humidity.")
	@In
	public GridCoverage2D inRelativeHumidityGrid;
	@Description("The double value of the humidity")
	double relativeHumidity;
	private static final double defaultRelativeHumidity = 70.0;
	
	@Description("The map of the wind.")
	@In
	public GridCoverage2D inWindVelocityGrid;
	@Description("The double value of the humidity")
	double windVelocity;
	private static final double defaultWindVelocity = 2.0;
	
	@Description("The map of the pressure.")
	@In
	public GridCoverage2D inAtmosphericPressureGrid;
	@Description("The double value of the humidity")
	double atmosphericPressure;
	private static final double defaultAtmosphericPressure = 101325.0;
	
	// GEOGRAPHIC VARIABLES

	@Description("The map of the interpolated temperature.")
	@In
	public GridCoverage2D inDemElevationGrid;
	@Description("The double value of the air temperature")
	double demElevation;
	private static final double defaultDemElevation = 0;

	// ECOLOGICAL VARIABLES
	@Description("Leaves temperature")
	@In
	@Unit("K")
	public double leafTemperature;
	
	
	@Description("Leaf area index.")
	@In
	@Unit("m2 m-2")
	public GridCoverage2D inLeafAreaIndexGrid;
	double leafAreaIndex;
	private static final double defaultLeafAreaIndex = 1.0;
	
	// OTHERS
	@Description("area.")
	@In
	@Unit("m2")
	public double area;	

	@Description("doHourly allows to chose between the hourly time step"
			+ " or the daily time step. It could be: "
			+ " Hourly--> true or Daily-->false")
	@In
	public boolean doHourly;

	@Description("It is needed to iterate on the date")
	int step;
	public int time;
	double nullValue = -9999.0;
	
	@Description("The first day of the simulation.")
	@In
	public String tStartDate;

	CoordinateReferenceSystem targetCRS = DefaultGeographicCRS.WGS84;
	LinkedHashMap<Integer, Coordinate> cellGrid;
	
	// OUTPUT
	@Description("The output diffuse radiation map")
	@Out
	public GridCoverage2D outTranspirationGrid;
	WritableRaster rasterGrid;
	
	WritableRaster demElevationMap;
	WritableRaster temperatureMap;
	WritableRaster shortWaveRadiationDirectMap;
	WritableRaster shortWaveRadiationDiffuseMap;
	WritableRaster longWaveRadiationMap;
	WritableRaster relativeHumidityMap;
	WritableRaster windVelocityMap;
	WritableRaster atmosphericPressureMap;
	WritableRaster leafAreaIndexMap;
	int columns;
	int rows;
	double dx;
	RegionMap regionMap;
	public GridGeometry2D inInterpolationGrid;
	public GridCoverage2D inGridCoverage2D = null;
	WritableRaster demWR;
	DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").withZone(DateTimeZone.UTC);
	double elevation;	

	// METHODS FROM CLASSES
	SensibleHeatMethods sensibleHeat 	= new SensibleHeatMethods();
	LatentHeatMethods latentHeat 		= new LatentHeatMethods();
	PressureMethods pressure 			= new PressureMethods(); 
	RadiationMethod radiationMethods 	= new RadiationMethod();
	SolarGeometry solarGeometry 		= new SolarGeometry();
	
	@Execute
	
	public void process() throws Exception {
		if (doHourly == true) {
			time =3600;
			} else {
			time = 86400;
			}
		DateTime startDateTime = formatter.parseDateTime(tStartDate);
		DateTime date=(doHourly==false)?startDateTime.plusDays(step).plusHours(12):startDateTime.plusHours(step);
		Leaf propertyOfLeaf = new Leaf();
		double poreRadius = propertyOfLeaf.poreRadius;
		double poreArea = propertyOfLeaf.poreArea;
		double poreDepth = propertyOfLeaf.poreDepth;
		double poreDensity = propertyOfLeaf.poreDensity;
		double leafLength = propertyOfLeaf.length;
		int leafSide = propertyOfLeaf.side;
		// Shortwave property
		double longWaveEmittance = propertyOfLeaf.longWaveEmittance;
		
		
		demElevationMap 			=	mapsTransform(inDemElevationGrid);	
		temperatureMap				=	mapsTransform(inAirTemperatureGrid);	
		shortWaveRadiationDirectMap	=	mapsTransform(inShortWaveRadiationDirectGrid);
		shortWaveRadiationDiffuseMap=	mapsTransform(inShortWaveRadiationDiffuseGrid);
		longWaveRadiationMap		=	mapsTransform(inLongWaveRadiationGrid);
		if (relativeHumidityMap!= null) relativeHumidityMap			=	mapsTransform(inRelativeHumidityGrid);
		if (windVelocityMap!= null) windVelocityMap				=	mapsTransform(inWindVelocityGrid);
		if (atmosphericPressureMap!= null) atmosphericPressureMap		=	mapsTransform(inAtmosphericPressureGrid);
		if (leafAreaIndexMap!= null)leafAreaIndexMap			=   mapsTransform(inLeafAreaIndexGrid);

		// get the dimension of the maps
		CoordinateReferenceSystem sourceCRS = inDemElevationGrid.getCoordinateReferenceSystem2D();
		rasterGrid=mapsTransform(inAirTemperatureGrid);
		regionMap = CoverageUtilities.getRegionParamsFromGridCoverage(inAirTemperatureGrid);
		columns = regionMap.getCols();
		rows = regionMap.getRows();
		dx=regionMap.getXres();

		GridGeometry2D inDemGridGeo = inDemElevationGrid.getGridGeometry();
		cellGrid = getCoordinate(inDemGridGeo);
		WritableRaster outSoWritableRaster = CoverageUtilities.createDoubleWritableRaster(columns, rows, null, null, null);
		WritableRandomIter outRasterIter = RandomIterFactory.createWritable(outSoWritableRaster, null);     
		int k=0;
		for( int row = 1; row < rows - 1; row++ ) {
			for( int column = 1; column < columns - 1; column++ ) {
				demElevation 		= 	demElevationMap.getSampleDouble(column, row, 0);
				if (demElevation == (nullValue)) {elevation = defaultDemElevation;}
				Coordinate coordinate = (Coordinate) cellGrid.get(k);
				k++;
				Point [] idPoint=getPoint(coordinate,sourceCRS, targetCRS);			
				double longitude = (idPoint[0].getX());
				double latitude = Math.toRadians(idPoint[0].getY());
				double solarElevationAngle = solarGeometry.getSolarElevationAngle(date, latitude, longitude, doHourly);	
				
				airTemperature 		= 	temperatureMap.getSampleDouble(column, row, 0)+273.0;
				if (airTemperature == (nullValue+273.0)) {airTemperature = defaultAirTemperature;}		
				leafTemperature = airTemperature;
			
				shortWaveRadiationDirect 	= 	shortWaveRadiationDirectMap.getSampleDouble(column, row, 0);
				if (shortWaveRadiationDirect == nullValue) {shortWaveRadiationDirect = defaultShortWaveRadiationDirect;}   
				shortWaveRadiationDiffuse 	= 	shortWaveRadiationDiffuseMap.getSampleDouble(column, row, 0);
				if (shortWaveRadiationDiffuse == nullValue) {shortWaveRadiationDiffuse = 0.159*shortWaveRadiationDirect;}   

				longWaveRadiation 	= 	longWaveRadiationMap.getSampleDouble(column, row, 0);
				if (longWaveRadiation == nullValue) {longWaveRadiation = 1 * stefanBoltzmannConstant * pow (airTemperature, 4);}

				relativeHumidity= defaultRelativeHumidity;
				if (relativeHumidityMap!= null)
				relativeHumidity = relativeHumidityMap.getSampleDouble(column, row, 0);
				if (relativeHumidity == nullValue) relativeHumidity=defaultRelativeHumidity;
			
				windVelocity= defaultWindVelocity;
				if (windVelocityMap!= null)
				windVelocity = windVelocityMap.getSampleDouble(column, row, 0);
				if (windVelocity == nullValue) windVelocity=defaultWindVelocity;
			
				atmosphericPressure= pressure.computePressure(defaultAtmosphericPressure, massAirMolecule, gravityConstant, elevation,boltzmannConstant, airTemperature);
				if (atmosphericPressureMap!= null)
				atmosphericPressure = atmosphericPressureMap.getSampleDouble(column, row, 0);
				if (atmosphericPressure == nullValue) atmosphericPressure=pressure.computePressure(defaultAtmosphericPressure, massAirMolecule, gravityConstant, elevation,boltzmannConstant, airTemperature);
			

				leafAreaIndex= defaultLeafAreaIndex;
				if (leafAreaIndexMap!= null)
				leafAreaIndex = leafAreaIndexMap.getSampleDouble(column, row, 0);
				leafAreaIndex=(leafAreaIndex>100)?defaultLeafAreaIndex:leafAreaIndex/10;
				if (leafAreaIndex == nullValue) leafAreaIndex=defaultLeafAreaIndex;		
			
			/*	windVelocity 		= 	windVelocityMap.getSampleDouble(column, row, 0);
			if (windVelocity == nullValue) {windVelocity = defaultWindVelocity;}   

			atmosphericPressure = 	atmosphericPressureMap.getSampleDouble(column, row, 0);
			if (atmosphericPressure == nullValue) {atmosphericPressure = pressure.computePressure(defaultAtmosphericPressure, massAirMolecule, gravityConstant, elevation,boltzmannConstant, airTemperature);}	
						
			leafAreaIndex = 	leafAreaIndexMap.getSampleDouble(column, row, 0);
			if (leafAreaIndex == nullValue) {leafAreaIndex = defaultLeafAreaIndex;}	
			else if (leafAreaIndex > 100) {leafAreaIndex = defaultLeafAreaIndex;}	
			else {leafAreaIndex = leafAreaIndex/10;}*/
		/*	relativeHumidity 	= 	relativeHumidityMap.getSampleDouble(column, row, 0);
			if (relativeHumidity == nullValue) {relativeHumidity = defaultRelativeHumidity;}

			windVelocity 		= 	windVelocityMap.getSampleDouble(column, row, 0);
			if (windVelocity == nullValue) {windVelocity = defaultWindVelocity;}   

			atmosphericPressure = 	atmosphericPressureMap.getSampleDouble(column, row, 0);
			if (atmosphericPressure == nullValue) {atmosphericPressure = pressure.computePressure(defaultAtmosphericPressure, massAirMolecule, gravityConstant, elevation,boltzmannConstant, airTemperature);}	
						
			leafAreaIndex = 	leafAreaIndexMap.getSampleDouble(column, row, 0);
			if (leafAreaIndex == nullValue) {leafAreaIndex = defaultLeafAreaIndex;}	
			else if (leafAreaIndex > 100) {leafAreaIndex = defaultLeafAreaIndex;}	
			else {leafAreaIndex = leafAreaIndex/10;}*/
			
				double saturationVaporPressure = pressure.computeSaturationVaporPressure(airTemperature, waterMolarMass, latentHeatEvaporation, molarGasConstant);
				double vaporPressure = relativeHumidity * saturationVaporPressure/100.0;
				double delta = pressure.computeDelta(airTemperature, waterMolarMass, latentHeatEvaporation, molarGasConstant);
			
				double convectiveTransferCoefficient = sensibleHeat.computeConvectiveTransferCoefficient(airTemperature, windVelocity, leafLength, criticalReynoldsNumber, prandtlNumber);
				double sensibleHeatTransferCoefficient = sensibleHeat.computeSensibleHeatTransferCoefficient(convectiveTransferCoefficient, leafSide);
				double latentHeatTransferCoefficient = latentHeat.computeLatentHeatTransferCoefficient(airTemperature, atmosphericPressure, leafSide, convectiveTransferCoefficient, airSpecificHeat, 
					airDensity, molarGasConstant, molarVolume, waterMolarMass, latentHeatEvaporation, poreDensity, poreArea, poreDepth, poreRadius);
					
				double shortWaveRadiationInSun = radiationMethods.computeAbsordebRadiationSunlit(leafAreaIndex, solarElevationAngle, shortWaveRadiationDirect,shortWaveRadiationDiffuse);
				double residual = 1.0;
				double latentHeatFlux = 0;
				double sensibleHeatFlux = 0;
				double netLongWaveRadiation = 0;
				double leafTemperatureSun = leafTemperature;
				double TranspirationSun = 0;
				double TranspirationShadow = 0;
				int iterator = 0;
				double leafInSunlit = radiationMethods.computeSunlitLeafAreaIndex(leafAreaIndex, solarElevationAngle);

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
						
		double shortWaveRadiationInShadow = radiationMethods.computeAbsordebRadiationShadow(leafAreaIndex, solarElevationAngle, shortWaveRadiationDirect,shortWaveRadiationDiffuse);
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
		
		outRasterIter.setSample(column, row, 0,TotalTranspiration);
	
			}
		}
	CoverageUtilities.setNovalueBorder(outSoWritableRaster);
	outTranspirationGrid = CoverageUtilities.buildCoverage("Transpiration", outSoWritableRaster,regionMap, inAirTemperatureGrid.getCoordinateReferenceSystem());
	//step++;
	
	}

private WritableRaster mapsTransform  ( GridCoverage2D inValues){	
	RenderedImage inValuesRenderedImage = inValues.getRenderedImage();
	WritableRaster inValuesWR = CoverageUtilities.replaceNovalue(inValuesRenderedImage, -9999.0);
	inValuesRenderedImage = null;
	return inValuesWR;
}
private Point[] getPoint(Coordinate coordinate, CoordinateReferenceSystem sourceCRS, CoordinateReferenceSystem targetCRS)
		throws Exception{
	Point[] point = new Point[] { GeometryUtilities.gf().createPoint(coordinate) };
	CrsUtilities.reproject(sourceCRS, targetCRS, point);
	return point;
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
	double leafTemperature = (shortWaveRadiation + sensibleHeatTransferCoefficient*airTemperature +
			latentHeatTransferCoefficient*(delta*airTemperature + vaporPressure - saturationVaporPressure) + 
			side * emissivity * stefanBoltzmannConstant * 4 * pow(airTemperature,4))*
			(1/(sensibleHeatTransferCoefficient + latentHeatTransferCoefficient * delta +	
			side * emissivity * stefanBoltzmannConstant * 4 * pow(airTemperature,3)));
	return leafTemperature;	
}
private LinkedHashMap<Integer, Coordinate> getCoordinate(GridGeometry2D grid) {
	LinkedHashMap<Integer, Coordinate> out = new LinkedHashMap<Integer, Coordinate>();
	int count = 0;
	RegionMap regionMap = CoverageUtilities.gridGeometry2RegionParamsMap(grid);
	double cols = regionMap.getCols();
	double rows = regionMap.getRows();
	double south = regionMap.getSouth();
	double west = regionMap.getWest();
	double xres = regionMap.getXres();
	double yres = regionMap.getYres();
	double northing = south;
	double easting = west;
	for (int i = 0; i < cols; i++) {
		easting = easting + xres;
		for (int j = 0; j < rows; j++) {
			northing = northing + yres;
			Coordinate coordinate = new Coordinate();
			coordinate.x = west + i * xres;
			coordinate.y = south + j * yres;
			out.put(count, coordinate);
			count++;
		}
	}

	return out;
}
}