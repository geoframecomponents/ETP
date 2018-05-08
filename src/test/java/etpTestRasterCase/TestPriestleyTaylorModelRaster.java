package etpTestRasterCase;
import org.geotools.coverage.grid.GridCoverage2D;
import org.jgrasstools.gears.io.rasterreader.OmsRasterReader;
import org.jgrasstools.gears.io.rasterwriter.OmsRasterWriter;
import org.junit.Test;
import etpRasterCase.OmsPriestleyTaylorEtRaster;

public class TestPriestleyTaylorModelRaster {
	String startDate = "2016-06-01 00:00";
    String endDate = "2016-08-01 00:00";
    int timeStepMinutes = 60*24;
    String fId 			= "ID";
	GridCoverage2D outETDataGrid = null;
	@Test
	public void Test() throws Exception {
	
		OmsRasterReader airTemperatureReader = new OmsRasterReader();
		airTemperatureReader.file = "resources/Input/dataET_raster/kriging_interpolated_temp_20080722_1500.asc";
		airTemperatureReader.fileNovalue = -9999.0;
		airTemperatureReader.geodataNovalue = Double.NaN;
		airTemperatureReader.process();
		GridCoverage2D airTemperature = airTemperatureReader.outRaster;
		
		OmsRasterReader netRadiationReader = new OmsRasterReader();
		netRadiationReader.file = "resources/Input/dataET_raster/SWRB_raster.asc";
		netRadiationReader.fileNovalue = -9999.0;
		netRadiationReader.geodataNovalue = Double.NaN;
		netRadiationReader.process();
		GridCoverage2D netRadiation = netRadiationReader.outRaster;
		
		OmsPriestleyTaylorEtRaster ETRaster = new OmsPriestleyTaylorEtRaster();

		ETRaster.inAirTemperatureGrid = airTemperature;
		ETRaster.inNetRadiationGrid= netRadiation;
		ETRaster.tStartDate=startDate;

		//ETRaster.startDateTime = tStartDate;

		ETRaster.pAlpha = 1.06;
		ETRaster.pGmorn = 0.35;
		ETRaster.pGnight = 0.75;
		ETRaster.doHourly = true;

		ETRaster.process();
		
		outETDataGrid  = ETRaster.outEtPtGrid;

		OmsRasterWriter writerETtraster = new OmsRasterWriter();
		writerETtraster.inRaster = outETDataGrid;
		writerETtraster.file = "resources/Output/ET_PT.asc";
		writerETtraster.process();

		

	}

}