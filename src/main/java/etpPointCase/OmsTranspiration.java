package etpPointCase;
import static java.lang.Math.pow;
//import static org.jgrasstools.gears.libs.modules.JGTConstants.isNovalue;
//import static org.jgrasstools.gears.libs.modules.JGTConstants.isNovalue;
import static java.lang.Math.abs;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;

import oms3.annotations.Author;
import oms3.annotations.Description;
import oms3.annotations.Execute;
import oms3.annotations.In;
import oms3.annotations.Keywords;
import oms3.annotations.Label;
import oms3.annotations.License;
import oms3.annotations.Name;
import oms3.annotations.Out;
import oms3.annotations.Status;
import oms3.annotations.Unit;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.SchemaException;
import org.jgrasstools.gears.libs.modules.JGTModel;
import org.jgrasstools.hortonmachine.i18n.HortonMessageHandler;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import etpClasses.*;

/*
* This file is part of JGrasstools (http://www.jgrasstools.org)
* (C) HydroloGIS - www.hydrologis.com
*
* JGrasstools is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
@Description("Calculates evapotranspiration at hourly/daily timestep using Schimanski & Or formula")
@Author(name = "Michele Bottazzi", contact = "michele.bottazzi@gmail.com")
@Keywords("Evapotranspiration, Hydrology")
@Label("")
@Name("")
@Status(Status.CERTIFIED)
@License("General Public License Version 3 (GPLv3)")
public class OmsTranspiration extends JGTModel implements Parameters {
	
	// ENVIRONMENTAL VARIABLES
	@Description("Air temperature.")
	@In
	@Unit("K")
	public HashMap<Integer, double[]> inAirTemperature;
	@Description("The air temperature default value in case of missing data.")
	@In
	@Unit("K")
	public double defaultAirTemperature = 15.0+273.0;
	  
	@Description("The wind speed.")
	@In
	@Unit("m s-1")
	public HashMap<Integer, double[]> inWindVelocity;
	@Description("The wind default value in case of missing data.")
	@In
	@Unit("m s-1")
	public double defaultWindVelocity = 2.0;
	
	@Description("The air relative humidity.")
	@In
	@Unit("%")
	public HashMap<Integer, double[]> inRelativeHumidity;
	@Description("The humidity default value in case of missing data.")
	@In
	@Unit("%")
	public double defaultRelativeHumidity = 70.0;
	
	@Description("The short wave radiation at the surface.")
	@In
	@Unit("W m-2")
	public HashMap<Integer, double[]> inShortWaveRadiation;
	@Description("The short wave radiation default value in case of missing data.")
	@In
	@Unit("W m-2")
	public double defaultShortWaveRadiation = 0.0;
	
	@Description("The long wave radiation at the surface.")
	@In
	@Unit("W m-2")
	public HashMap<Integer, double[]> inLongWaveRadiation;

	@Description("The atmospheric pressure.")
	@In
	@Unit("Pa")
	public HashMap<Integer, double[]> inAtmosphericPressure;
	@Description("The atmospheric pressure default value in case of missing data.")
	@In
	@Unit("Pa")
	public double defaultAtmosphericPressure = 101325.0;
	
	@Description("The soilflux.")
	@In
	@Unit("W m-2")
	public HashMap<Integer, double[]> inSoilFlux;
	@Description("The soilflux default value in case of missing data.")
	@In
	@Unit("W m-2")
	public double defaultSoilFlux = 0.0;
	
	// GEOGRAPHIC VARIABLES
	@Description("The elevation of the centroid.")
	@In
	@Unit("m")
	public String fPointZ;
	
	@Description("The shape file with the station measuremnts")
	@In
	public SimpleFeatureCollection inStations;

	@Description("The name of the field containing the ID of the station in the shape file")
	@In
	public String fStationsid;

	@Description(" The vetor containing the id of the station")
	Object []basinId;
	//@Description(" The vetor containing the id of the station")
//	Object []elevStations;
	
	double elevation;

	@Description("the linked HashMap with the coordinate of the stations")
	LinkedHashMap<Integer, Coordinate> stationCoordinates;
	
	// ECOLOGICAL VARIABLES
	
	@Description("Leaf area index.")
	@In
	@Unit("m2 m-2")
	public HashMap<Integer, double[]> inLeafAreaIndex;
	@Description("The leaf area index default value in case of missing data.")
	@In
	@Unit("m2 m-2")
	public double defaultLeafAreaIndex = 1.0;
	
	// OUTPUT
	@Description("The reference evapotranspiration.")
	@Unit("mm day-1")
	@Out
	public HashMap<Integer, double[]> outTranspiration;
	@Description("The reference evapotranspiration.")
	@Unit("mm day-1")
	@Out
	public HashMap<Integer, double[]> outLeafTemperature;
	
	// OTHERS
	@Description("area.")
	@In
	@Unit("m2")
	public double area;
	
	@Description("Switch that defines if it is hourly.")
	@In
	public boolean doHourly = true;

	double nullValue = -9999.0;
	public int time;
	private HortonMessageHandler msg = HortonMessageHandler.getInstance();
	
	// METHODS FROM CLASSES
		
	SensibleHeat sensibleHeat = new SensibleHeat();
	LatentHeat latentHeat = new LatentHeat();
	Pressures getPressure = new Pressures(); 
	LongWaveRadiationBalance longWaveRadiationBalance = new LongWaveRadiationBalance();

	@Execute
	public void process() throws Exception {
			
		stationCoordinates = getCoordinate(0,inStations, fStationsid);
		Iterator<Integer> idIterator = stationCoordinates.keySet().iterator();
		basinId= stationCoordinates.keySet().toArray();
		Coordinate coordinate = (Coordinate) stationCoordinates.get(idIterator.next());
		elevation = coordinate.z;
		outTranspiration = new HashMap<Integer, double[]>();
		outLeafTemperature = new HashMap<Integer, double[]>();
		Set<Entry<Integer, double[]>> entrySet = inAirTemperature.entrySet();
		//for (int i=0;i<basinId.length;i++){
		for( Entry<Integer, double[]> entry : entrySet ) {
			Integer basinId = entry.getKey();
			
			if (doHourly == true) {
				time =3600;
				} else {
				time = 86400;
				}

			Leaf propertyOfLeaf = new Leaf();
			double poreRadius = propertyOfLeaf.poreRadius;
			double poreArea = propertyOfLeaf.poreArea;
			double poreDepth = propertyOfLeaf.poreDepth;
			double poreDensity = propertyOfLeaf.poreDensity;
			double leafLength = propertyOfLeaf.length;
			int leafSide = propertyOfLeaf.side;
			// Shortwave property
			double shortWaveAbsorption = propertyOfLeaf.shortWaveAbsorption;	
			double shortWaveReflectance = propertyOfLeaf.shortWaveReflectance;	
			double shortWaveTransmittance = propertyOfLeaf.shortWaveTransmittance;
			// Longwave property
			double longWaveAbsorption = propertyOfLeaf.longWaveAbsorption;	
			double longWaveReflectance = propertyOfLeaf.longWaveReflectance;	
			double longWaveTransmittance = propertyOfLeaf.longWaveTransmittance;
			double longWaveEmittance = propertyOfLeaf.longWaveEmittance;
			

		
			double relativeHumidity = inRelativeHumidity.get(basinId)[0];
			if (relativeHumidity == nullValue) {relativeHumidity = defaultRelativeHumidity;}
			
			double airTemperature = inAirTemperature.get(basinId)[0]+273.0;
			if (airTemperature == (nullValue+273.0)) {airTemperature = defaultAirTemperature;}		
			
			double leafTemperature = airTemperature;   	
			
			double shortWaveRadiation = inShortWaveRadiation.get(basinId)[0];
			if (shortWaveRadiation == nullValue) {shortWaveRadiation = defaultShortWaveRadiation;}   
			
			double absorbedRadiation = shortWaveRadiation * shortWaveAbsorption;
			
			double longWaveRadiation = inLongWaveRadiation.get(basinId)[0];
			if (longWaveRadiation == nullValue) {longWaveRadiation = longWaveEmittance * stefanBoltzmannConstant * pow (airTemperature, 4);}//defaultLongWaveRadiation;}
			
			double windVelocity = inWindVelocity.get(basinId)[0];
			if (windVelocity == nullValue) {windVelocity = defaultWindVelocity;}   
			
			double atmosphericPressure = inAtmosphericPressure.get(basinId)[0];
			if (atmosphericPressure == nullValue) {atmosphericPressure = getPressure.computePressure(defaultAtmosphericPressure, massAirMolecule, gravityConstant, elevation,boltzmannConstant, airTemperature);}	

			double leafAreaIndex = inLeafAreaIndex.get(basinId)[0];
			if (leafAreaIndex == nullValue) {leafAreaIndex = defaultLeafAreaIndex;}	
			
			double saturationVaporPressure = getPressure.computeSaturationVaporPressure(airTemperature, waterMolarMass, latentHeatEvaporation, molarGasConstant);
			double vaporPressure = relativeHumidity * saturationVaporPressure/100.0;
			double delta = getPressure.computeDelta(airTemperature, waterMolarMass, latentHeatEvaporation, molarGasConstant);
			
			double convectiveTransferCoefficient = sensibleHeat.computeConvectiveTransferCoefficient(airTemperature, windVelocity, leafLength, criticalReynoldsNumber, prandtlNumber);
			double sensibleHeatTransferCoefficient = sensibleHeat.computeSensibleHeatTransferCoefficient(convectiveTransferCoefficient, leafSide);
			double latentHeatTransferCoefficient = latentHeat.computeLatentHeatTransferCoefficient(airTemperature, atmosphericPressure, leafSide, convectiveTransferCoefficient, airSpecificHeat, 
					airDensity, molarGasConstant, molarVolume, waterMolarMass, latentHeatEvaporation, poreDensity, poreArea, poreDepth, poreRadius);
			
			if (leafAreaIndex != 0) {	

			shortWaveRadiation = absorbedRadiation;
			double residual = 10.0;
			double latentHeatFlux = 0;
			double sensibleHeatFlux = 0;
			double netLongWaveRadiation = 0;
			double leafTemperatureSun = leafTemperature;
			double TranspirationSun = 0;
			double TranspirationShadow = 0;
			while(abs(residual) > 1) 
				{
				sensibleHeatFlux = sensibleHeat.computeSensibleHeatFlux(sensibleHeatTransferCoefficient, leafTemperatureSun, airTemperature);
				latentHeatFlux = latentHeat.computeLatentHeatFlux(delta, leafTemperatureSun, airTemperature, latentHeatTransferCoefficient, sensibleHeatTransferCoefficient, vaporPressure, saturationVaporPressure);
				netLongWaveRadiation = longWaveRadiationBalance.computeLongWaveRadiationBalance(leafSide, longWaveEmittance, airTemperature, leafTemperatureSun, stefanBoltzmannConstant);
				residual = (shortWaveRadiation - netLongWaveRadiation) - sensibleHeatFlux - latentHeatFlux;
				leafTemperatureSun = computeLeafTemperature(leafSide, longWaveEmittance, sensibleHeatTransferCoefficient,latentHeatTransferCoefficient,airTemperature,shortWaveRadiation,longWaveRadiation,vaporPressure, saturationVaporPressure,delta);
				}
			TranspirationSun = latentHeat.computeLatentHeatFlux(delta, leafTemperatureSun, airTemperature, latentHeatTransferCoefficient, sensibleHeatTransferCoefficient, vaporPressure, saturationVaporPressure);
			//outLeafTemperature.put(basinId, new double[]{leafTemperatureSun});

			shortWaveRadiation = absorbedRadiation*0.2;
			double residualSh = 10.0;
			double latentHeatFluxSh = 0;
			double sensibleHeatFluxSh = 0;
			double netLongWaveRadiationSh = 0;
			double leafTemperatureSh = leafTemperature;
			
			while(abs(residualSh) > 1) 
				{
				sensibleHeatFluxSh = sensibleHeat.computeSensibleHeatFlux(sensibleHeatTransferCoefficient, leafTemperatureSh, airTemperature);
				latentHeatFluxSh = latentHeat.computeLatentHeatFlux(delta, leafTemperatureSh, airTemperature, latentHeatTransferCoefficient, sensibleHeatTransferCoefficient, vaporPressure, saturationVaporPressure);
				netLongWaveRadiationSh = longWaveRadiationBalance.computeLongWaveRadiationBalance(leafSide, longWaveEmittance, airTemperature, leafTemperatureSh, stefanBoltzmannConstant);
				residualSh = (shortWaveRadiation- netLongWaveRadiationSh) - sensibleHeatFluxSh - latentHeatFluxSh;
				leafTemperatureSh = computeLeafTemperature(leafSide, longWaveEmittance,sensibleHeatTransferCoefficient,latentHeatTransferCoefficient,airTemperature,shortWaveRadiation,longWaveRadiation,vaporPressure, saturationVaporPressure,delta);
				}
			TranspirationShadow = latentHeat.computeLatentHeatFlux(delta, leafTemperatureSh, airTemperature, latentHeatTransferCoefficient, sensibleHeatTransferCoefficient, vaporPressure, saturationVaporPressure);
			
			double TotalTranspiration = ((2.0*TranspirationSun) + (TranspirationShadow*(leafAreaIndex-2.0*area)))*(time/latentHeatEvaporation);
			
			storeResult((Integer)basinId,TotalTranspiration,leafTemperatureSun);}
			else {storeResult((Integer)basinId,0,0);}
			//outTranspiration.put(basinId, new double[]{(((2.0*TranspirationSun) + (TranspirationShadow*(leafAreaIndex-2.0*area)))*time/latentHeatEvaporation)});
			}
		}
	
	private double computeLeafTemperature(
			double side,
			double emissivity,
			double sensibleHeatTransferCoefficient,
			double latentHeatTransferCoefficient, 
			double airTemperature, 
			double shortWaveRadiation,
			double longWaveRadiation,
			double vaporPressure,
			double saturationVaporPressure,
			double delta) {
		double leafTemperature = (shortWaveRadiation + sensibleHeatTransferCoefficient*airTemperature +
				latentHeatTransferCoefficient*(delta*airTemperature + vaporPressure - saturationVaporPressure) + 
				side * longWaveRadiation * 4 )*
				(1/(sensibleHeatTransferCoefficient + latentHeatTransferCoefficient * delta +	
				side * longWaveRadiation/airTemperature * 4));
		return leafTemperature;	
	}
	
	private LinkedHashMap<Integer, Coordinate> getCoordinate(int nStaz,
			SimpleFeatureCollection collection, String idField)
					throws Exception {
		LinkedHashMap<Integer, Coordinate> id2CoordinatesMcovarianceMatrix = new LinkedHashMap<Integer, Coordinate>();
		FeatureIterator<SimpleFeature> iterator = collection.features();
		Coordinate coordinate = null;
		try {
			while (iterator.hasNext()) {
				SimpleFeature feature = iterator.next();
				int name = ((Number) feature.getAttribute(idField)).intValue();
				coordinate = ((Geometry) feature.getDefaultGeometry())
						.getCentroid().getCoordinate();
				double z = 0;
				if (fPointZ != null) {
					try {
						z = ((Number) feature.getAttribute(fPointZ))
								.doubleValue();
					} catch (NullPointerException e) {
						pm.errorMessage(msg.message("kriging.noPointZ"));
						throw new Exception(msg.message("kriging.noPointZ"));
					}
				}
				coordinate.z = z;
				id2CoordinatesMcovarianceMatrix.put(name, coordinate);
			}
		} finally {
			iterator.close();
		}

		return id2CoordinatesMcovarianceMatrix;
	}
	private void storeResult(int ID,double TotalTranspiration, double leafTemperatureSun) 
			throws SchemaException {

		outTranspiration.put(ID, new double[]{TotalTranspiration});
		outLeafTemperature.put(ID, new double[]{leafTemperatureSun});
	}
}
