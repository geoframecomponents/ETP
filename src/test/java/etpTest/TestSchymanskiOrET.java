package etpTest;
import java.net.URISyntaxException;
import java.util.HashMap;

import org.jgrasstools.gears.io.timedependent.OmsTimeSeriesIteratorReader;
import org.jgrasstools.gears.libs.monitor.PrintStreamProgressMonitor;

//import etp.Leaf;
//import org.jgrasstools.gears.utils.math.NumericsUtilities;
import etp.OmsSchymanskiOrET;
import org.junit.*;

//import static org.junit.Assert.assertTrue;
/**
 * Test Schymanski & Or evapotranspiration.
 * @author Michele Bottazzi (michele.bottazzi@gmail.com)
 */
//@SuppressWarnings("nls")
public class TestSchymanskiOrET{
	@Test
    public void Test() throws Exception {
		String startDate = "2016-06-01 00:00";
        String endDate = "2016-06-10 00:00";
        int timeStepMinutes = 1440;
        String fId = "ID";

        PrintStreamProgressMonitor pm = new PrintStreamProgressMonitor(System.out, System.out);

        String inPathToTemperature 		="resources/Input/So/SoAirTemperature.csv";
        String inPathToWind 			="resources/Input/So/SoWind.csv";
        String inPathToRelativeHumidity ="resources/Input/So/SoRHumidity.csv";
        String inPathToSWRad 			="resources/Input/So/SoSWrad.csv";
        String inPathToLWRad 			="resources/Input/So/SoLWrad.csv";
        String inPathToPressure 		="resources/Input/So/SoPressure.csv";

        OmsTimeSeriesIteratorReader temperatureReader = getTimeseriesReader(inPathToTemperature, fId, startDate, endDate, timeStepMinutes);
        OmsTimeSeriesIteratorReader windReader = getTimeseriesReader(inPathToWind, fId, startDate, endDate, timeStepMinutes);
        OmsTimeSeriesIteratorReader humidityReader = getTimeseriesReader(inPathToRelativeHumidity, fId, startDate, endDate, timeStepMinutes);
        OmsTimeSeriesIteratorReader shortwaveReader = getTimeseriesReader(inPathToSWRad, fId, startDate, endDate,timeStepMinutes);
        OmsTimeSeriesIteratorReader longwaveReader = getTimeseriesReader(inPathToLWRad, fId, startDate, endDate,timeStepMinutes);
        OmsTimeSeriesIteratorReader pressureReader = getTimeseriesReader(inPathToPressure, fId, startDate, endDate,timeStepMinutes);

        OmsSchymanskiOrET SoET = new OmsSchymanskiOrET();
        while(temperatureReader.doProcess ) {
        	temperatureReader.nextRecord();

            HashMap<Integer, double[]> id2ValueMap = temperatureReader.outData;
            SoET.inAirTemperature = id2ValueMap;
            
            SoET.leafLength = 0.05;	
            SoET.leafSide = 2;	
            SoET.leafEmissivity = 1.0;	
            //SoET.leafTemperature = SoET.inAirTemperature + 2.0;
			//double leafTemperature = leaf.temperature;   
			

            windReader.nextRecord();
            id2ValueMap = windReader.outData;
            SoET.inWindVelocity = id2ValueMap;

            humidityReader.nextRecord();
            id2ValueMap = humidityReader.outData;
            SoET.inRelativeHumidity = id2ValueMap;

            shortwaveReader.nextRecord();
            id2ValueMap = shortwaveReader.outData;
            SoET.inShortWaveRadiation = id2ValueMap;
            
            longwaveReader.nextRecord();
            id2ValueMap = longwaveReader.outData;
            SoET.inLongWaveRadiation = id2ValueMap;
            
            pressureReader.nextRecord();
            id2ValueMap = pressureReader.outData;
            SoET.inAtmosphericPressure = id2ValueMap;
                      
            SoET.pm = pm;
            SoET.process();

//            HashMap<Integer, double[]> outEt = SoET.outSOEt;
//
//            double value = outEt.get(1221)[0];
//            assertTrue(NumericsUtilities.dEq(value, 2.8583603700962774));
//            break;
        }
        temperatureReader.close();
        windReader.close();
        humidityReader.close();
        shortwaveReader.close();
        longwaveReader.close();
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
