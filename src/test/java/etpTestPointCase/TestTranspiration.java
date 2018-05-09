package etpTestPointCase;
import java.net.URISyntaxException;
import java.util.HashMap;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.jgrasstools.gears.io.shapefile.OmsShapefileFeatureReader;
import org.jgrasstools.gears.io.timedependent.OmsTimeSeriesIteratorReader;
import org.jgrasstools.gears.libs.monitor.PrintStreamProgressMonitor;

//import etp.Leaf;
//import org.jgrasstools.gears.utils.math.NumericsUtilities;
import etpPointCase.OmsTranspiration;

import org.junit.*;

//import static org.junit.Assert.assertTrue;
/**
 * Test Schymanski & Or evapotranspiration.
 * @author Michele Bottazzi (michele.bottazzi@gmail.com)
 */
//@SuppressWarnings("nls")
public class TestTranspiration{
	@Test
    public void Test() throws Exception {
		String startDate= "2016-06-01 00:00";
        String endDate	= "2016-08-01 00:00";
        int timeStepMinutes = 60*24;
        String fId = "ID";

        PrintStreamProgressMonitor pm = new PrintStreamProgressMonitor(System.out, System.out);

        String inPathToTemperature 		="resources/Input/dataET_point/AirTemperature.csv";
        String inPathToWind 			="resources/Input/dataET_point/WindVelocity.csv";
        String inPathToRelativeHumidity ="resources/Input/dataET_point/RelativeHumidity.csv";
        String inPathToSWRad 			="resources/Input/dataET_point/ShortWaveRadiation.csv";
        String inPathToLWRad 			="resources/Input/dataET_point/LongWaveRadiation.csv";
        String inPathToPressure 		="resources/Input/dataET_point/AtmosphericPressure.csv";
        String inPathToLai 				="resources/Input/dataET_point/LeafAreaIndex.csv";
        String inPathToCentroids 		="resources/Input/dataET_point/Centroid.shp";

        OmsTimeSeriesIteratorReader temperatureReader	= getTimeseriesReader(inPathToTemperature, fId, startDate, endDate, timeStepMinutes);
        OmsTimeSeriesIteratorReader windReader 		 	= getTimeseriesReader(inPathToWind, fId, startDate, endDate, timeStepMinutes);
        OmsTimeSeriesIteratorReader humidityReader 		= getTimeseriesReader(inPathToRelativeHumidity, fId, startDate, endDate, timeStepMinutes);
        OmsTimeSeriesIteratorReader shortwaveReader 	= getTimeseriesReader(inPathToSWRad, fId, startDate, endDate,timeStepMinutes);
        OmsTimeSeriesIteratorReader longwaveReader 		= getTimeseriesReader(inPathToLWRad, fId, startDate, endDate,timeStepMinutes);
        OmsTimeSeriesIteratorReader pressureReader 		= getTimeseriesReader(inPathToPressure, fId, startDate, endDate,timeStepMinutes);
        OmsTimeSeriesIteratorReader leafAreaIndexReader	= getTimeseriesReader(inPathToLai, fId, startDate, endDate,timeStepMinutes);      
        OmsShapefileFeatureReader centroidsReader 		= new OmsShapefileFeatureReader();
		centroidsReader.file = inPathToCentroids;
		centroidsReader.readFeatureCollection();
		SimpleFeatureCollection stationsFC = centroidsReader.geodata;
		
		OmsTranspiration Transpiration = new OmsTranspiration();
		Transpiration.inStations = stationsFC;
		Transpiration.fStationsid="id";
		Transpiration.fPointZ="quota";
         

        while(temperatureReader.doProcess ) {
        	temperatureReader.nextRecord();

            HashMap<Integer, double[]> id2ValueMap = temperatureReader.outData;
            Transpiration.inAirTemperature = id2ValueMap;
            Transpiration.doHourly = true;
            Transpiration.area = 1.0;	
            
           
            windReader.nextRecord();
            id2ValueMap = windReader.outData;
            Transpiration.inWindVelocity = id2ValueMap;

            humidityReader.nextRecord();
            id2ValueMap = humidityReader.outData;
            Transpiration.inRelativeHumidity = id2ValueMap;

            shortwaveReader.nextRecord();
            id2ValueMap = shortwaveReader.outData;
            Transpiration.inShortWaveRadiation = id2ValueMap;
            
            longwaveReader.nextRecord();
            id2ValueMap = longwaveReader.outData;
            Transpiration.inLongWaveRadiation = id2ValueMap;
            
            pressureReader.nextRecord();
            id2ValueMap = pressureReader.outData;
            Transpiration.inAtmosphericPressure = id2ValueMap;
            
            leafAreaIndexReader.nextRecord();
            id2ValueMap = leafAreaIndexReader.outData;
            Transpiration.inLeafAreaIndex = id2ValueMap;
            
            Transpiration.pm = pm;
            Transpiration.process();

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
