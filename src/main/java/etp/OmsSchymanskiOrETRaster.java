package etp;
//import static org.jgrasstools.gears.libs.modules.ModelsEngine.calcInverseSunVector;
//import static org.jgrasstools.gears.libs.modules.ModelsEngine.calcNormalSunVector;
//import static org.jgrasstools.gears.libs.modules.ModelsEngine.calculateFactor;
//import static org.jgrasstools.gears.libs.modules.ModelsEngine.scalarProduct;

//import org.jgrasstools.gears.libs.modules.JGTConstants;

import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
//import java.io.IOException;
//import java.util.ArrayList;

import java.util.LinkedHashMap;

import static java.lang.Math.abs;
import static java.lang.Math.exp;
import static java.lang.Math.pow;
//import static org.jgrasstools.gears.libs.modules.JGTConstants.isNovalue;

import javax.media.jai.RasterFactory;
import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;
import javax.media.jai.iterator.WritableRandomIter;

import oms3.annotations.Author;
//import oms3.annotations.Bibliography;
import oms3.annotations.Description;
//import oms3.annotations.Documentation;
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

//import org.jgrasstools.gears.utils.CrsUtilities;
import org.jgrasstools.gears.utils.RegionMap;
import org.jgrasstools.gears.utils.coverage.CoverageUtilities;
//import org.jgrasstools.gears.utils.geometry.GeometryUtilities;
//import org.joda.time.DateTime;
//import org.joda.time.DateTimeZone;
//import org.joda.time.format.DateTimeFormat;
//import org.joda.time.format.DateTimeFormatter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Coordinate;
//import com.vividsolutions.jts.geom.Point;
//import flanagan.analysis.Regression;
//import krigingsPointCase.StationsSelection;

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
public class OmsSchymanskiOrETRaster extends JGTModel {
	
	//TEMPERATURE
	@Description("The map of the interpolated temperature.")
	@In
	public GridCoverage2D inAirTemperatureGrid;
	@Description("The double value of the air temperature")
	double airTemperature;
	private static final double defaultAirTemperature = 15.0+273.0;
	
	//SHORTWAVE
	@Description("sw.")
	@In
	public GridCoverage2D inShortWaveRadiationGrid;
	@Description("SW")
	double shortWaveRadiation;
	private static final double defaultShortWaveRadiation = 0.0;

	
	@Description("Leaves length")
	@In
	@Unit("m")
	public double leafLength;
	
	@Description("Leaves side")
	@In
	@Unit("")
	public int leafSide;
	
	@Description("Leaves emissivity")
	@In
	@Unit(" ")
	public double leafEmissivity;
	
	@Description("Leaves temperature")
	@In
	@Unit("K")
	public double leafTemperature;
	
	//LONGWAVE
	@Description("lw.")
	@In
	public GridCoverage2D inLongWaveRadiationGrid;
	@Description("lwe")
	double longWaveRadiation;
	
	//HUMIDITY
	@Description("The map of the interpolated humidity.")
	@In
	public GridCoverage2D inRelativeHumidityGrid;
	@Description("The double value of the humidity")
	double relativeHumidity;
	private static final double defaultRelativeHumidity = 70.0;
	
	//WIND
	@Description("The map of the wind.")
	@In
	public GridCoverage2D inWindVelocityGrid;
	@Description("The double value of the humidity")
	double windVelocity;
	private static final double defaultWindVelocity = 2.0;
	
	//PRESSURE
	@Description("The map of the pressure.")
	@In
	public GridCoverage2D inAtmosphericPressureGrid;
	@Description("The double value of the humidity")
	double atmosphericPressure;
	private static final double defaultAtmosphericPressure = 101325.0;
	
	//LAI
	@Description("Leaf area index.")
	@In
	@Unit("m2 m-2")
	public GridCoverage2D inLeafAreaIndexGrid;
	double leafAreaIndex;
	private static final double defaultLeafAreaIndex = 1.0;
	
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

	//@Description("Final target CRS")
	CoordinateReferenceSystem targetCRS = DefaultGeographicCRS.WGS84;

	//@Description("the linked HashMap with the coordinate of the stations")
	LinkedHashMap<Integer, Coordinate> stationCoordinates;
	

	@Description("The output diffuse radiation map")
	@Out
	public GridCoverage2D outETGrid;
	WritableRaster rasterGrid;


	//WritableRaster normalWR;
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
	
	double waterMolarMass = 0.018;
	double latentHeatEvaporation = 2.45 * pow(10,6);
	double molarGasConstant = 8.314472;
	double nullValue = -9999.0;
	double stefanBoltzmannConstant = 5.670373 * pow(10,-8);
	public int time;
	
