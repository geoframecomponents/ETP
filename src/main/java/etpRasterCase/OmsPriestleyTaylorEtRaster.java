package etp;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.util.LinkedHashMap;
import static java.lang.Math.exp;
import static java.lang.Math.pow;
import javax.media.jai.RasterFactory;
import javax.media.jai.iterator.RandomIter;
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
import org.jgrasstools.gears.libs.modules.JGTConstants;
import org.jgrasstools.gears.libs.modules.JGTModel;
import org.jgrasstools.gears.utils.RegionMap;
import org.jgrasstools.gears.utils.coverage.CoverageUtilities;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
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
public class OmsPriestleyTaylorEtRaster extends JGTModel {
	
		//TEMPERATURE
	@Description("The map of the interpolated temperature.")
	@In
	public GridCoverage2D inAirTemperatureGrid;
	@Description("The double value of the air temperature")
	double airTemperature;
	private static final double defaultAirTemperature = 15.0;
	
	//SHORTWAVE
	@Description("sw.")
	@In
	public GridCoverage2D inNetRadiationGrid;
	@Description("Net Radiation")
	double netRadiation;
	private static final double defaultNetRadiation = 0.0;
	
	@Description("The alpha.")
	@In
	@Unit("m")
	public double pAlpha = 0;

	@Description("The coefficient for the soil heat flux during daylight")
	@In
	public double pGmorn = 0;

	@Description("The coefficient for the soil heat flux during nighttime")
	@In
	public double pGnight = 0;
	
	@Description("The mean hourly air temperature.")
	@In
	public String tStartDate;
	
	double lambda = 2.45*pow(10,6);
	
	@Description("The pressure default value in case of missing data.")
	@In
	@Unit("KPa")
	public double defaultPressure = 101.325;

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
	public GridCoverage2D outEtPtGrid;
	WritableRaster rasterGrid;
	
	private DateTimeFormatter formatter = JGTConstants.utcDateFormatterYYYYMMDDHHMM;

	//WritableRaster normalWR;
	WritableRaster temperatureMap;
	WritableRaster netRadiationMap;
		
	int columns;
	int rows;
	double dx;
	RegionMap regionMap;
	
	double nullValue = -9999.0;
	public int time;
	
	@Execute
	
	public void process() throws Exception {
		
		//rasterGrid=mapsTransform(inGridCoverage2D);
		if(step==0){
		// transform the GrifCoverage2D maps into writable rasters
		temperatureMap	= mapsTransform(inAirTemperatureGrid);	
		netRadiationMap	= mapsTransform(inNetRadiationGrid);
		// get the dimension of the maps
		rasterGrid		= mapsTransform(inAirTemperatureGrid);
		regionMap 		= CoverageUtilities.getRegionParamsFromGridCoverage(inAirTemperatureGrid);
		columns 		= regionMap.getCols();
		rows 			= regionMap.getRows();
		dx				= regionMap.getXres();
		}
		
		
		DateTime startDateTime = formatter.parseDateTime(tStartDate);
		DateTime date=(doHourly==false)?startDateTime.plusDays(step):startDateTime.plusHours(step).plusMinutes(30); 
	
		WritableRaster outEtPtWritableRaster = CoverageUtilities.createDoubleWritableRaster(columns, rows, null, null, null);
		WritableRandomIter outEtPtIter = RandomIterFactory.createWritable(outEtPtWritableRaster, null);       

	// get the geometry of the maps and the coordinates of the stations
	GridGeometry2D inAirTemperatureGridGeo = inAirTemperatureGrid.getGridGeometry();
    stationCoordinates = getCoordinate(inAirTemperatureGridGeo);
	// iterate over the entire domain and compute for each pixel the SWE
	for( int row = 1; row < rows - 1; row++ ) {
		for( int column = 1; column < columns - 1; column++ ) {
		//	int k=0;
			// get the exact value of the variable in the pixel i, j 
			airTemperature 		= 	temperatureMap.getSampleDouble(column, row, 0);
			if (airTemperature == (nullValue)) {airTemperature = defaultAirTemperature;}		
			
			netRadiation 	= 	netRadiationMap.getSampleDouble(column, row, 0);
			if (netRadiation == nullValue) {netRadiation = defaultNetRadiation;}   
			
			if (doHourly == true) {
				time =3600;
				} else {
				time = 86400;
				}
			
			int ora = date.getHourOfDay();
			boolean isLigth = false;
			if (ora > 6 && ora < 18) {
				isLigth = true;
			}

			double etp = (netRadiation<0)?0:compute(pGmorn, pGnight, pAlpha, netRadiation, airTemperature, defaultPressure, isLigth, lambda, doHourly);
			etp=(etp<0)?0:etp;
			etp = etp*time;
			outEtPtIter.setSample(column, row, 0,etp);
		}		
	}	
	CoverageUtilities.setNovalueBorder(outEtPtWritableRaster);
	outEtPtGrid = CoverageUtilities.buildCoverage("ET", outEtPtWritableRaster,regionMap, inAirTemperatureGrid.getCoordinateReferenceSystem());
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
private double compute( double ggm, double ggn, double alpha, double NetRad, double AirTem, double AtmPres, boolean islight,double lambda,
		boolean ishourlyo ) {
	double result = 0;
	if (ishourlyo == true) {
		double den_Delta = (AirTem + 237.3) * (AirTem + 237.3);
		double exp_Delta = (17.27 * AirTem) / (AirTem + 237.3);
		double num_Delta = 4098 * (0.6108 * exp(exp_Delta));
		double Delta = num_Delta / den_Delta;
		//double lambda = 2.501 - 0.002361 * AirTem;
		double gamma = 1013 * AtmPres / (0.622 * lambda);
		double coeff_G;
		if (islight == true) {
			coeff_G = ggm;
		} else {
			coeff_G = ggn;
		}
		double G = coeff_G * NetRad;
		result = (alpha) * Delta * (NetRad - G) / ((gamma + Delta) * lambda);
	} else {
		double den_Delta = (AirTem + 237.3) * (AirTem + 237.3);
		double exp_Delta = (17.27 * AirTem) / (AirTem + 237.3);
		double num_Delta = 4098 * (0.6108 * Math.exp(exp_Delta));
		double Delta = num_Delta / den_Delta;
		//double lambda = 2.45*pow(10,6);//.501 - 0.002361 * AirTem;
		double gamma = 1013 * AtmPres / (0.622 * lambda);
		result = (alpha) * Delta * (NetRad) / ((gamma + Delta) * lambda);
	}
	return result;
	//System.out.println(result);
}
}

