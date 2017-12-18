package etpTest;
import java.net.URISyntaxException;
import java.util.HashMap;

import org.jgrasstools.gears.io.timedependent.OmsTimeSeriesIteratorReader;
import org.jgrasstools.gears.libs.monitor.PrintStreamProgressMonitor;
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
		String startDate = "2005-05-01 00:00";
        String endDate = "2005-05-02 00:00";
        int timeStepMinutes = 1440;
        String fId = "ID";

        PrintStreamProgressMonitor pm = new PrintStreamProgressMonitor(System.out, System.out);

        String inPathToTemperature ="resources/Input/Temperature.csv";
        String inPathToWind ="resources/Input/Wind.csv";
        String inPathToRelativeHumidity ="resources/Input/RHumidity.csv";
        String inPathToSWRad ="resources/Input/SWrad.csv";
        String inPathToLWRad ="resources/Input/LWrad.csv";
        String inPathToPressure ="resources/Input/Pressure.csv";

        OmsTimeSeriesIteratorReader temperatureReader = getTimeseriesReader(inPathToTemperature, fId, startDate, endDate, timeStepMinutes);
        OmsTimeSeriesIteratorReader windReader = getTimeseriesReader(inPathToWind, fId, startDate, endDate, timeStepMinutes);
        OmsTimeSeriesIteratorReader humidityReader = getTimeseriesReader(inPathToRelativeHumidity, fId, startDate, endDate, timeStepMinutes);
        OmsTimeSeriesIteratorReader shortwaveReader = getTimeseriesReader(inPathToSWRad, fId, startDate, endDate,timeStepMinutes);
        OmsTimeSeriesIteratorReader longwaveReader = getTimeseriesReader(inPathToLWRad, fId, startDate, endDate,timeStepMinutes);
        OmsTimeSeriesIteratorReader pressureReader = getTimeseriesReader(inPathToPressure, fId, startDate, endDate,timeStepMinutes);

        OmsSchymanskiOrET SoET = new OmsSchymanskiOrET();
        while(temperatureReader.doProcess ) {
        	temperatureReader.nextRecord();

            temperatureReader.nextRecord();
            HashMap<Integer, double[]> id2ValueMap = temperatureReader.outData;
            SoET.inAirTemperature = id2ValueMap;

            windReader.nextRecord();
            id2ValueMap = windReader.outData;
            SoET.inWindVelocity = id2ValueMap;

            //SoET.defaultPressure = 101.3;

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
        reader.fileNovalue = "-9999";
        reader.initProcess();
        return reader;
    }

}
