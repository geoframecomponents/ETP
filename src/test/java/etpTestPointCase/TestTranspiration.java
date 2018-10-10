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
		String startDate= "2016-06-15 00:00";
        String endDate	= "2016-07-16 00:00";
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
       
        String outPathToLatentHeatSun	="resources/Output/LatentHeatSun.csv";
        String outPathToLatentHeatShadow="resources/Output/LatentHeatShadow.csv";
        String outPathToTranspiration	="resources/Output/Transpiration.csv";
		String outPathToLeafTemperatureSun	="resources/Output/LeafTemperatureSun.csv";
		String outPathToLeafTemperatureShadow	="resources/Output/LeafTemperatureSh.csv";

		
		String outPathToSun				="resources/Output/RadSun.csv";
		String outPathToShadow			="resources/Output/RadShadow.csv";

		
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
		
		OmsTimeSeriesIteratorWriter latentHeatSunWriter = new OmsTimeSeriesIteratorWriter();
		latentHeatSunWriter.file = outPathToLatentHeatSun;
		latentHeatSunWriter.tStart = startDate;
		latentHeatSunWriter.tTimestep = timeStepMinutes;
		latentHeatSunWriter.fileNovalue="-9999";
		
		OmsTimeSeriesIteratorWriter latentHeatShadowWriter = new OmsTimeSeriesIteratorWriter();
		latentHeatShadowWriter.file = outPathToLatentHeatShadow;
		latentHeatShadowWriter.tStart = startDate;
		latentHeatShadowWriter.tTimestep = timeStepMinutes;
		latentHeatShadowWriter.fileNovalue="-9999";
		
		OmsTimeSeriesIteratorWriter TranspirationWriter = new OmsTimeSeriesIteratorWriter();
		TranspirationWriter.file = outPathToTranspiration;
		TranspirationWriter.tStart = startDate;
		TranspirationWriter.tTimestep = timeStepMinutes;
		TranspirationWriter.fileNovalue="-9999";
		
		OmsTimeSeriesIteratorWriter leafTemperatureSunWriter = new OmsTimeSeriesIteratorWriter();
		leafTemperatureSunWriter.file = outPathToLeafTemperatureSun;
		leafTemperatureSunWriter.tStart = startDate;
		leafTemperatureSunWriter.tTimestep = timeStepMinutes;
		leafTemperatureSunWriter.fileNovalue="-9999";
		
		OmsTimeSeriesIteratorWriter leafTemperatureShadowWriter = new OmsTimeSeriesIteratorWriter();
		leafTemperatureShadowWriter.file = outPathToLeafTemperatureShadow;
		leafTemperatureShadowWriter.tStart = startDate;
		leafTemperatureShadowWriter.tTimestep = timeStepMinutes;
		leafTemperatureShadowWriter.fileNovalue="-9999";
		
		OmsTimeSeriesIteratorWriter radiationSunWriter = new OmsTimeSeriesIteratorWriter();
		radiationSunWriter.file = outPathToSun;
		radiationSunWriter.tStart = startDate;
		radiationSunWriter.tTimestep = timeStepMinutes;
		radiationSunWriter.fileNovalue="-9999";
		
		OmsTimeSeriesIteratorWriter radiationShadowWriter = new OmsTimeSeriesIteratorWriter();
		radiationShadowWriter.file = outPathToShadow;
		radiationShadowWriter.tStart = startDate;
		radiationShadowWriter.tTimestep = timeStepMinutes;
		radiationShadowWriter.fileNovalue="-9999";
		
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
            Transpiration.doFullPrint = false;
            Transpiration.typeOfTerrainCover = "FlatSurface";
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
            


            latentHeatSunWriter.inData = Transpiration.outLatentHeatSun;
            latentHeatSunWriter.writeNextLine();
            latentHeatShadowWriter.inData = Transpiration.outLatentHeatShadow;
            latentHeatShadowWriter.writeNextLine();			 	
            
			if (outPathToLatentHeatSun != null) {
				latentHeatSunWriter.close();
				}
			
			TranspirationWriter.inData = Transpiration.outTranspiration;
			TranspirationWriter.writeNextLine();			 	
			if (outPathToTranspiration != null) {
				TranspirationWriter.close();
				}
			if (Transpiration.doFullPrint == true) {
			leafTemperatureSunWriter.inData = Transpiration.outLeafTemperatureSun;
			leafTemperatureSunWriter.writeNextLine();			 	
			if (outPathToLeafTemperatureSun != null) {
				leafTemperatureSunWriter.close();
				}
			radiationSunWriter.inData = Transpiration.outRadiationSun;
			radiationSunWriter.writeNextLine();			 	
			if (outPathToSun != null) {
				radiationSunWriter.close();
				}
			radiationShadowWriter.inData = Transpiration.outRadiationShadow;
			radiationShadowWriter.writeNextLine();			 	
			if (outPathToShadow != null) {
				radiationShadowWriter.close();
				}
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
