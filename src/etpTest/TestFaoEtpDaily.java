package etpTest;


import java.net.URISyntaxException;

import java.util.HashMap;


import org.jgrasstools.gears.io.timedependent.OmsTimeSeriesIteratorReader;

import org.jgrasstools.gears.libs.monitor.PrintStreamProgressMonitor;
import org.jgrasstools.gears.utils.math.NumericsUtilities;
import etp.OmsFaoEtpDaily;
import org.jgrasstools.hortonmachine.utils.HMTestCase;
/**
 * Test FAO daily evapotranspiration.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
@SuppressWarnings("nls")
public class TestFaoEtpDaily extends HMTestCase {

    public void testFaoEtpDaily() throws Exception {

        // PrintStreamProgressMonitor pm = new PrintStreamProgressMonitor(System.out, System.err);

        // URL rainUrl = this.getClass().getClassLoader().getResource("etp_in_data_rain.csv");

        String startDate = "2005-05-01 00:00";
        String endDate = "2005-05-02 00:00";
        int timeStepMinutes = 1440;
        String fId = "ID";

        PrintStreamProgressMonitor pm = new PrintStreamProgressMonitor(System.out, System.out);

        String inPathToTmax ="resources/Input/faoetpday_in_tmax.csv";
        String inPathToTmin ="resources/Input/faoetpday_in_tmin.csv";
        String inPathToWind ="resources/Input/faoetpday_in_wind.csv";
        String inPathToRelativeHumidity ="resources/Input/faoetpday_in_rh.csv";
        String inPathToNetRad ="resources/Input/faoetpday_in_rad.csv";

        OmsTimeSeriesIteratorReader maxtempReader = getTimeseriesReader(inPathToTmax, fId, startDate, endDate, timeStepMinutes);
        OmsTimeSeriesIteratorReader mintempReader = getTimeseriesReader(inPathToTmin, fId, startDate, endDate, timeStepMinutes);
        OmsTimeSeriesIteratorReader windReader = getTimeseriesReader(inPathToWind, fId, startDate, endDate, timeStepMinutes);
        OmsTimeSeriesIteratorReader humReader = getTimeseriesReader(inPathToRelativeHumidity, fId, startDate, endDate, timeStepMinutes);
        OmsTimeSeriesIteratorReader netradReader = getTimeseriesReader(inPathToNetRad, fId, startDate, endDate,
                timeStepMinutes);

        OmsFaoEtpDaily faoEtpDaily = new OmsFaoEtpDaily();

        while( maxtempReader.doProcess ) {
            maxtempReader.nextRecord();

            maxtempReader.nextRecord();
            HashMap<Integer, double[]> id2ValueMap = maxtempReader.outData;
            faoEtpDaily.inMaxTemp = id2ValueMap;

            mintempReader.nextRecord();
            id2ValueMap = mintempReader.outData;
            faoEtpDaily.inMinTemp = id2ValueMap;

            windReader.nextRecord();
            id2ValueMap = windReader.outData;
            faoEtpDaily.inWind = id2ValueMap;

            faoEtpDaily.defaultPressure = 101.3;

            humReader.nextRecord();
            id2ValueMap = humReader.outData;
            faoEtpDaily.inRh = id2ValueMap;

            netradReader.nextRecord();
            id2ValueMap = netradReader.outData;
            faoEtpDaily.inNetradiation = id2ValueMap;

            faoEtpDaily.pm = pm;
            faoEtpDaily.process();

            HashMap<Integer, double[]> outEtp = faoEtpDaily.outFaoEtp;

            double value = outEtp.get(1221)[0];
            assertTrue(NumericsUtilities.dEq(value, 3.7612114870933824));
            break;
        }

        maxtempReader.close();
        windReader.close();
        humReader.close();
        netradReader.close();

    }

    private OmsTimeSeriesIteratorReader getTimeseriesReader( String path, String id, String startDate, String endDate,
            int timeStepMinutes ) throws URISyntaxException {
        OmsTimeSeriesIteratorReader reader = new OmsTimeSeriesIteratorReader();
        reader.file = path;
        reader.idfield = "ID";
        reader.tStart = "2005-05-01 00:00";
        reader.tTimestep = 1440;
        reader.tEnd = "2005-05-02 00:00";
        reader.fileNovalue = "-9999";
        reader.initProcess();
        return reader;
    }

}
