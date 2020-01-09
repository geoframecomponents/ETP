package etpTestPointCase;




import java.net.URISyntaxException;

import java.util.HashMap;


import org.jgrasstools.gears.io.timedependent.OmsTimeSeriesIteratorReader;
import org.jgrasstools.gears.io.timedependent.OmsTimeSeriesIteratorWriter;
import org.jgrasstools.gears.libs.monitor.PrintStreamProgressMonitor;
import org.junit.*;
import etpPointCase.OmsPenmanMonteithFAO;
/**
 * Test FAO Hourly evapotranspiration.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
//@SuppressWarnings("nls")
public class TestPenmanMonteithFAO{
	
	@Test
    public void Test() throws Exception {
		String startDate= "2012-07-15 10:30";
        String endDate	= "2012-07-15 11:30";
        int timeStepMinutes = 30;
        String fId = "val";

        PrintStreamProgressMonitor pm = new PrintStreamProgressMonitor(System.out, System.out);

        String inPathToTemperature 			="resources/Input/dataET_point/Viote/Viote_Temp.csv";
        String inPathToWind 				="resources/Input/dataET_point/Viote/Viote_Wind.csv";
        String inPathToRelativeHumidity 	="resources/Input/dataET_point/Viote/Viote_RH.csv";
        String inPathToNetRad 				="resources/Input/dataET_point/Viote/Viote_Net.csv";
        String inPathToPressure 			="resources/Input/dataET_point/Viote/Viote_Pres.csv";
        String inPathToSoilHeatFlux 		="resources/Input/dataET_point/Viote/Viote_GHF.csv";
        String inPathToSoilMosture 			="resources/Input/dataET_point/Viote/Viote_null.csv";

        String pathToEvapotranspirationFAO	="resources/Output/evapotranspirationFAO.csv";
        String pathToLatentHeatFAO			="resources/Output/LatentHeatFAO.csv";


        OmsTimeSeriesIteratorReader tempReader 			= getTimeseriesReader(inPathToTemperature, fId, startDate, endDate, timeStepMinutes);
        OmsTimeSeriesIteratorReader windReader 			= getTimeseriesReader(inPathToWind, fId, startDate, endDate, timeStepMinutes);
        OmsTimeSeriesIteratorReader humReader 			= getTimeseriesReader(inPathToRelativeHumidity, fId, startDate, endDate, timeStepMinutes);
        OmsTimeSeriesIteratorReader netradReader 		= getTimeseriesReader(inPathToNetRad, fId, startDate, endDate, timeStepMinutes);
        OmsTimeSeriesIteratorReader pressureReader 		= getTimeseriesReader(inPathToPressure, fId, startDate, endDate,timeStepMinutes);
        OmsTimeSeriesIteratorReader soilMostureReader 	= getTimeseriesReader(inPathToSoilMosture, fId, startDate, endDate,timeStepMinutes);
        OmsTimeSeriesIteratorReader soilHeatFluxReader 	= getTimeseriesReader(inPathToSoilHeatFlux, fId, startDate, endDate,timeStepMinutes);

        OmsTimeSeriesIteratorWriter writerEvapotranspirationFAO = new OmsTimeSeriesIteratorWriter();
        writerEvapotranspirationFAO.file = pathToEvapotranspirationFAO;
        writerEvapotranspirationFAO.tStart = startDate;
        writerEvapotranspirationFAO.tTimestep = timeStepMinutes;
        writerEvapotranspirationFAO.fileNovalue="-9999";

		OmsTimeSeriesIteratorWriter writerLatentHeatFAO = new OmsTimeSeriesIteratorWriter();
		writerLatentHeatFAO.file = pathToLatentHeatFAO;
		writerLatentHeatFAO.tStart = startDate;
		writerLatentHeatFAO.tTimestep = timeStepMinutes;
		writerLatentHeatFAO.fileNovalue="-9999";

        OmsPenmanMonteithFAO PmFAO = new OmsPenmanMonteithFAO();
        
        PmFAO.cropCoefficient = 1.0;
        PmFAO.waterWiltingPoint = 0.15;
        PmFAO.waterFieldCapacity = 0.27; 
        PmFAO.rootsDepth = 0.75;
        PmFAO.depletionFraction = 0.55;
        
        PmFAO.tStartDate=startDate;
        PmFAO.temporalStep = timeStepMinutes;
        PmFAO.defaultAtmosphericPressure = 101.3;
        PmFAO.doHourly = true;
        
        while( tempReader.doProcess ) {
            tempReader.nextRecord();

            HashMap<Integer, double[]> id2ValueMap = tempReader.outData;
            PmFAO.inAirTemperature = id2ValueMap;            

            windReader.nextRecord();
            id2ValueMap = windReader.outData;
            PmFAO.inWindVelocity = id2ValueMap;
            
            humReader.nextRecord();
            id2ValueMap = humReader.outData;
            PmFAO.inRelativeHumidity = id2ValueMap;

            netradReader.nextRecord();
            id2ValueMap = netradReader.outData;
            PmFAO.inNetRadiation = id2ValueMap;
            
            pressureReader.nextRecord();
            id2ValueMap = pressureReader.outData;
            PmFAO.inAtmosphericPressure = id2ValueMap;
            
            soilMostureReader.nextRecord();
            id2ValueMap = soilMostureReader.outData;
            PmFAO.inSoilMosture = id2ValueMap;
            
            soilHeatFluxReader.nextRecord();
            id2ValueMap = soilHeatFluxReader.outData;
            PmFAO.inSoilFlux = id2ValueMap;

            PmFAO.pm = pm;
            PmFAO.process();
            
            OmsTimeSeriesIteratorWriter writerLAtentHeatFAO = new OmsTimeSeriesIteratorWriter();
    		writerLAtentHeatFAO.file = pathToLatentHeatFAO;
    		writerLAtentHeatFAO.tStart = startDate;
    		writerLAtentHeatFAO.tTimestep = timeStepMinutes;
    		writerLAtentHeatFAO.fileNovalue="-9999";
    		
    		HashMap<Integer, double[]> outLatentHeat = PmFAO.outLatentHeatFao;
    		writerLatentHeatFAO.inData = outLatentHeat;
    		writerLatentHeatFAO.writeNextLine();	

            HashMap<Integer, double[]> outEvapotranspiration = PmFAO.outEvapotranspirationFao;
            writerEvapotranspirationFAO.inData = outEvapotranspiration;
            writerEvapotranspirationFAO.writeNextLine();	

        }

        tempReader.close();
        windReader.close();
        humReader.close();
        netradReader.close();
        pressureReader.close();
        soilHeatFluxReader.close();
		writerLatentHeatFAO.close();
        writerEvapotranspirationFAO.close();
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