	@Execute
	
	public void process() throws Exception {
		
		//rasterGrid=mapsTransform(inGridCoverage2D);
		if(step==0){
		// transform the GrifCoverage2D maps into writable rasters
		temperatureMap			=	mapsTransform(inAirTemperatureGrid);	
		shortWaveRadiationMap	=	mapsTransform(inShortWaveRadiationGrid);
		longWaveRadiationMap	=	mapsTransform(inLongWaveRadiationGrid);
		relativeHumidityMap		=	mapsTransform(inRelativeHumidityGrid);
		windVelocityMap			=	mapsTransform(inWindVelocityGrid);
		atmosphericPressureMap	=	mapsTransform(inAtmosphericPressureGrid);
		leafAreaIndexMap		=   mapsTransform(inLeafAreaIndexGrid);

		//CoordinateReferenceSystem sourceCRS = inAirTemperatureGrid.getCoordinateReferenceSystem2D();


		// get the dimension of the maps
		rasterGrid=mapsTransform(inAirTemperatureGrid);
		regionMap = CoverageUtilities.getRegionParamsFromGridCoverage(inAirTemperatureGrid);
		columns = regionMap.getCols();
		rows = regionMap.getRows();
		dx=regionMap.getXres();
		}
		// compute the vector normal to a grid cell surface.
		//normalWR = normalVector(demWR, dx);
	


	WritableRaster outEtSoWritableRaster = CoverageUtilities.createDoubleWritableRaster(columns, rows, null, null, null);
	WritableRandomIter outEtSoIter = RandomIterFactory.createWritable(outEtSoWritableRaster, null);       

	// get the geometry of the maps and the coordinates of the stations
	GridGeometry2D inAirTemperatureGridGeo = inAirTemperatureGrid.getGridGeometry();
    stationCoordinates = getCoordinate(inAirTemperatureGridGeo);
	// iterate over the entire domain and compute for each pixel the SWE
	for( int row = 1; row < rows - 1; row++ ) {
		for( int column = 1; column < columns - 1; column++ ) {
		//	int k=0;
			// get the exact value of the variable in the pixel i, j 
			airTemperature 		= 	temperatureMap.getSampleDouble(column, row, 0)+273.0;
			if (airTemperature == (nullValue+273.0)) {airTemperature = defaultAirTemperature;}		
			leafTemperature = airTemperature + 2.0;
			
			shortWaveRadiation 	= 	shortWaveRadiationMap.getSampleDouble(column, row, 0);
			if (shortWaveRadiation == nullValue) {shortWaveRadiation = defaultShortWaveRadiation;}   

			double leafAbsorption = 0.8;

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
			
			double absorbedRadiation = shortWaveRadiation * leafAbsorption; //double transmittedRadiation[] = new double[numberOfLayers];
			
			
			if (doHourly == true) {
				time =3600;
				} else {
				time = 86400;
				}

				//double leafTransmittance = 0.1;
			//int numberOfLayers = 3; 
			//double area = 1.0;
			//double absorbedRadiation = new double[numberOfLayers]; //double transmittedRadiation[] = new double[numberOfLayers];
								
			
			double saturationVaporPressure = computeSaturationVaporPressure(airTemperature);
			double vaporPressure = relativeHumidity * saturationVaporPressure/100.0;
			
			double delta = computeDelta (airTemperature);
			SensibleHeatTransferCoefficient cH = new SensibleHeatTransferCoefficient();
			LatentHeatTransferCoefficient cE = new LatentHeatTransferCoefficient();
			
			double convectiveTransferCoefficient = cH.computeConvectiveTransferCoefficient(airTemperature, windVelocity, leafLength);
			double sensibleHeatTransferCoefficient = cH.computeSensibleHeatTransferCoefficient(convectiveTransferCoefficient, leafSide);
			double latentHeatTransferCoefficient = cE.computeLatentHeatTransferCoefficient(airTemperature, atmosphericPressure, leafSide, convectiveTransferCoefficient);
			
			shortWaveRadiation = absorbedRadiation;
			double residual = 1.0;
			double latentHeatFlux = 0;
			double sensibleHeatFlux = 0;
			double netLongWaveRadiation = 0;
			double leafTemperatureSun = leafTemperature;
			double ETsun = 0;
			double ETshadow = 0;
			while(abs(residual) > pow(10,-1)) 
				{
				//deltaLeaf = computeDeltaLeaf(leafTemperatureSun, airTemperature);
				sensibleHeatFlux = computeSensibleHeatFlux(sensibleHeatTransferCoefficient, leafTemperatureSun, airTemperature);
				latentHeatFlux = computeLatentHeatFlux(delta, leafTemperatureSun, airTemperature, latentHeatTransferCoefficient, sensibleHeatTransferCoefficient, vaporPressure, saturationVaporPressure);
				netLongWaveRadiation = computeNetLongWaveRadiation(leafSide,leafEmissivity, airTemperature, leafTemperatureSun);
				residual = (shortWaveRadiation - netLongWaveRadiation) - sensibleHeatFlux - latentHeatFlux;
				leafTemperatureSun = computeLeafTemperature(leafSide, leafEmissivity,sensibleHeatTransferCoefficient,latentHeatTransferCoefficient,airTemperature,shortWaveRadiation,longWaveRadiation,vaporPressure, saturationVaporPressure,delta);
				}
			ETsun = computeLatentHeatFlux(delta, leafTemperatureSun, airTemperature, latentHeatTransferCoefficient, sensibleHeatTransferCoefficient, vaporPressure, saturationVaporPressure);
			//outSOLT.put(basinId, new double[]{leafTemperatureSun});

			if (leafAreaIndex >1.0) {
				
				
			shortWaveRadiation = absorbedRadiation*0.2;
			double residualSh = 1.0;
			double latentHeatFluxSh = 0;
			double sensibleHeatFluxSh = 0;
			double netLongWaveRadiationSh = 0;
			double leafTemperatureSh = leafTemperature;
			
			while(abs(residualSh) > pow(10,-1)) 
				{
				sensibleHeatFluxSh = computeSensibleHeatFlux(sensibleHeatTransferCoefficient, leafTemperatureSh, airTemperature);
				latentHeatFluxSh = computeLatentHeatFlux(delta, leafTemperatureSh, airTemperature, latentHeatTransferCoefficient, sensibleHeatTransferCoefficient, vaporPressure, saturationVaporPressure);
				netLongWaveRadiationSh = computeNetLongWaveRadiation(leafSide,leafEmissivity, airTemperature, leafTemperatureSh);
				residualSh = (shortWaveRadiation- netLongWaveRadiationSh) - sensibleHeatFluxSh - latentHeatFluxSh;
				leafTemperatureSh = computeLeafTemperature(leafSide, leafEmissivity,sensibleHeatTransferCoefficient,latentHeatTransferCoefficient,airTemperature,shortWaveRadiation,longWaveRadiation,vaporPressure, saturationVaporPressure,delta);
				}
			ETshadow = computeLatentHeatFlux(delta, leafTemperatureSh, airTemperature, latentHeatTransferCoefficient, sensibleHeatTransferCoefficient, vaporPressure, saturationVaporPressure);
			}
			double ETout = (ETsun*area + ETshadow*(leafAreaIndex-area))*time/latentHeatEvaporation;

			outEtSoIter.setSample(column, row, 0,ETout);

			//System.out.print(ETout);
			
			// upgrade the step for the new date
			//k++;
			
			//outSOEt.put(basinId, new double[]{(((2.0*ETsun) + (ETshadow*(leafAreaIndex-2.0*area)))*time/latentHeatEvaporation)});
			}
		}
	CoverageUtilities.setNovalueBorder(outEtSoWritableRaster);
	outETGrid = CoverageUtilities.buildCoverage("ET", outEtSoWritableRaster,regionMap, inAirTemperatureGrid.getCoordinateReferenceSystem());
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

/**
 * Gets the coordinate of each pixel of the given map.
 *
 * @param GridGeometry2D grid is the map 
 * @return the coordinate of each point
 */
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

/**
 * Gets the point.
 *
 * @param coordinate the coordinate
 * @param sourceCRS is the source crs
 * @param targetCRS the target crs
 * @return the point
 * @throws Exception the exception
 */
//private Point[] getPoint(Coordinate coordinate, CoordinateReferenceSystem sourceCRS, CoordinateReferenceSystem targetCRS) 
//		throws Exception{
//	Point[] point = new Point[] { GeometryUtilities.gf().createPoint(coordinate) };
//	CrsUtilities.reproject(sourceCRS, targetCRS, point);
//	return point;
//}

/**
 * Compute the correction factor related to Earth’s orbit eccentricity.
 *
 * @param date is the current date
 * @return the double value of E0
 */


protected WritableRaster normalVector(WritableRaster rasterGrid, double res) {

	int minX = rasterGrid.getMinX();
	int minY = rasterGrid.getMinY();
	int rows = rasterGrid.getHeight();
	int cols = rasterGrid.getWidth();

	RandomIter pitIter = RandomIterFactory.create(rasterGrid, null);
	/*
	 * Initialize the image of the normal vector in the central point of the
	 * cells, which have 3 components (X;Y;Z), so the Image have 3 bands..
	 */
	SampleModel sm = RasterFactory.createBandedSampleModel(5, cols, rows, 3);
	WritableRaster tmpNormalVectorWR = CoverageUtilities .createDoubleWritableRaster(cols, rows, null, sm, 0.0);
	WritableRandomIter tmpNormalIter = RandomIterFactory.createWritable( tmpNormalVectorWR, null);
	/*
	 * apply the corripio's formula 
	 */
	for (int j = minY; j < minX + rows - 1; j++) {
		for (int i = minX; i < minX + cols - 1; i++) {
			double zij = pitIter.getSampleDouble(i, j, 0);
			double zidxj = pitIter.getSampleDouble(i + 1, j, 0);
			double zijdy = pitIter.getSampleDouble(i, j + 1, 0);
			double zidxjdy = pitIter.getSampleDouble(i + 1, j + 1, 0);
			double firstComponent = res * (zij - zidxj + zijdy - zidxjdy);
			double secondComponent = res * (zij + zidxj - zijdy - zidxjdy);
			double thirthComponent = 2 * (res * res);
			double den = Math.sqrt(firstComponent * firstComponent
					+ secondComponent * secondComponent + thirthComponent
					* thirthComponent);
			tmpNormalIter.setPixel(i, j, new double[] {
					firstComponent / den, secondComponent / den,
					thirthComponent / den });

		}
	}
	pitIter.done();

	return tmpNormalVectorWR;

}
private double computeSaturationVaporPressure(double airTemperature) {
	 // Computation of the saturation vapor pressure at air temperature [Pa]
	double saturationVaporPressure = 611.0 * exp((waterMolarMass*latentHeatEvaporation/molarGasConstant)*((1.0/273.0)-(1.0/airTemperature)));
	return saturationVaporPressure;
}
private double computeDelta (double airTemperature) {
	// Computation of delta [Pa K-1]
	// Slope of saturation vapor pressure at air temperature
	double numerator = 611 * waterMolarMass * latentHeatEvaporation;
	double exponential = exp((waterMolarMass * latentHeatEvaporation / molarGasConstant)*((1/273.0)-(1/airTemperature)));
	double denominator = (molarGasConstant * pow(airTemperature,2));
	double delta = numerator * exponential / denominator;
	return delta;
}
//private double computeDeltaLeaf (double airTemperature,double leafTemperature) {
	// Computation of delta [Pa K-1]
	// Slope of saturation vapor pressure at air temperature
//	double first = 611 * exp((waterMolarMass * latentHeatEvaporation / molarGasConstant)*((1/273.0)-(1/leafTemperature)));
//	double second = 611 * exp((waterMolarMass * latentHeatEvaporation / molarGasConstant)*((1/273.0)-(1/airTemperature)));
//	double deltaLeaf = (first - second)/(leafTemperature - airTemperature);
//	return deltaLeaf;
//}
//private double computeLongWaveRadiation(double side, double emissivity, double Temperature) {
//	double longWaveRadiation = 4 * side * emissivity * stefanBoltzmannConstant * (pow (Temperature, 4));
//	return longWaveRadiation;	
//}
private double computeNetLongWaveRadiation(double leafSide, double leafEmissivity, double airTemperature, double leafTemperature) {
	 // Compute the net long wave radiation i.e. the incoming minus outgoing [J m-2 s-1]
	double longWaveRadiation = 4 * leafSide * leafEmissivity * stefanBoltzmannConstant * (((pow (airTemperature, 3))*leafTemperature - (pow (airTemperature, 4))));
	return longWaveRadiation;	
}
private double computeLatentHeatFlux(double delta, double leafTemperature, double airTemperature, double latentHeatTransferCoefficient,double sensibleHeatTransferCoefficient, double vaporPressure, double saturationVaporPressure) {
	 // Computation of the latent heat flux from leaf [J m-2 s-1]
	double latentHeatFlux = (sensibleHeatTransferCoefficient* (delta * (leafTemperature - airTemperature) + saturationVaporPressure - vaporPressure))/(sensibleHeatTransferCoefficient/latentHeatTransferCoefficient);
	return latentHeatFlux;	
}
private double computeSensibleHeatFlux(double sensibleHeatTransferCoefficient, double leafTemperature, double airTemperature) {
	 // Computation of the sensible heat flux from leaf [J m-2 s-1]
	double sensibleHeatFlux = sensibleHeatTransferCoefficient * (leafTemperature - airTemperature);
	return sensibleHeatFlux;	
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