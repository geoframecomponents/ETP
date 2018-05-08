package etpTest;
import java.net.URISyntaxException;
import java.util.HashMap;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.jgrasstools.gears.io.shapefile.OmsShapefileFeatureReader;
import org.jgrasstools.gears.io.timedependent.OmsTimeSeriesIteratorReader;
import org.jgrasstools.gears.libs.monitor.PrintStreamProgressMonitor;

//import etp.Leaf;
//import org.jgrasstools.gears.utils.math.NumericsUtilities;
import etpPointCase.OmsSchymanskiOrET;

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
		String startDate = "1994-06-22 05:00";
        String endDate = "1994-06-22 12:00";
        int timeStepMinutes = 60;
        String fId = "ID";

        PrintStreamProgressMonitor pm = new PrintStreamProgressMonitor(System.out, System.out);

        String inPathToTemperature 		="resources/Input/Pm/airT_1.csv";
        String inPathToWind 			="resources/Input/Pm/fake.csv";
        String inPathToRelativeHumidity ="resources/Input/Pm/fake.csv";
        String inPathToSWRad 			="resources/Input/Pm/total_1.csv";
        String inPathToLWRad 			="resources/Input/Pm/fake.csv";
        String inPathToPressure 		="resources/Input/Pm/fake.csv";
        String inPathToLai 				="resources/Input/Pm/LAI_1.csv";
        String inPathToCentroids 		="resources/Input/Pm/punti.shp";
       // String inPathToArea 			="resources/Input/So/SoArea.csv";

        OmsTimeSeriesIteratorReader temperatureReader = getTimeseriesReader(inPathToTemperature, fId, startDate, endDate, timeStepMinutes);
        OmsTimeSeriesIteratorReader windReader = getTimeseriesReader(inPathToWind, fId, startDate, endDate, timeStepMinutes);
        OmsTimeSeriesIteratorReader humidityReader = getTimeseriesReader(inPathToRelativeHumidity, fId, startDate, endDate, timeStepMinutes);
        OmsTimeSeriesIteratorReader shortwaveReader = getTimeseriesReader(inPathToSWRad, fId, startDate, endDate,timeStepMinutes);
        OmsTimeSeriesIteratorReader longwaveReader = getTimeseriesReader(inPathToLWRad, fId, startDate, endDate,timeStepMinutes);
        OmsTimeSeriesIteratorReader pressureReader = getTimeseriesReader(inPathToPressure, fId, startDate, endDate,timeStepMinutes);
        OmsTimeSeriesIteratorReader laiReader = getTimeseriesReader(inPathToLai, fId, startDate, endDate,timeStepMinutes);      
        OmsShapefileFeatureReader stationsReader = new OmsShapefileFeatureReader();
		stationsReader.file = inPathToCentroids;
		stationsReader.readFeatureCollection();
		SimpleFeatureCollection stationsFC = stationsReader.geodata;
		
        OmsSchymanskiOrET SoET = new OmsSchymanskiOrET();
        SoET.inStations = stationsFC;
        SoET.fStationsid="id";
        SoET.fPointZ="quota";
         

        while(temperatureReader.doProcess ) {
        	temperatureReader.nextRecord();

            HashMap<Integer, double[]> id2ValueMap = temperatureReader.outData;
            SoET.inAirTemperature = id2ValueMap;
            SoET.doHourly = true;
            SoET.area = 1.0;	
            
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
            
            laiReader.nextRecord();
            id2ValueMap = laiReader.outData;
            SoET.inLeafAreaIndex = id2ValueMap;
            
            SoET.pm = pm;
            SoET.process();

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
