package prosperoTestCase;
import java.net.URISyntaxException;
import java.util.HashMap;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.jgrasstools.gears.io.rasterreader.OmsRasterReader;
import org.jgrasstools.gears.io.shapefile.OmsShapefileFeatureReader;
import org.jgrasstools.gears.io.timedependent.OmsTimeSeriesIteratorReader;
import org.jgrasstools.gears.io.timedependent.OmsTimeSeriesIteratorWriter;
import org.jgrasstools.gears.libs.monitor.PrintStreamProgressMonitor;
import org.junit.*;

//import etpPointCase.OmsTranspiration;
import prospero.OmsProspero;
//import static org.junit.Assert.assertTrue;
/**
 * Test Schymanski & Or evapotranspiration.
 * @author Michele Bottazzi (michele.bottazzi@gmail.com)
 */
//@SuppressWarnings("nls")
public class prosperoTest{
	@Test
    public void Test() throws Exception {
		String startDate= "2013-12-15 10:00";
        String endDate	= "2014-12-31 00:00";
        int timeStepMinutes = 60;
        String fId = "ID";

        PrintStreamProgressMonitor pm = new PrintStreamProgressMonitor(System.out, System.out);
        
        String pathToInput 				= "/home/drugo/Downloads/WinterETP/data/Cavone_modificato_dati_Michele/1_origin/";
        String basin = "Cavone";
        OmsRasterReader DEMreader = new OmsRasterReader();
		DEMreader.file =  pathToInput + "dem_1.asc";
		DEMreader.fileNovalue = -9999.0;
		DEMreader.geodataNovalue = Double.NaN;
		DEMreader.process();
		GridCoverage2D digitalElevationModel = DEMreader.outRaster;
		
        String inPathToTemperature 		= pathToInput + "airT_1.csv";
        String inPathToWind 			= pathToInput + basin +"_Wind_1.csv";
        String inPathToRelativeHumidity = pathToInput + basin +"_RH_1.csv";
        String inPathToShortWaveRadiationDirect= pathToInput + basin +"_SwDirect_1.csv";
        String inPathToShortWaveRadiationDiffuse= pathToInput + basin +"_null.csv";
        String inPathToLWRad 			= pathToInput + basin +"_null.csv";
        String inPathToNetRad 			= pathToInput + basin +"_Net_1.csv";

        String inPathToSoilHeatFlux 	= pathToInput + basin +"_GHF_1.csv";


        String inPathToPressure 		= pathToInput + basin +"_Pres_1.csv";
        String inPathToLai 				= pathToInput + "LAI_1.csv";
        String inPathToCentroids 		= pathToInput + "centroids_ID_1.shp";

        
        String outPathToLatentHeatSun			="resources/Output/LatentHeatSun.csv";
        String outPathToLatentHeatShadow		="resources/Output/LatentHeatShadow.csv";
        String outPathToTranspiration			="resources/Output/Transpiration.csv";
		String outPathToLeafTemperatureSun		="resources/Output/LeafTemperatureSun.csv";
		String outPathToLeafTemperatureShadow	="resources/Output/LeafTemperatureSh.csv";
		String outPathToSensibleSun				="resources/Output/sensibleSun.csv";
		String outPathToSensibleShadow			="resources/Output/sensibleShadow.csv";
        String outPathToSoilEvaporation 		="resources/Output/Evaporation.csv";

		String outPathToSun				="resources/Output/RadSun.csv";
		String outPathToShadow			="resources/Output/RadShadow.csv";
		String outPathToCanopy			="resources/Output/Canopy.csv";
	
        OmsTimeSeriesIteratorReader temperatureReader	= getTimeseriesReader(inPathToTemperature, fId, startDate, endDate, timeStepMinutes);
        OmsTimeSeriesIteratorReader windReader 		 	= getTimeseriesReader(inPathToWind, fId, startDate, endDate, timeStepMinutes);
        OmsTimeSeriesIteratorReader humidityReader 		= getTimeseriesReader(inPathToRelativeHumidity, fId, startDate, endDate, timeStepMinutes);
        OmsTimeSeriesIteratorReader shortwaveReaderDirect 	= getTimeseriesReader(inPathToShortWaveRadiationDirect, fId, startDate, endDate,timeStepMinutes);
        OmsTimeSeriesIteratorReader shortwaveReaderDiffuse 	= getTimeseriesReader(inPathToShortWaveRadiationDiffuse, fId, startDate, endDate,timeStepMinutes);
        OmsTimeSeriesIteratorReader longwaveReader 		= getTimeseriesReader(inPathToLWRad, fId, startDate, endDate,timeStepMinutes);
        OmsTimeSeriesIteratorReader pressureReader 		= getTimeseriesReader(inPathToPressure, fId, startDate, endDate,timeStepMinutes);
        OmsTimeSeriesIteratorReader leafAreaIndexReader	= getTimeseriesReader(inPathToLai, fId, startDate, endDate,timeStepMinutes);
        OmsTimeSeriesIteratorReader soilHeatFluxReader 	= getTimeseriesReader(inPathToSoilHeatFlux, fId, startDate, endDate,timeStepMinutes);
        OmsTimeSeriesIteratorReader netRadReader 	= getTimeseriesReader(inPathToNetRad, fId, startDate, endDate,timeStepMinutes);

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
		
		OmsTimeSeriesIteratorWriter sensibleSunWriter = new OmsTimeSeriesIteratorWriter();
		sensibleSunWriter.file = outPathToSensibleSun;
		sensibleSunWriter.tStart = startDate;
		sensibleSunWriter.tTimestep = timeStepMinutes;
		sensibleSunWriter.fileNovalue="-9999";
		
		OmsTimeSeriesIteratorWriter sensibleShadowWriter = new OmsTimeSeriesIteratorWriter();
		sensibleShadowWriter.file = outPathToSensibleShadow;
		sensibleShadowWriter.tStart = startDate;
		sensibleShadowWriter.tTimestep = timeStepMinutes;
		sensibleShadowWriter.fileNovalue="-9999";
		
		OmsTimeSeriesIteratorWriter evaporationWriter = new OmsTimeSeriesIteratorWriter();
		evaporationWriter.file = outPathToSoilEvaporation;
		evaporationWriter.tStart = startDate;
		evaporationWriter.tTimestep = timeStepMinutes;
		evaporationWriter.fileNovalue="-9999";
		
		OmsTimeSeriesIteratorWriter canopyWriter = new OmsTimeSeriesIteratorWriter();
		canopyWriter.file = outPathToCanopy;
		canopyWriter.tStart = startDate;
		canopyWriter.tTimestep = timeStepMinutes;
		canopyWriter.fileNovalue="-9999";
		
		OmsProspero Prospero= new OmsProspero();
		Prospero.inCentroids = stationsFC;
		Prospero.idCentroids="ID";
		Prospero.centroidElevation="pitfiller_";
		
		Prospero.inDem = digitalElevationModel; 


		//Prospero.elevation = 1556;
		//Prospero.latitude = 46.015966;
		//Prospero.longitude = 11.045879;
		Prospero.canopyHeight = 0.2;
		Prospero.defaultStress = 1.0;
		Prospero.doIterative = false;
		
		Prospero.useRadiationStress=true;
		Prospero.useTemperatureStress=false;
		Prospero.useVDPStress=false;
		Prospero.useWaterStress=true;
		
		
		Prospero.alpha = 0.005;
		Prospero.theta = 0.9;
		Prospero.VPD0 = 5.0;
        	
		Prospero.Tl = -5.0;
		Prospero.T0 = 20.0;
		Prospero.Th = 45.0;
		Prospero.typeOfCanopy="multilayer";
		Prospero.waterWiltingPoint = 0.15;
		Prospero.waterFieldCapacity = 0.27; 
		Prospero.rootsDepth = 0.75;
		Prospero.depletionFraction = 0.55;        
		
		while(temperatureReader.doProcess ) {
        	temperatureReader.nextRecord();
        	
       		

            HashMap<Integer, double[]> id2ValueMap = temperatureReader.outData;
            Prospero.inAirTemperature = id2ValueMap;
            Prospero.doHourly = true;
            Prospero.doFullPrint = true;
           //Prospero.typeOfTerrainCover = "FlatSurface";
            Prospero.tStartDate = startDate;
            Prospero.temporalStep = timeStepMinutes;

            windReader.nextRecord();
            id2ValueMap = windReader.outData;
            Prospero.inWindVelocity = id2ValueMap;

            humidityReader.nextRecord();
            id2ValueMap = humidityReader.outData;
            Prospero.inRelativeHumidity = id2ValueMap;

            shortwaveReaderDirect.nextRecord();
            id2ValueMap = shortwaveReaderDirect.outData;
            Prospero.inShortWaveRadiationDirect = id2ValueMap;
            
            shortwaveReaderDiffuse.nextRecord();
            id2ValueMap = shortwaveReaderDiffuse.outData;
            Prospero.inShortWaveRadiationDiffuse = id2ValueMap;
            
            longwaveReader.nextRecord();
            id2ValueMap = longwaveReader.outData;
            Prospero.inLongWaveRadiation = id2ValueMap;
            
            soilHeatFluxReader.nextRecord();
            id2ValueMap = soilHeatFluxReader.outData;
            Prospero.inSoilFlux = id2ValueMap;
            
            pressureReader.nextRecord();
            id2ValueMap = pressureReader.outData;
            Prospero.inAtmosphericPressure = id2ValueMap;
            
            leafAreaIndexReader.nextRecord();
            id2ValueMap = leafAreaIndexReader.outData;
            Prospero.inLeafAreaIndex = id2ValueMap;
            
            netRadReader.nextRecord();
            id2ValueMap = netRadReader.outData;
            Prospero.inNetLongWaveRadiation = id2ValueMap;
            
           // inPathToNetRad
            Prospero.pm = pm;
            Prospero.process();

            latentHeatSunWriter.inData = Prospero.outLatentHeat;
            latentHeatSunWriter.writeNextLine();

			latentHeatShadowWriter.inData = Prospero.outLatentHeatShade;
            latentHeatShadowWriter.writeNextLine();			 	

			TranspirationWriter.inData = Prospero.outTranspiration;
			TranspirationWriter.writeNextLine();			 	

			if (Prospero.doFullPrint == true) {
			leafTemperatureSunWriter.inData = Prospero.outLeafTemperature;
			leafTemperatureSunWriter.writeNextLine();			 	

			leafTemperatureShadowWriter.inData = Prospero.outLeafTemperatureShade;
			leafTemperatureShadowWriter.writeNextLine();			 	

			radiationSunWriter.inData = Prospero.outRadiation;
			radiationSunWriter.writeNextLine();			 	

			radiationShadowWriter.inData = Prospero.outRadiationShade;
			radiationShadowWriter.writeNextLine();			 	
			
			sensibleSunWriter.inData = Prospero.outSensibleHeat;
			sensibleSunWriter.writeNextLine();			 	
			
			sensibleShadowWriter.inData = Prospero.outSensibleHeatShade;
			sensibleShadowWriter.writeNextLine();	
			
			evaporationWriter.inData = Prospero.outEvaporation;
			evaporationWriter.writeNextLine();
			
			canopyWriter.inData = Prospero.outCanopy;
			canopyWriter.writeNextLine();
			}
	        }
       
        temperatureReader.close();        
        windReader.close();
        humidityReader.close();     
        shortwaveReaderDirect.close();
        shortwaveReaderDiffuse.close();
        longwaveReader.close();
        soilHeatFluxReader.close();
        pressureReader.close();
        leafAreaIndexReader.close();
                
        latentHeatSunWriter.close();
		latentHeatShadowWriter.close();
		TranspirationWriter.close();
		leafTemperatureSunWriter.close();
		leafTemperatureShadowWriter.close();
		radiationSunWriter.close();
		radiationShadowWriter.close();
		sensibleSunWriter.close();
		sensibleShadowWriter.close();
		evaporationWriter.close();
		canopyWriter.close();


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
