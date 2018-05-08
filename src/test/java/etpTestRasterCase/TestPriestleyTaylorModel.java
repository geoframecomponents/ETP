package etpTestRasterCase;


import org.geotools.coverage.grid.GridCoverage2D;
import org.jgrasstools.gears.io.rasterreader.OmsRasterReader;
import org.jgrasstools.gears.io.rasterwriter.OmsRasterWriter;
import org.junit.*;

import etpRasterCase.OmsPriestleyTaylorEtpModel;

/**
 * Test PrestleyTaylorModel.
 * 
 */
@SuppressWarnings("nls")
public class TestPriestleyTaylorModel{
	
	GridCoverage2D etpDataGrid = null;

	@Test
    public void Test() throws Exception {
		
		String startDate = "2007-10-17 15:00" ;

		OmsRasterReader airTReader = new OmsRasterReader();
		airTReader.file = "resources/Input/dataET_raster/kriging_interpolated_temp_20080722_1500.asc";
		airTReader.fileNovalue = -9999.0;
		airTReader.geodataNovalue = Double.NaN;
		airTReader.process();
		GridCoverage2D airT = airTReader.outRaster;


		OmsRasterReader netReader = new OmsRasterReader();
		netReader.file = "resources/Input/dataET_raster/LwrbDownWellingRaster.asc";
		netReader.fileNovalue = -9999.0;
		netReader.geodataNovalue = Double.NaN;
		netReader.process();
		GridCoverage2D netRad = netReader.outRaster;

        OmsPriestleyTaylorEtpModel PTEtp = new OmsPriestleyTaylorEtpModel();


            PTEtp.tStartDate=startDate;


            PTEtp.defaultPressure = 101.3;



            PTEtp.pAlpha = 1.06;
            PTEtp.pGmorn = 0.35;
            PTEtp.pGnight = 0.75;
            PTEtp.doHourly = true;
            
            PTEtp.inNetradiationGrid =  netRad;
            PTEtp.inTemperatureGrid= airT;
    		
    		
            PTEtp.process();
            
            
            etpDataGrid  =  PTEtp.outETpDataGrid;



    		OmsRasterWriter writerNetraster = new OmsRasterWriter();
    		writerNetraster .inRaster = etpDataGrid;
    		writerNetraster .file = "resources/Output/etp.asc";
    		writerNetraster.process();


        
    }



}
