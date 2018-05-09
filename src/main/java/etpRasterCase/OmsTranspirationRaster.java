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
import org.jgrasstools.gears.utils.RegionMap;
import org.jgrasstools.gears.utils.coverage.CoverageUtilities;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import com.vividsolutions.jts.geom.Coordinate;

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
	public GridCoverage2D inShortWaveRadiationGrid;
	@Description("SW")
	double shortWaveRadiation;
	private static final double defaultShortWaveRadiation = 0.0;

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

	CoordinateReferenceSystem targetCRS = DefaultGeographicCRS.WGS84;
	LinkedHashMap<Integer, Coordinate> stationCoordinates;
	
	// OUTPUT
	@Description("The output diffuse radiation map")
	@Out
	public GridCoverage2D outTranspirationGrid;
	WritableRaster rasterGrid;
	
	WritableRaster demElevationMap;
	WritableRaster temperatureMap;
	WritableRaster shortWaveRadiationMap;
	WritableRaster longWaveRadiationMap;
	WritableRaster relativeHumidityMap;
	WritableRaster windVelocityMap;
	WritableRaster atmosphericPressureMap;
	WritableRaster leafAreaIndexMap;
	int columns;
	int rows;
	double dx;
	RegionMap regionMap;
	

	// METHODS FROM CLASSES
	SensibleHeat sensibleHeat = new SensibleHeat();
	LatentHeat latentHeat = new LatentHeat();
	Pressures getPressure = new Pressures(); 
	LongWaveRadiationBalance longWaveRadiationBalance = new LongWaveRadiationBalance();
	
	//private WritableRaster outWR;

	public GridGeometry2D inInterpolationGrid;
	public GridCoverage2D inGridCoverage2D = null;
	WritableRaster demWR;
	double elevation;

	@Execute
	
	public void process() throws Exception {
		if(step==0){
		// transform the GrifCoverage2D maps into writable rasters
		demElevationMap 		=	mapsTransform(inDemElevationGrid);	
		temperatureMap			=	mapsTransform(inAirTemperatureGrid);	
		shortWaveRadiationMap	=	mapsTransform(inShortWaveRadiationGrid);
		longWaveRadiationMap	=	mapsTransform(inLongWaveRadiationGrid);
		relativeHumidityMap		=	mapsTransform(inRelativeHumidityGrid);
		windVelocityMap			=	mapsTransform(inWindVelocityGrid);
		atmosphericPressureMap	=	mapsTransform(inAtmosphericPressureGrid);
		leafAreaIndexMap		=   mapsTransform(inLeafAreaIndexGrid);

		// get the dimension of the maps
		rasterGrid=mapsTransform(inAirTemperatureGrid);
		regionMap = CoverageUtilities.getRegionParamsFromGridCoverage(inAirTemperatureGrid);
		columns = regionMap.getCols();
		rows = regionMap.getRows();
		dx=regionMap.getXres();
		}

	WritableRaster outSoWritableRaster = CoverageUtilities.createDoubleWritableRaster(columns, rows, null, null, null);
	WritableRandomIter outRasterIter = RandomIterFactory.createWritable(outSoWritableRaster, null);       

	for( int row = 1; row < rows - 1; row++ ) {
		for( int column = 1; column < columns - 1; column++ ) {
			
			if (doHourly == true) {
				time =3600;
				} else {
				time = 86400;
				}
			Leaf propertyOfLeaf = new Leaf();
			double poreRadius = propertyOfLeaf.poreRadius;
			double poreArea = propertyOfLeaf.poreArea;
			double poreDepth = propertyOfLeaf.poreDepth;
			double poreDensity = propertyOfLeaf.poreDensity;
			double leafLength = propertyOfLeaf.length;
			int leafSide = propertyOfLeaf.side;
			// Shortwave property
			double shortWaveAbsorption = propertyOfLeaf.shortWaveAbsorption;	
			double shortWaveReflectance = propertyOfLeaf.shortWaveReflectance;	
			double shortWaveTransmittance = propertyOfLeaf.shortWaveTransmittance;
			// Longwave property
			double longWaveAbsorption = propertyOfLeaf.longWaveAbsorption;	
			double longWaveReflectance = propertyOfLeaf.longWaveReflectance;	
			double longWaveTransmittance = propertyOfLeaf.longWaveTransmittance;
			double longWaveEmittance = propertyOfLeaf.longWaveEmittance;
			
			airTemperature 		= 	temperatureMap.getSampleDouble(column, row, 0)+273.0;
			if (airTemperature == (nullValue+273.0)) {airTemperature = defaultAirTemperature;}
			
			leafTemperature = airTemperature + 2.0;
			
			demElevation 		= 	demElevationMap.getSampleDouble(column, row, 0);
			if (demElevation == (nullValue)) {elevation = defaultDemElevation;}
			
			shortWaveRadiation 	= 	shortWaveRadiationMap.getSampleDouble(column, row, 0);
			if (shortWaveRadiation == nullValue) {shortWaveRadiation = defaultShortWaveRadiation;}   

			double absorbedRadiation = shortWaveRadiation * shortWaveAbsorption;

			longWaveRadiation 	= 	longWaveRadiationMap.getSampleDouble(column, row, 0);
			if (longWaveRadiation == nullValue) {longWaveRadiation = 1 * stefanBoltzmannConstant * pow (airTemperature, 4);}

			relativeHumidity 	= 	relativeHumidityMap.getSampleDouble(column, row, 0);
			if (relativeHumidity == nullValue) {relativeHumidity = defaultRelativeHumidity;}

			windVelocity 		= 	windVelocityMap.getSampleDouble(column, row, 0);
			if (windVelocity == nullValue) {windVelocity = defaultWindVelocity;}   

			atmosphericPressure = 	atmosphericPressureMap.getSampleDouble(column, row, 0);
			if (atmosphericPressure == nullValue) {atmosphericPressure = defaultAtmosphericPressure;}	
						
			leafAreaIndex = 	leafAreaIndexMap.getSampleDouble(column, row, 0);
			if (leafAreaIndex == nullValue) {leafAreaIndex = defaultLeafAreaIndex;}	
			else if (leafAreaIndex > 100) {leafAreaIndex = defaultLeafAreaIndex;}	
			else {leafAreaIndex = leafAreaIndex/10;}
			
			double saturationVaporPressure = getPressure.computeSaturationVaporPressure(airTemperature, waterMolarMass, latentHeatEvaporation, molarGasConstant);
			double vaporPressure = relativeHumidity * saturationVaporPressure/100.0;
			double delta = getPressure.computeDelta(airTemperature, waterMolarMass, latentHeatEvaporation, molarGasConstant);
			
			double convectiveTransferCoefficient = sensibleHeat.computeConvectiveTransferCoefficient(airTemperature, windVelocity, leafLength, criticalReynoldsNumber, prandtlNumber);
			double sensibleHeatTransferCoefficient = sensibleHeat.computeSensibleHeatTransferCoefficient(convectiveTransferCoefficient, leafSide);
			double latentHeatTransferCoefficient = latentHeat.computeLatentHeatTransferCoefficient(airTemperature, atmosphericPressure, leafSide, convectiveTransferCoefficient, airSpecificHeat, 
					airDensity, molarGasConstant, molarVolume, waterMolarMass, latentHeatEvaporation, poreDensity, poreArea, poreDepth, poreRadius);
					
				
			
			shortWaveRadiation = absorbedRadiation;
			double residual = 1.0;
			double latentHeatFlux = 0;
			double sensibleHeatFlux = 0;
			double netLongWaveRadiation = 0;
			double leafTemperatureSun = leafTemperature;
			double TranspirationSun = 0;
			double TranspirationShadow = 0;
			while(abs(residual) > pow(10,-1)) 
			{
				//deltaLeaf = computeDeltaLeaf(leafTemperatureSun, airTemperature);
				sensibleHeatFlux = sensibleHeat.computeSensibleHeatFlux(sensibleHeatTransferCoefficient, leafTemperatureSun, airTemperature);
				latentHeatFlux = latentHeat.computeLatentHeatFlux(delta, leafTemperatureSun, airTemperature, latentHeatTransferCoefficient, sensibleHeatTransferCoefficient, vaporPressure, saturationVaporPressure);
				netLongWaveRadiation = longWaveRadiationBalance.computeLongWaveRadiationBalance(leafSide, longWaveEmittance, airTemperature, leafTemperatureSun, stefanBoltzmannConstant);
				residual = (shortWaveRadiation - netLongWaveRadiation) - sensibleHeatFlux - latentHeatFlux;
				leafTemperatureSun = computeLeafTemperature(leafSide, longWaveEmittance, sensibleHeatTransferCoefficient,latentHeatTransferCoefficient,airTemperature,shortWaveRadiation,longWaveRadiation,vaporPressure, saturationVaporPressure,delta);
				}
			TranspirationSun = latentHeat.computeLatentHeatFlux(delta, leafTemperatureSun, airTemperature, latentHeatTransferCoefficient, sensibleHeatTransferCoefficient, vaporPressure, saturationVaporPressure);
						
			shortWaveRadiation = absorbedRadiation*0.2;
			double residualSh = 1.0;
			double latentHeatFluxSh = 0;
			double sensibleHeatFluxSh = 0;
			double netLongWaveRadiationSh = 0;
			double leafTemperatureSh = leafTemperature;
			
			while(abs(residualSh) > pow(10,-1)) 
				{
				sensibleHeatFluxSh = sensibleHeat.computeSensibleHeatFlux(sensibleHeatTransferCoefficient, leafTemperatureSh, airTemperature);
				latentHeatFluxSh = latentHeat.computeLatentHeatFlux(delta, leafTemperatureSh, airTemperature, latentHeatTransferCoefficient, sensibleHeatTransferCoefficient, vaporPressure, saturationVaporPressure);
				netLongWaveRadiationSh = longWaveRadiationBalance.computeLongWaveRadiationBalance(leafSide, longWaveEmittance, airTemperature, leafTemperatureSh, stefanBoltzmannConstant);
				residualSh = (shortWaveRadiation- netLongWaveRadiationSh) - sensibleHeatFluxSh - latentHeatFluxSh;
				leafTemperatureSh = computeLeafTemperature(leafSide, longWaveEmittance,sensibleHeatTransferCoefficient,latentHeatTransferCoefficient,airTemperature,shortWaveRadiation,longWaveRadiation,vaporPressure, saturationVaporPressure,delta);
				}
			TranspirationShadow = latentHeat.computeLatentHeatFlux(delta, leafTemperatureSh, airTemperature, latentHeatTransferCoefficient, sensibleHeatTransferCoefficient, vaporPressure, saturationVaporPressure);			
			
			double TranspirationOut = (TranspirationSun*area + TranspirationShadow*(leafAreaIndex-area))*time/latentHeatEvaporation;

			outRasterIter.setSample(column, row, 0,TranspirationOut);
			}
		}
	CoverageUtilities.setNovalueBorder(outSoWritableRaster);
	outTranspirationGrid = CoverageUtilities.buildCoverage("Transpiration", outSoWritableRaster,regionMap, inAirTemperatureGrid.getCoordinateReferenceSystem());
	step++;
	}
//////////////////////////////////////////////////////////////
/**
 * Maps reader transform the GrifCoverage2D in to the writable raster and
 * replace the -9999.0 value with no value.
 *
 * @param inValues: the input map values
 * @return the writable raster of the given map
 */
private WritableRaster mapsTransform  ( GridCoverage2D inValues){	
	RenderedImage inValuesRenderedImage = inValues.getRenderedImage();
	WritableRaster inValuesWR = CoverageUtilities.replaceNovalue(inValuesRenderedImage, -9999.0);
	inValuesRenderedImage = null;
	return inValuesWR;
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
}