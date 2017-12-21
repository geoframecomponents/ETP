package etpTest;
import java.net.URISyntaxException;
import java.util.HashMap;
import org.jgrasstools.gears.io.timedependent.OmsTimeSeriesIteratorReader;
import org.jgrasstools.gears.io.timedependent.OmsTimeSeriesIteratorWriter;
import org.jgrasstools.gears.libs.monitor.PrintStreamProgressMonitor;
import etp.OmsPenmanMonteithETDaily;
import org.junit.*;
/**
 * Test Pm daily evapotranspiration.
 *
 * @author Michele Bottazzi (www.michele.bottazzi@gmail.com)
 */
//@SuppressWarnings("nls")
public class TestPenmanMonteithDaily {

    @Test
    public void Test() throws Exception {
        // PrintStreamProgressMonitor pm = new PrintStreamProgressMonitor(System.out, System.err);
        // URL rainUrl = this.getClass().getClassLoader().getResource("etp_in_data_rain.csv");
    	String startDate	= "2016-06-01 00:00";
        String endDate		= "2016-06-10 00:00";
        int timeStepMinutes = 60*24;
        String fId 			= "ID";
        PrintStreamProgressMonitor pm = new PrintStreamProgressMonitor(System.out, System.out);
       
        String inPathToTmax 			="resources/Input/Pm/PmAirTemperature.csv";
        String inPathToTmin				="resources/Input/Pm/PmAirTemperature.csv";
        String inPathToWind 			="resources/Input/Pm/PmWind.csv";
        String inPathToRelativeHumidity ="resources/Input/Pm/PmRHumidity.csv";
        String inPathToNetRad 			="resources/Input/Pm/PmSWrad.csv";
        String inPathToPressure 		="resources/Input/Pm/PmPressure.csv";
        String inPathToSoilFlux 		="resources/Input/Pm/PmSoilFlux.csv";
        String inPathToZC 				="resources/Input/Pm/PmZCentroid.csv";
        String pathOut 					="resources/Output/ET_penman.csv";
        
        OmsTimeSeriesIteratorReader maxtempReader = getTimeseriesReader(inPathToTmax, fId, startDate, endDate, timeStepMinutes);
        OmsTimeSeriesIteratorReader mintempReader = getTimeseriesReader(inPathToTmin, fId, startDate, endDate, timeStepMinutes);
        OmsTimeSeriesIteratorReader windReader 	  = getTimeseriesReader(inPathToWind, fId, startDate, endDate, timeStepMinutes);
        OmsTimeSeriesIteratorReader humReader 	  = getTimeseriesReader(inPathToRelativeHumidity, fId, startDate, endDate, timeStepMinutes);
        OmsTimeSeriesIteratorReader netradReader  = getTimeseriesReader(inPathToNetRad, fId, startDate, endDate, timeStepMinutes);
        OmsTimeSeriesIteratorReader pressureReader= getTimeseriesReader(inPathToPressure, fId, startDate, endDate, timeStepMinutes);
        OmsTimeSeriesIteratorReader soilfluxReader= getTimeseriesReader(inPathToSoilFlux, fId, startDate, endDate, timeStepMinutes);
        OmsTimeSeriesIteratorReader zcentroidReader= getTimeseriesReader(inPathToZC, fId, startDate, endDate, timeStepMinutes);
        
        OmsTimeSeriesIteratorWriter writerETP = new OmsTimeSeriesIteratorWriter();
		writerETP.file = pathOut;
		writerETP.tStart = startDate;
		writerETP.tTimestep = timeStepMinutes;
		writerETP.fileNovalue="-9999.0";

        OmsPenmanMonteithETDaily PMEtpDaily = new OmsPenmanMonteithETDaily();

        while( maxtempReader.doProcess ) {
            maxtempReader.nextRecord();
            HashMap<Integer, double[]> id2ValueMap = maxtempReader.outData;
            PMEtpDaily.inMaxTemp = id2ValueMap;
            mintempReader.nextRecord();
            id2ValueMap = mintempReader.outData;
            PMEtpDaily.inMinTemp = id2ValueMap;

            windReader.nextRecord();
            id2ValueMap = windReader.outData;
            PMEtpDaily.inWind = id2ValueMap;

            //PMEtpDaily.defaultPressure = 101.3;

            humReader.nextRecord();
            id2ValueMap = humReader.outData;
            PMEtpDaily.inRelativeHumidity = id2ValueMap;

            netradReader.nextRecord();
            id2ValueMap = netradReader.outData;
            PMEtpDaily.inNetradiation = id2ValueMap;
            
            pressureReader.nextRecord();
            id2ValueMap = pressureReader.outData;
            PMEtpDaily.inPressure = id2ValueMap;
            
            soilfluxReader.nextRecord();
            id2ValueMap = soilfluxReader.outData;
            PMEtpDaily.inSoilFlux = id2ValueMap;
            
            zcentroidReader.nextRecord();
            id2ValueMap = zcentroidReader.outData;
            PMEtpDaily.inZCentroid = id2ValueMap;
            //HashMap<Integer, double[]> outEtp = PMEtpDaily.outPMEtp;

            PMEtpDaily.pm = pm;
            PMEtpDaily.process();
        }
        maxtempReader.close();
        mintempReader.close();
        windReader.close();
        humReader.close();
        netradReader.close();
        pressureReader.close();
        soilfluxReader.close();
        zcentroidReader.close();
    }
    private OmsTimeSeriesIteratorReader getTimeseriesReader( String path, String id, String startDate, String endDate,                                                             int timeStepMinutes ) throws URISyntaxException {
        OmsTimeSeriesIteratorReader reader = new OmsTimeSeriesIteratorReader();
        reader.file = path;
        reader.idfield = "ID";
        reader.tStart = startDate;
        reader.tTimestep = 1440;
        reader.tEnd = endDate;
        reader.fileNovalue = "-9999.0";
        reader.initProcess();
        return reader;
    }

}
