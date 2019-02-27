
package etpTestPointCase;

import java.net.URISyntaxException;
import java.util.HashMap;
import org.jgrasstools.gears.io.timedependent.OmsTimeSeriesIteratorReader;
import org.jgrasstools.gears.io.timedependent.OmsTimeSeriesIteratorWriter;
import org.jgrasstools.gears.libs.monitor.PrintStreamProgressMonitor;

import org.junit.*;

import etpPointCase.OmsPriestleyTaylor;

/**
 * Test PrestleyTaylorModel.
 * 
 */
//@SuppressWarnings("nls")
public class TestPriestleyTaylor{
	@Test
    public void Test() throws Exception {
		String startDate= "2015-07-21 00:00";
        String endDate	= "2015-07-21 23:00";
        int timeStepMinutes = 60;
        String fId = "val";
        
        PrintStreamProgressMonitor pm = new PrintStreamProgressMonitor(System.out, System.out);
        String inPathToNetRad 		="resources/Input/dataET_point/NetRadiation.csv";
		String inPathToTemperature 	="resources/Input/dataET_point/AirTemperature.csv";
		String inPathToPressure		="resources/Input/dataET_point/AtmosphericPressure.csv";
        String inPathToSoilHeatFlux ="resources/Input/dataET_point/SoilHeatFlux.csv";

		String pathToLatentHeatPT	="resources/Output/latentHeatPt.csv";
		String pathToEvapotranspirationPT			="resources/Output/etp_PrestleyTaylor.csv";
        OmsTimeSeriesIteratorReader tempReader = getTimeseriesReader(inPathToTemperature, fId, startDate, endDate, timeStepMinutes);
        OmsTimeSeriesIteratorReader netradReader = getTimeseriesReader(inPathToNetRad, fId, startDate, endDate, timeStepMinutes);
        OmsTimeSeriesIteratorReader pressureReader = getTimeseriesReader(inPathToPressure, fId, startDate, endDate, timeStepMinutes);      
        OmsTimeSeriesIteratorReader soilHeatFluxReader 	= getTimeseriesReader(inPathToSoilHeatFlux, fId, startDate, endDate,timeStepMinutes);

        OmsTimeSeriesIteratorWriter writerLatentHeatPT = new OmsTimeSeriesIteratorWriter();
        writerLatentHeatPT.file = pathToLatentHeatPT;
        writerLatentHeatPT.tStart = startDate;
        writerLatentHeatPT.tTimestep = timeStepMinutes;
        writerLatentHeatPT.fileNovalue="-9999";
		
        OmsTimeSeriesIteratorWriter writerEvapotranspirationPT = new OmsTimeSeriesIteratorWriter();
        writerEvapotranspirationPT.file = pathToEvapotranspirationPT;
        writerEvapotranspirationPT.tStart = startDate;
        writerEvapotranspirationPT.tTimestep = timeStepMinutes;
        writerEvapotranspirationPT.fileNovalue="-9999";
		OmsPriestleyTaylor PtEt = new OmsPriestleyTaylor();
		
		PtEt.alpha = 1.26;
        PtEt.soilFluxParameterDay = 0.35;
        PtEt.soilFluxParameterNight = 0.75;
        PtEt.doHourly = true;
        PtEt.temporalStep = timeStepMinutes;
        PtEt.defaultAtmosphericPressure = 101.3;

        while(tempReader.doProcess ) {
            
        	tempReader.nextRecord();
            HashMap<Integer, double[]> id2ValueMap = tempReader.outData;
            PtEt.inAirTemperature = id2ValueMap;
            PtEt.tStartDate=startDate;
            
            netradReader.nextRecord();
            id2ValueMap = netradReader.outData;
            PtEt.inNetRadiation = id2ValueMap;

            pressureReader.nextRecord();
            id2ValueMap = pressureReader.outData;
            PtEt.inAtmosphericPressure = id2ValueMap;
                      
            soilHeatFluxReader.nextRecord();
            id2ValueMap = soilHeatFluxReader.outData;
            PtEt.inSoilFlux = id2ValueMap;
            
            PtEt.pm = pm;
            PtEt.process();
            HashMap<Integer, double[]> outLatentHeat = PtEt.outLatentHeatPt;
            writerLatentHeatPT.inData = outLatentHeat;
            writerLatentHeatPT.writeNextLine();	
            
            if (pathToLatentHeatPT != null) {
            	writerLatentHeatPT.close();
			}
            
            HashMap<Integer, double[]> outEvapotranspiration= PtEt.outEvapotranspirationPt;
            writerEvapotranspirationPT.inData = outEvapotranspiration;
            writerEvapotranspirationPT.writeNextLine();	
			
			if (pathToEvapotranspirationPT != null) {
				writerEvapotranspirationPT.close();
			}
        }
        tempReader.close();
        netradReader.close();    
        soilHeatFluxReader.close();
        pressureReader.close();


    }
	private OmsTimeSeriesIteratorReader getTimeseriesReader( String path, String id, String startDate, String endDate,
            int timeStepMinutes ) throws URISyntaxException {
        OmsTimeSeriesIteratorReader reader = new OmsTimeSeriesIteratorReader();
        reader.file = path;
        reader.idfield = id;
        reader.tStart =startDate;
        reader.tTimestep = timeStepMinutes;
        reader.tEnd = endDate;
        reader.fileNovalue = "-9999.0";
        reader.initProcess();
        return reader;
    }
}
