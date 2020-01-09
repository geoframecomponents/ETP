package prosperoTestCase;

import org.junit.Test;

import prospero.OmsProsperoStress;

import java.net.URISyntaxException;
import java.util.HashMap;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.jgrasstools.gears.io.shapefile.OmsShapefileFeatureReader;
import org.jgrasstools.gears.io.timedependent.OmsTimeSeriesIteratorReader;
import org.jgrasstools.gears.io.timedependent.OmsTimeSeriesIteratorWriter;
import org.jgrasstools.gears.libs.monitor.PrintStreamProgressMonitor;


public class prosperoResistanceTest {
	@Test
    public void Test() throws Exception {
		String startDate= "2015-07-21 00:00";
        String endDate	= "2015-07-21 23:00";
        int timeStepMinutes = 60;
        String fId = "val";

        PrintStreamProgressMonitor pm = new PrintStreamProgressMonitor(System.out, System.out);

        String inPathToTemperature 			="resources/Input/dataET_point/AirTemperature.csv";
        String inPathToRelativeHumidity 	="resources/Input/dataET_point/RelativeHumidity.csv";
        String inPathToShortWaveRadiation	="resources/Input/dataET_point/ShortWaveRadiationDirect.csv";
        String inPathToSoilMosture			="resources/Input/dataET_point/SoilMosture.csv";
        String inPathToPressure 			="resources/Input/dataET_point/AtmosphericPressure.csv";
        
       // String inPathToCentroids 			="resources/Input/dataET_point/CentroidDem.shp";
                   
		String outPathToStressResistance	="resources/Output/stressResistance.csv";

		OmsTimeSeriesIteratorReader temperatureReader	= getTimeseriesReader(inPathToTemperature, fId, startDate, endDate, timeStepMinutes);
        OmsTimeSeriesIteratorReader humidityReader 		= getTimeseriesReader(inPathToRelativeHumidity, fId, startDate, endDate, timeStepMinutes);
        OmsTimeSeriesIteratorReader shortwaveReaderDirect 	= getTimeseriesReader(inPathToShortWaveRadiation, fId, startDate, endDate,timeStepMinutes);
        OmsTimeSeriesIteratorReader pressureReader 		= getTimeseriesReader(inPathToPressure, fId, startDate, endDate,timeStepMinutes);        
        OmsTimeSeriesIteratorReader soilMostureReader	= getTimeseriesReader(inPathToSoilMosture, fId, startDate, endDate, timeStepMinutes);

                
		/*OmsShapefileFeatureReader centroidsReader 		= new OmsShapefileFeatureReader();
        centroidsReader.file = inPathToCentroids;
		centroidsReader.readFeatureCollection();
		SimpleFeatureCollection stationsFC = centroidsReader.geodata;*/
		
		OmsTimeSeriesIteratorWriter stressResistanceWriter = new OmsTimeSeriesIteratorWriter();
		stressResistanceWriter.file = outPathToStressResistance;
		stressResistanceWriter.tStart = startDate;
		stressResistanceWriter.tTimestep = timeStepMinutes;
		stressResistanceWriter.fileNovalue="-9999";
	
		OmsProsperoStress Resistance = new OmsProsperoStress();
	//	Resistance.inCentroids = stationsFC;
	//	Resistance.idCentroids="id";
		//Resistance.centroidElevation="Elevation";	
		Resistance.elevation=100;	

		Resistance.alpha = 0.005;
        Resistance.theta = 0.85;
       // Resistance.d = 1.1;
        Resistance.VPD0 = 5.0;
     	
        Resistance.Tl = -5.0;
        Resistance.T0 = 20.0;
        Resistance.Th = 45.0;
     	
        Resistance.f = -40.0E-6;

        Resistance.thetaW = 0.205;
        Resistance.thetaC = 0.387;		
        
        while(temperatureReader.doProcess ) {
        	temperatureReader.nextRecord();
        	HashMap<Integer, double[]> id2ValueMap = temperatureReader.outData;
        	Resistance.inAirTemperature = id2ValueMap;                                             
        	
        	humidityReader.nextRecord();
            id2ValueMap = humidityReader.outData;
            Resistance.inRelativeHumidity = id2ValueMap;

            shortwaveReaderDirect.nextRecord();
            id2ValueMap = shortwaveReaderDirect.outData;
            Resistance.inShortWaveRadiationDirect = id2ValueMap;
            
            soilMostureReader.nextRecord();
            id2ValueMap = soilMostureReader.outData;
            Resistance.inSoilMosture = id2ValueMap;
                 
            pressureReader.nextRecord();
            id2ValueMap = pressureReader.outData;
            Resistance.inAtmosphericPressure = id2ValueMap;
            
            Resistance.pm = pm;
            Resistance.process();
            
            stressResistanceWriter.inData = Resistance.outStressResistance;
            stressResistanceWriter.writeNextLine();
			if (outPathToStressResistance != null) {
				stressResistanceWriter.close();
				}
		
	        }
       
        temperatureReader.close();
        humidityReader.close();
        shortwaveReaderDirect.close();
        soilMostureReader.close();
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
