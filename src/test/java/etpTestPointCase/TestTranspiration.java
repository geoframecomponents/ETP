package etpTestPointCase;
import java.net.URISyntaxException;
import java.util.HashMap;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.jgrasstools.gears.io.rasterreader.OmsRasterReader;
import org.jgrasstools.gears.io.shapefile.OmsShapefileFeatureReader;
import org.jgrasstools.gears.io.timedependent.OmsTimeSeriesIteratorReader;
import org.jgrasstools.gears.io.timedependent.OmsTimeSeriesIteratorWriter;
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
		String startDate= "2016-01-15 00:00";
        String endDate	= "2016-12-16 00:00";
        int timeStepMinutes = 60;
        String fId = "ID";

        PrintStreamProgressMonitor pm = new PrintStreamProgressMonitor(System.out, System.out);
        
        OmsRasterReader DEMreader = new OmsRasterReader();
		DEMreader.file = "resources/Input/dataET_raster/mybasin.asc";
		DEMreader.fileNovalue = -9999.0;
		DEMreader.geodataNovalue = Double.NaN;
		DEMreader.process();
		GridCoverage2D digitalElevationModel = DEMreader.outRaster;

        String inPathToTemperature 		="resources/Input/dataET_point/AirTemperature.csv";
        String inPathToWind 			="resources/Input/dataET_point/WindVelocity.csv";
        String inPathToRelativeHumidity ="resources/Input/dataET_point/RelativeHumidity.csv";
        String inPathToShortWaveRadiationDirect="resources/Input/dataET_point/ShortWaveRadiationDirect.csv";
        String inPathToShortWaveRadiationDiffuse="resources/Input/dataET_point/ShortWaveRadiationDiffuse.csv";
        String inPathToLWRad 			="resources/Input/dataET_point/LongWaveRadiation.csv";
        String inPathToPressure 		="resources/Input/dataET_point/AtmosphericPressure.csv";
        String inPathToLai 				="resources/Input/dataET_point/LeafAreaIndex.csv";
        String inPathToCentroids 		="resources/Input/dataET_point/CentroidDem.shp";
        String outPathToTranspiration	="resources/Output/Transpiration.csv";
		String outPathToLeafTemperature	="resources/Output/LeafTemperature.csv";

        OmsTimeSeriesIteratorReader temperatureReader	= getTimeseriesReader(inPathToTemperature, fId, startDate, endDate, timeStepMinutes);
        OmsTimeSeriesIteratorReader windReader 		 	= getTimeseriesReader(inPathToWind, fId, startDate, endDate, timeStepMinutes);
        OmsTimeSeriesIteratorReader humidityReader 		= getTimeseriesReader(inPathToRelativeHumidity, fId, startDate, endDate, timeStepMinutes);
        OmsTimeSeriesIteratorReader shortwaveReaderDirect 	= getTimeseriesReader(inPathToShortWaveRadiationDirect, fId, startDate, endDate,timeStepMinutes);
        OmsTimeSeriesIteratorReader shortwaveReaderDiffuse 	= getTimeseriesReader(inPathToShortWaveRadiationDiffuse, fId, startDate, endDate,timeStepMinutes);
        OmsTimeSeriesIteratorReader longwaveReader 		= getTimeseriesReader(inPathToLWRad, fId, startDate, endDate,timeStepMinutes);
        OmsTimeSeriesIteratorReader pressureReader 		= getTimeseriesReader(inPathToPressure, fId, startDate, endDate,timeStepMinutes);
        OmsTimeSeriesIteratorReader leafAreaIndexReader	= getTimeseriesReader(inPathToLai, fId, startDate, endDate,timeStepMinutes);      
                
		OmsShapefileFeatureReader centroidsReader 		= new OmsShapefileFeatureReader();
        centroidsReader.file = inPathToCentroids;
		centroidsReader.readFeatureCollection();
		SimpleFeatureCollection stationsFC = centroidsReader.geodata;
		
		
		OmsTimeSeriesIteratorWriter TranspirationWriter = new OmsTimeSeriesIteratorWriter();
		TranspirationWriter.file = outPathToTranspiration;
		TranspirationWriter.tStart = startDate;
		TranspirationWriter.tTimestep = timeStepMinutes;
		TranspirationWriter.fileNovalue="-9999";
		
		OmsTimeSeriesIteratorWriter leafTemperatureWriter = new OmsTimeSeriesIteratorWriter();
		leafTemperatureWriter.file = outPathToLeafTemperature;
		leafTemperatureWriter.tStart = startDate;
		leafTemperatureWriter.tTimestep = timeStepMinutes;
		leafTemperatureWriter.fileNovalue="-9999";
		
		OmsTranspiration Transpiration = new OmsTranspiration();
		Transpiration.inCentroids = stationsFC;
		Transpiration.idCentroids="id";
		Transpiration.centroidElevation="elevation";
		
        Transpiration.inDem = digitalElevationModel; 
         
        while(temperatureReader.doProcess ) {
        	temperatureReader.nextRecord();

            HashMap<Integer, double[]> id2ValueMap = temperatureReader.outData;
            Transpiration.inAirTemperature = id2ValueMap;
            Transpiration.doHourly = true;
            Transpiration.tStartDate = startDate;

           
            windReader.nextRecord();
            id2ValueMap = windReader.outData;
            Transpiration.inWindVelocity = id2ValueMap;

            humidityReader.nextRecord();
            id2ValueMap = humidityReader.outData;
            Transpiration.inRelativeHumidity = id2ValueMap;

            shortwaveReaderDirect.nextRecord();
            id2ValueMap = shortwaveReaderDirect.outData;
            Transpiration.inShortWaveRadiationDirect = id2ValueMap;
            
            shortwaveReaderDiffuse.nextRecord();
            id2ValueMap = shortwaveReaderDiffuse.outData;
            Transpiration.inShortWaveRadiationDiffuse = id2ValueMap;
            
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

			TranspirationWriter.inData = Transpiration.outTranspiration;
			TranspirationWriter.writeNextLine();			 	
			if (outPathToTranspiration != null) {
				TranspirationWriter.close();
				}
			leafTemperatureWriter.inData = Transpiration.outLeafTemperature;
			leafTemperatureWriter.writeNextLine();			 	
			if (outPathToLeafTemperature != null) {
				leafTemperatureWriter.close();
				}
	        }
       
        temperatureReader.close();
        windReader.close();
        humidityReader.close();
        shortwaveReaderDirect.close();
        shortwaveReaderDiffuse.close();
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
