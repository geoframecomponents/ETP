package etpTest;

//import static org.junit.Assert.*;
import etp.OmsBulkResistance;
import java.net.URISyntaxException;
import java.util.HashMap;

import org.jgrasstools.gears.io.timedependent.OmsTimeSeriesIteratorReader;
import org.jgrasstools.gears.io.timedependent.OmsTimeSeriesIteratorWriter;
import org.jgrasstools.gears.libs.monitor.PrintStreamProgressMonitor;
import org.junit.*;

//import etp.OmsPenmanMonteithETDaily;
/**
 * Test.
 *
 * @author Michele Bottazzi (www.michele.bottazzi@gmail.com)
 */
public class TestBulkResistance {

	@Test
	public void test() throws Exception {
		//fail("Not yet implemented");
		String startDate 			  	= "2005-05-01 00:00";
        String endDate 					= "2005-05-12 00:00";
        int timeStepMinutes 			= 60*24;
        String fId 						= "ID";
        PrintStreamProgressMonitor pm 	= new PrintStreamProgressMonitor(System.out, System.out);
        String inPathToStomatal 		="resources/Input/etp/rs.csv";
        String inPathToAreaIndex 		="resources/Input/etp/lai.csv";
        String inPathToConstant 		="resources/Input/etp/constant.csv";//"resources/Input/faoetpday_in_wind.csv";
        String pathOut 					="resources/Output/Bulk.csv";
        
        OmsTimeSeriesIteratorReader rsReader  	= getTimeseriesReader(inPathToStomatal, fId, startDate, endDate, timeStepMinutes);
        OmsTimeSeriesIteratorReader laiReader 	= getTimeseriesReader(inPathToAreaIndex, fId, startDate, endDate, timeStepMinutes);
        OmsTimeSeriesIteratorReader cReader 	= getTimeseriesReader(inPathToConstant, fId, startDate, endDate, timeStepMinutes);
        
        OmsTimeSeriesIteratorWriter writerETP 	= new OmsTimeSeriesIteratorWriter();
		writerETP.file 							= pathOut;
		writerETP.tStart 						= startDate;
		writerETP.tTimestep 					= timeStepMinutes;
		writerETP.fileNovalue					="-9999";

        OmsBulkResistance bulkResistanceDaily = new OmsBulkResistance();

        while(rsReader.doProcess) {
        	rsReader.nextRecord();
            HashMap<Integer, double[]> id2ValueMap = rsReader.outData;
            bulkResistanceDaily.inLeafStomatalResistance = id2ValueMap;

            laiReader.nextRecord();
            id2ValueMap = laiReader.outData;
            bulkResistanceDaily.inLeafAreaIndex = id2ValueMap;

            cReader.nextRecord();
            id2ValueMap = cReader.outData;
            bulkResistanceDaily.inActiveLeafConstant = id2ValueMap;

            bulkResistanceDaily.pm = pm;
            bulkResistanceDaily.process();
        }
        rsReader.close();
        laiReader.close();
        cReader.close();
    }
    private OmsTimeSeriesIteratorReader getTimeseriesReader( String path, String id, String startDate, String endDate,                                                             int timeStepMinutes ) throws URISyntaxException {
        OmsTimeSeriesIteratorReader reader = new OmsTimeSeriesIteratorReader();
        reader.file 		= path;
        reader.idfield 		= "ID";
        reader.tStart 		= startDate;
        reader.tTimestep 	= 1440;
        reader.tEnd 		= endDate;
        reader.fileNovalue 	= "-9999";
        reader.initProcess();
        return reader;
    }

}