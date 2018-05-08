package etpTestRasterCase;

//import static org.junit.Assert.*;

import org.geotools.coverage.grid.GridCoverage2D;
import org.jgrasstools.gears.io.rasterreader.OmsRasterReader;
import org.jgrasstools.gears.io.rasterwriter.OmsRasterWriter;
import org.junit.Test;

import etpRasterCase.OmsSchymanskiOrETRaster;

//import swrbRasterCase.ShortwaveRadiationBalanceRasterCase;

public class TestSchymanskiOrETRaster {

	
	GridCoverage2D outETDataGrid = null;
	
	@Test
	public void Test() throws Exception {

		//String startDate = "2007-10-17 15:00" ;

		OmsRasterReader demElevationReader = new OmsRasterReader();
		demElevationReader.file = "resources/Input/dataET_raster/mybasin.asc";
		demElevationReader.fileNovalue = -9999.0;
		demElevationReader.geodataNovalue = Double.NaN;
		demElevationReader.process();
		GridCoverage2D demElevation = demElevationReader.outRaster;
	
		OmsRasterReader airTemperatureReader = new OmsRasterReader();
		airTemperatureReader.file = "resources/Input/dataET_raster/kriging_interpolated_temp_20080722_1500.asc";
		airTemperatureReader.fileNovalue = -9999.0;
		airTemperatureReader.geodataNovalue = Double.NaN;
		airTemperatureReader.process();
		GridCoverage2D airTemperature = airTemperatureReader.outRaster;
		
		OmsRasterReader shortWaveRadiationReader = new OmsRasterReader();
		shortWaveRadiationReader.file = "resources/Input/dataET_raster/SWRB_raster.asc";
		shortWaveRadiationReader.fileNovalue = -9999.0;
		shortWaveRadiationReader.geodataNovalue = Double.NaN;
		shortWaveRadiationReader.process();
		GridCoverage2D shortWaveRadiation = shortWaveRadiationReader.outRaster;
		
		OmsRasterReader longWaveRadiationReader = new OmsRasterReader();
		longWaveRadiationReader.file = "resources/Input/dataET_raster/LwrbDownWellingRaster.asc";
		longWaveRadiationReader.fileNovalue = -9999.0;
		longWaveRadiationReader.geodataNovalue = Double.NaN;
		longWaveRadiationReader.process();
		GridCoverage2D longWaveRadiation = longWaveRadiationReader.outRaster;
		
		OmsRasterReader relativeHumidityReader = new OmsRasterReader();
		relativeHumidityReader.file = "resources/Input/dataET_raster/kriging_interpolated_20080722_1500.asc";
		relativeHumidityReader.fileNovalue = -9999.0;
		relativeHumidityReader.geodataNovalue = Double.NaN;
		relativeHumidityReader.process();
		GridCoverage2D relativeHumidity = relativeHumidityReader.outRaster;
		
		OmsRasterReader windVelocityReader = new OmsRasterReader();
		windVelocityReader.file = "resources/Input/dataET_raster/Wind.asc";
		windVelocityReader.fileNovalue = -9999.0;
		windVelocityReader.geodataNovalue = Double.NaN;
		windVelocityReader.process();
		GridCoverage2D windVelocity = windVelocityReader.outRaster;
		
		OmsRasterReader atmosphericPressureReader = new OmsRasterReader();
		atmosphericPressureReader.file = "resources/Input/dataET_raster/Pressure.asc";
		atmosphericPressureReader.fileNovalue = -9999.0;
		atmosphericPressureReader.geodataNovalue = Double.NaN;
		atmosphericPressureReader.process();
		GridCoverage2D atmosphericPressure = atmosphericPressureReader.outRaster;
		
		OmsRasterReader leafAreaIndexReader = new OmsRasterReader();
		leafAreaIndexReader.file = "resources/Input/dataET_raster/leaf2100.tif";
		leafAreaIndexReader.fileNovalue = -9999.0;
		leafAreaIndexReader.geodataNovalue = Double.NaN;
		leafAreaIndexReader.process();
		GridCoverage2D leafAreaIndex = leafAreaIndexReader.outRaster;
		
		OmsSchymanskiOrETRaster ETRaster = new OmsSchymanskiOrETRaster();

		ETRaster.inAirTemperatureGrid = airTemperature;
		ETRaster.inDemElevationGrid = demElevation;
		ETRaster.inShortWaveRadiationGrid= shortWaveRadiation;
		ETRaster.inLongWaveRadiationGrid = longWaveRadiation;
		ETRaster.inRelativeHumidityGrid = relativeHumidity;
		ETRaster.inWindVelocityGrid = windVelocity;
		ETRaster.inAtmosphericPressureGrid = atmosphericPressure;
		ETRaster.inLeafAreaIndexGrid = leafAreaIndex;
		ETRaster.doHourly=true;
			
		ETRaster.area = 1.0;	
		
		ETRaster.process();
		
		outETDataGrid  = ETRaster.outETGrid;

		OmsRasterWriter writerETtraster = new OmsRasterWriter();
		writerETtraster.inRaster = outETDataGrid;
		writerETtraster.file = "resources/Output/ETP_SO.asc";
		writerETtraster.process();
	}
}
