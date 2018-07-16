package etpTestPointCase;




import java.net.URISyntaxException;

import java.util.HashMap;


import org.jgrasstools.gears.io.timedependent.OmsTimeSeriesIteratorReader;

import org.jgrasstools.gears.libs.monitor.PrintStreamProgressMonitor;
import org.junit.*;
import etpPointCase.OmsFaoEtpHourly;
/**
 * Test FAO Hourly evapotranspiration.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
//@SuppressWarnings("nls")
public class TestFaoEtpHourly{
	
	@Test
    public void Test() throws Exception {

        // PrintStreamProgressMonitor pm = new PrintStreamProgressMonitor(System.out, System.err);

        // URL rainUrl = this.getClass().getClassLoader().getResource("etp_in_data_rain.csv");
		String startDate= "2016-01-15 00:00";
        String endDate	= "2016-12-16 00:00";
        int timeStepMinutes = 60*24;
        String fId = "ID";

        PrintStreamProgressMonitor pm = new PrintStreamProgressMonitor(System.out, System.out);

        String inPathToTemperature 		="resources/Input/dataET_point/AirTemperature.csv";
        String inPathToWind 			="resources/Input/dataET_point/WindVelocity.csv";
        String inPathToRelativeHumidity ="resources/Input/dataET_point/RelativeHumidity.csv";
        String inPathToNetRad 			="resources/Input/dataET_point/NetRadiation.csv";
        

        OmsTimeSeriesIteratorReader tempReader = getTimeseriesReader(inPathToTemperature, fId, startDate, endDate, timeStepMinutes);
        OmsTimeSeriesIteratorReader windReader = getTimeseriesReader(inPathToWind, fId, startDate, endDate, timeStepMinutes);
        OmsTimeSeriesIteratorReader humReader = getTimeseriesReader(inPathToRelativeHumidity, fId, startDate, endDate, timeStepMinutes);
        OmsTimeSeriesIteratorReader netradReader = getTimeseriesReader(inPathToNetRad, fId, startDate, endDate,
                timeStepMinutes);

        OmsFaoEtpHourly faoEtpHourly = new OmsFaoEtpHourly();

        while( tempReader.doProcess ) {
            tempReader.nextRecord();

            tempReader.nextRecord();
            HashMap<Integer, double[]> id2ValueMap = tempReader.outData;
            faoEtpHourly.inTemp = id2ValueMap;

            windReader.nextRecord();
            id2ValueMap = windReader.outData;
            faoEtpHourly.inWind = id2ValueMap;

            faoEtpHourly.defaultPressure = 101.3;

            humReader.nextRecord();
            id2ValueMap = humReader.outData;
            faoEtpHourly.inRh = id2ValueMap;

            netradReader.nextRecord();
            id2ValueMap = netradReader.outData;
            faoEtpHourly.inNetradiation = id2ValueMap;

            faoEtpHourly.pm = pm;
            faoEtpHourly.process();

        }

        tempReader.close();
        windReader.close();
        humReader.close();
        netradReader.close();

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
