package etpTest;
import java.net.URISyntaxException;
import java.util.HashMap;
import org.jgrasstools.gears.io.timedependent.OmsTimeSeriesIteratorReader;
import org.jgrasstools.gears.io.timedependent.OmsTimeSeriesIteratorWriter;
import org.jgrasstools.gears.libs.monitor.PrintStreamProgressMonitor;
import etpPointCase.OmsPenmanMonteithETDaily;
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
    	String startDate	= "1994-01-01 11:00";
        String endDate		= "1994-01-01 23:00";
        int timeStepMinutes = 60;
        String fId 			= "ID";
        PrintStreamProgressMonitor pm = new PrintStreamProgressMonitor(System.out, System.out);
       
        String inPathToTmax 			="resources/Input/Pm/airT_1.csv";
        String inPathToTmin				="resources/Input/Pm/airT_1.csv";
        String inPathToWind 			="resources/Input/Pm/fake.csv";
        String inPathToRelativeHumidity ="resources/Input/Pm/fake.csv";
        String inPathToNetRad 			="resources/Input/Pm/netRad_1.csv";
        String inPathToPressure 		="resources/Input/Pm/fake.csv";
        String inPathToSoilFlux 		="resources/Input/Pm/fake.csv";
        String pathOut 					="resources/Output/ET_penman.csv";
        
        OmsTimeSeriesIteratorReader maxtempReader = getTimeseriesReader(inPathToTmax, fId, startDate, endDate, timeStepMinutes);
        OmsTimeSeriesIteratorReader mintempReader = getTimeseriesReader(inPathToTmin, fId, startDate, endDate, timeStepMinutes);
        OmsTimeSeriesIteratorReader windReader 	  = getTimeseriesReader(inPathToWind, fId, startDate, endDate, timeStepMinutes);
        OmsTimeSeriesIteratorReader humReader 	  = getTimeseriesReader(inPathToRelativeHumidity, fId, startDate, endDate, timeStepMinutes);
        OmsTimeSeriesIteratorReader netradReader  = getTimeseriesReader(inPathToNetRad, fId, startDate, endDate, timeStepMinutes);
        OmsTimeSeriesIteratorReader pressureReader= getTimeseriesReader(inPathToPressure, fId, startDate, endDate, timeStepMinutes);
        OmsTimeSeriesIteratorReader soilfluxReader= getTimeseriesReader(inPathToSoilFlux, fId, startDate, endDate, timeStepMinutes);
        
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
    }
    private OmsTimeSeriesIteratorReader getTimeseriesReader( String path, String id, String startDate, String endDate,                                                             int timeStepMinutes ) throws URISyntaxException {
        OmsTimeSeriesIteratorReader reader = new OmsTimeSeriesIteratorReader();
        reader.file = path;
        reader.idfield = "ID";
        reader.tStart = startDate;
        reader.tTimestep = 60;
        reader.tEnd = endDate;
        reader.fileNovalue = "-9999.0";
        reader.initProcess();
        return reader;
    }

}
