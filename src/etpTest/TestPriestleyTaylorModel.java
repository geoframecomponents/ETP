package etpTest;

import java.net.URISyntaxException;
import java.util.HashMap;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.jgrasstools.gears.io.shapefile.OmsShapefileFeatureReader;
import org.jgrasstools.gears.io.timedependent.OmsTimeSeriesIteratorReader;
import org.jgrasstools.gears.io.timedependent.OmsTimeSeriesIteratorWriter;
import org.jgrasstools.gears.libs.monitor.PrintStreamProgressMonitor;

import etp.OmsPriestleyTaylorEtpModel;

import org.jgrasstools.hortonmachine.utils.HMTestCase;

/**
 * Test PrestleyTaylorModel.
 * 
 */
@SuppressWarnings("nls")
public class TestPriestleyTaylorModel extends HMTestCase {

    public void testFaoEtpDaily() throws Exception {

        String startDate = "2007-10-17 00:00";
        String endDate = "2007-10-18 00:00";
        int timeStepMinutes = 60;
        String fId = "ID";

        PrintStreamProgressMonitor pm = new PrintStreamProgressMonitor(System.out, System.out);

        String inPathToNetRad ="resources/Input/NetRad.csv";
		String inPathToTemperature ="resources/Input/Taria.csv";
		String pathToETP= "resources/Output/etp_PrestleyTaylor.csv";

        OmsTimeSeriesIteratorReader tempReader = getTimeseriesReader(inPathToTemperature, fId, startDate, endDate, timeStepMinutes);
        OmsTimeSeriesIteratorReader netradReader = getTimeseriesReader(inPathToNetRad, fId, startDate, endDate, timeStepMinutes);
        
        OmsTimeSeriesIteratorWriter writerETP = new OmsTimeSeriesIteratorWriter();
        
		OmsShapefileFeatureReader stationsReader = new OmsShapefileFeatureReader();
		stationsReader.file = "resources/Input/stations.shp";
		stationsReader.readFeatureCollection();
		SimpleFeatureCollection stationsFC = stationsReader.geodata;

	
		writerETP.file = pathToETP;
		writerETP.tStart = startDate;
		writerETP.tTimestep = timeStepMinutes;
		writerETP.fileNovalue="-9999";


        OmsPriestleyTaylorEtpModel PTEtp = new OmsPriestleyTaylorEtpModel();
        PTEtp.inStations = stationsFC;
        PTEtp.fStationsid = "cat" ;

        while( tempReader.doProcess ) {
            tempReader.nextRecord();

            HashMap<Integer, double[]> id2ValueMap = tempReader.outData;
            PTEtp.inTemp = id2ValueMap;

            PTEtp.tStartDate=startDate;


            PTEtp.defaultPressure = 101.3;

            netradReader.nextRecord();
            id2ValueMap = netradReader.outData;
            PTEtp.inNetradiation = id2ValueMap;

            PTEtp.pAlpha = 1.06;
            PTEtp.pGmorn = 0.35;
            PTEtp.pGnight = 0.75;
            PTEtp.doHourly = true;
            PTEtp.pm = pm;
            PTEtp.process();

            HashMap<Integer, double[]> outEtp = PTEtp.outPTEtp;

			writerETP.inData = outEtp;
			writerETP.writeNextLine();
			
			
			
			if (pathToETP != null) {
				writerETP.close();
			}
        }

        tempReader.close();
        netradReader.close();
        
    }

    private OmsTimeSeriesIteratorReader getTimeseriesReader( String path, String id, String startDate, String endDate,
            int timeStepMinutes ) throws URISyntaxException {
        OmsTimeSeriesIteratorReader reader = new OmsTimeSeriesIteratorReader();
        reader.file = path;
        reader.idfield = "ID";
        reader.tStart = startDate;
        reader.tTimestep = timeStepMinutes;
        reader.tEnd = endDate;
        reader.fileNovalue = "-9999";
        reader.initProcess();
        return reader;
    }

}
