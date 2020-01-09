package prospero;

import static java.lang.Math.pow;
//import static org.jgrasstools.gears.libs.modules.JGTConstants.isNovalue;
//import static org.jgrasstools.gears.libs.modules.JGTConstants.isNovalue;



import java.util.HashMap;

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
import prosperoClasses.*;


import org.geotools.feature.SchemaException;
import org.jgrasstools.gears.libs.modules.JGTModel;

import org.jgrasstools.hortonmachine.i18n.HortonMessageHandler;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

@Description("The Prospero model")
@Author(name = "Michele Bottazzi", contact = "michele.bottazzi@gmail.com")
@Keywords("Evapotranspiration")
@Label("")
@Name("")
@Status(Status.CERTIFIED)
@License("General Public License Version 3 (GPLv3)")
public class OmsProsperoGrass extends JGTModel implements Parameters {
	
	/////////////////////////////////////////////
	// ENVIRONMENTAL VARIABLES - INPUT
	/////////////////////////////////////////////

	@Description("Air temperature.")
	@In
	@Unit("K")
	public HashMap<Integer, double[]> inAirTemperature;
	
	@Description("The wind speed.")
	@In
	@Unit("m s-1")
	public HashMap<Integer, double[]> inWindVelocity;
	
	@Description("The air relative humidity.")
	@In
	@Unit("%")
	public HashMap<Integer, double[]> inRelativeHumidity;
	
	@Description("The short wave radiation at the surface.")
	@In
	@Unit("W m-2")
	public HashMap<Integer, double[]> inShortWaveRadiationDirect;
	
	@Description("The short wave radiation at the surface.")
	@In
	@Unit("W m-2")
	public HashMap<Integer, double[]> inShortWaveRadiationDiffuse;
	
	@Description("The long wave radiation at the surface.")
	@In
	@Unit("W m-2")
	public HashMap<Integer, double[]> inLongWaveRadiation;

	@Description("The atmospheric pressure.")
	@In
	@Unit("Pa")
	public HashMap<Integer, double[]> inAtmosphericPressure;

	@Description("The soilflux.")
	@In
	@Unit("W m-2")
	public HashMap<Integer, double[]> inSoilFlux;
	
	@Description("Leaf area index.")
	@In
	@Unit("m2 m-2")
	public HashMap<Integer, double[]> inLeafAreaIndex;
	
	@Description("Input soil moisture.")
	@In
	@Unit("m3 m-3")
	public HashMap<Integer, double[]> inSoilMoisture;
	
	/////////////////////////////////////////////
	// ENVIRONMENTAL VARIABLES - DEFAULT
	/////////////////////////////////////////////

	@Description("The air temperature default value in case of missing data.")
	@In
	@Unit("K")
	public double defaultAirTemperature = 15.0+273.0;
	  
	@Description("The wind default value in case of missing data.")
	@In
	@Unit("m s-1")
	public double defaultWindVelocity = 1.5;
	
	@Description("The humidity default value in case of missing data.")
	@In
	@Unit("%")
	public double defaultRelativeHumidity = 70.0;
		
	@Description("The short wave radiation default value in case of missing data.")
	@In
	@Unit("W m-2")
	public double defaultShortWaveRadiationDirect = 0.0;
	
	@Description("The atmospheric pressure default value in case of missing data.")
	@In
	@Unit("Pa")
	public double defaultAtmosphericPressure = 101325.0;
	
	@Description("The soilflux default value in case of missing data.")
	@In
	@Unit("W m-2")
	public double defaultSoilFlux = 0.0;
	
	@Description("The leaf area index default value in case of missing data.")
	@In
	@Unit("m2 m-2")
	public double defaultLeafAreaIndex = 1.0;
		
	@Description("Default soil moisture.")
	@In
	@Unit("m3 m-3")
	public double defaultSoilMoisture = 0.20;
	
	@Description("The short wave radiation highlighting the canopy in sunlight.")
	@In
	@Unit("W m-2")
	public double shortWaveRadiationInSun;
	

		
	/////////////////////////////////////////////
	// GEOGRAPHIC VARIABLES - DEFAULT
	/////////////////////////////////////////////
	@Description("The elevation of the centroid.")
	@In
	@Unit("m")
	public String centroidElevation;
	

	@In public double elevation;
	@In public double latitude;
	@In public double longitude;
	@In public double canopyHeight;
	
	@In	public double alpha;
	@In public double theta;
	@In public double VPD0;

	@In	public double T0;
	@In public double Tl;
	@In public double Th;
	
	//@In	public double b5;
	/*@In public double f;
	@In public double thetaW;
	@In public double thetaC;*/
	
	@In public double waterFieldCapacity;
	@In public double waterWiltingPoint;
	@In public double rootsDepth;
	@In public double depletionFraction;
		
	@Description(" The vetor containing the id of the station")
	Object []ID;
		@Description("Type of transpiring area")
	@In
	public String typeOfTerrainCover;

	

	@Description("The first day of the simulation.")
	@In
	public String tStartDate;
	public DateTime date;
	
	@Description("The first day of the simulation.")
	@In
	public int temporalStep;
	@Description("It is needed to iterate on the date")
	int step;
	
	
	double nullValue = -9999.0;
	public int time;
	private HortonMessageHandler msg = HortonMessageHandler.getInstance();
	DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").withZone(DateTimeZone.UTC);
	
	
	/////////////////////////////////////////////
	// ECOLOGICAL VARIABLES - INPUT
	/////////////////////////////////////////////
	
	/////////////////////////////////////////////
	// ECOLOGICAL VARIABLES - DEFAULT
	/////////////////////////////////////////////
	
	/////////////////////////////////////////////
	// OUTPUT
	/////////////////////////////////////////////
	
	
	@Description("The latent heat.")
	@Unit("mm h-1")
	@Out
	public double totalTranspiration; 
	
	@Description("The latent heat.")
	@Unit("mm h-1")
	@Out
	public HashMap<Integer, double[]> outLatentHeat;
	
	@Description("The transpirated water.")
	@Unit("mm h-1")
	@Out
	public HashMap<Integer, double[]> outTranspiration;
	
	@Description("The sensible heat.")
	@Unit("W m-2")
	@Out
	public HashMap<Integer, double[]> outSensibleHeat;
	
	@Description("The leaf Temperature.")
	@Unit("K")
	@Out
	public HashMap<Integer, double[]> outLeafTemperature;
	
	@Description("The solar radiation absorbed by the sunlit canopy.")
	@Unit("W m-2")
	@Out
	public HashMap<Integer, double[]> outRadiation;
		
	@Description("Fraction of highlighted canopy.")
	@Unit("-")
	@Out
	public HashMap<Integer, double[]> outCanopy;
	
	@Description("Evaporation from soil.")
	@Unit("W m-2")
	@Out
	public HashMap<Integer, double[]> outEvaporation;
	
	//public double longitude;
	/////////////////////////////////////////////
	// OTHERS - DO
	/////////////////////////////////////////////
	@In
	public HashMap<Integer, double[]> inStressSun;
	//public double inStressSun;

	@In
	//public HashMap<Integer, double[]> inStressSun;
	public double defaultStress;
	
	
	@Description("Switch that defines if it is hourly.")
	@In
	public boolean doHourly = true;
	
	@Description("Define the closure of energy budget: single or full iteration.")
	@In
	public boolean doIterative = true;
	
	@Description("Choose if you want to print only the latent heat or all the other outputs.")
	@In
	public boolean doFullPrint = true;
	
	//@Description("Choose if you want to use the multi-layer canopy model or a simple transpiring surface.")
	//@In
	//public boolean doMultiLayer = true;

	@In
	String printo;
	// METHODS FROM CLASSES		
	SensibleHeatMethods sensibleHeat 	= new SensibleHeatMethods();
	LatentHeatMethods latentHeat 		= new LatentHeatMethods();
	PressureMethods pressure 			= new PressureMethods(); 
	RadiationMethod radiationMethods 	= new RadiationMethod();
	SolarGeometry solarGeometry 		= new SolarGeometry();
	EnvironmentalStress environmentalStress	= new EnvironmentalStress();
	

	

	
	@Execute
	public void process() throws Exception {
		if (doHourly == true) {
			time =temporalStep*60;

			} else {
			time = 86400;
			}
		DateTime startDateTime = formatter.parseDateTime(tStartDate);
		DateTime date=(doHourly==false)?startDateTime.plusDays(step).plusHours(12):startDateTime.plusMinutes(temporalStep*step);
	//	longitude = (idPoint[0].getX());
		latitude = Math.toRadians(latitude);
		//stationCoordinates = getCoordinate(0,inCentroids, idCentroids);
	//	Iterator<Integer> idIterator = stationCoordinates.keySet().iterator();
		//CoordinateReferenceSystem sourceCRS = inDem.getCoordinateReferenceSystem2D();

		Leaf propertyOfLeaf = new Leaf();
		double poreRadius = propertyOfLeaf.poreRadius;
		double poreArea = propertyOfLeaf.poreArea;
		double poreDepth = propertyOfLeaf.poreDepth;
		double poreDensity = propertyOfLeaf.poreDensity;
		double leafLength = propertyOfLeaf.length;
		int leafSide = propertyOfLeaf.side;
		int leafStomaSide = propertyOfLeaf.stomaSide;
		double longWaveEmittance = propertyOfLeaf.longWaveEmittance;
		
		//outLatentHeatShade 	= new HashMap<Integer, double[]>();
		outLatentHeat		= new HashMap<Integer, double[]>();
		outTranspiration 	= new HashMap<Integer, double[]>();
		if (doFullPrint == true) {
			outLeafTemperature 		= new HashMap<Integer, double[]>();
			outRadiation 			= new HashMap<Integer, double[]>();
		//	outRadiationShade 		= new HashMap<Integer, double[]>();
			outSensibleHeat 		= new HashMap<Integer, double[]>();
		//	outSensibleHeatShade 	= new HashMap<Integer, double[]>();
		//	outRadiationShade 		= new HashMap<Integer, double[]>();
		//	outLeafTemperatureShade	= new HashMap<Integer, double[]>();
			outCanopy 				= new HashMap<Integer, double[]>();
			outEvaporation			= new HashMap<Integer, double[]>();
			}
		
		Set<Entry<Integer, double[]>> entrySet = inAirTemperature.entrySet();
		for( Entry<Integer, double[]> entry : entrySet ) {
			Integer ID = entry.getKey();
			
		//	Coordinate coordinate = (Coordinate) stationCoordinates.get(idIterator.next());
		////	Point [] idPoint=getPoint(coordinate,sourceCRS, targetCRS);
		//	elevation = coordinate.z;
		//	longitude = (idPoint[0].getX());
		//	latitude = Math.toRadians(idPoint[0].getY());
		
			//double solarElevationAngle = solarGeometry.getSolarElevationAngle(date, latitude,longitude, doHourly);
			
			/////////////////////////////////////////////
			// INPUT READER
			/////////////////////////////////////////////
				
			double airTemperature = inAirTemperature.get(ID)[0]+273.0;
			if (airTemperature == (nullValue+273.0)) {airTemperature = defaultAirTemperature;}		
			/*double leafTemperatureSun = airTemperature;
			double leafTemperatureShade = airTemperature;
			double leafTemperatureSoil = airTemperature;*/
			
			double leafAreaIndex = defaultLeafAreaIndex;
			if (inLeafAreaIndex != null)				
				leafAreaIndex = inLeafAreaIndex.get(ID)[0];
			if (leafAreaIndex == nullValue) {leafAreaIndex = defaultLeafAreaIndex;}
			
			
		//	if (leafAreaIndex != 0) {	
										
				double shortWaveRadiationDirect = inShortWaveRadiationDirect.get(ID)[0];
				if (shortWaveRadiationDirect == nullValue) {shortWaveRadiationDirect = defaultShortWaveRadiationDirect;}
				
				double shortWaveRadiationDiffuse = inShortWaveRadiationDiffuse.get(ID)[0];
				if (shortWaveRadiationDiffuse == nullValue) {shortWaveRadiationDiffuse = 0.159*shortWaveRadiationDirect;} 						
				
				double longWaveRadiation = inLongWaveRadiation.get(ID)[0];
				if (longWaveRadiation == nullValue) {longWaveRadiation = longWaveEmittance * stefanBoltzmannConstant * pow (airTemperature, 4);}//defaultLongWaveRadiation;}	
				longWaveRadiation = longWaveEmittance * stefanBoltzmannConstant * pow (airTemperature, 4);
				
				double windVelocity = defaultWindVelocity;
				if (inWindVelocity != null){windVelocity = inWindVelocity.get(ID)[0];}
				if (windVelocity == nullValue) {windVelocity = defaultWindVelocity;}
				if (windVelocity == 0.0) {windVelocity = defaultWindVelocity;}			
				
				double atmosphericPressure = 101325;
				if (inAtmosphericPressure != null){atmosphericPressure = inAtmosphericPressure.get(ID)[0];}
				if (atmosphericPressure == nullValue) {atmosphericPressure = pressure.computePressure(defaultAtmosphericPressure, massAirMolecule, gravityConstant, elevation,boltzmannConstant, airTemperature);;}			
				
				double relativeHumidity = defaultRelativeHumidity;
				if (inRelativeHumidity != null){relativeHumidity = inRelativeHumidity.get(ID)[0];}
				if (relativeHumidity == nullValue) {relativeHumidity = defaultRelativeHumidity;}				
				
				double soilFlux = defaultSoilFlux;
				if (inSoilFlux != null){soilFlux = inSoilFlux.get(ID)[0];}
				if (soilFlux == nullValue) {soilFlux = defaultSoilFlux;}
				
				double soilMoisture = defaultSoilMoisture;
				if (inSoilMoisture != null){soilMoisture = inSoilMoisture.get(ID)[0];}
				if (soilMoisture == nullValue) {soilMoisture = defaultSoilMoisture;}
				

			/*	double stressSun = defaultStress;
				if (inStressSun != null){stressSun =  inStressSun.get(ID)[0];}	//resistance.outStress;//.get(ID)[0];} //

				double stressShade = defaultStress;
				if (inStressShade != null){stressShade = inStressShade.get(ID)[0];}	*/

				// Compute the saturation pressure
				double saturationVaporPressure = pressure.computeSaturationVaporPressure(airTemperature, waterMolarMass, latentHeatEvaporation, molarGasConstant);			
				// Compute the actual vapour pressure
				double vaporPressure = pressure.computeVaporPressure(relativeHumidity, saturationVaporPressure);		
				// Compute the delta
				double delta = pressure.computeDelta(airTemperature, waterMolarMass, latentHeatEvaporation, molarGasConstant);			
				// Compute the convective transfer coefficient - hc
				double convectiveTransferCoefficient = sensibleHeat.computeConvectiveTransferCoefficient(airTemperature, windVelocity, leafLength, criticalReynoldsNumber, prandtlNumber);
				// Compute the sensible transfer coefficient - cH
				double sensibleHeatTransferCoefficient = sensibleHeat.computeSensibleHeatTransferCoefficient(convectiveTransferCoefficient, leafSide);
				// Compute the latent transfer coefficient - cE
				double latentHeatTransferCoefficient = latentHeat.computeLatentHeatTransferCoefficient(airTemperature, atmosphericPressure, leafStomaSide, convectiveTransferCoefficient, airSpecificHeat,
						airDensity, molarGasConstant, molarVolume, waterMolarMass, latentHeatEvaporation, poreDensity, poreArea, poreDepth, poreRadius);			

				
				// RADIATION
				double solarElevationAngle = solarGeometry.getSolarElevationAngle(date, latitude,longitude, doHourly, time);
				double shortwaveCanopySun = radiationMethods.computeAbsordebRadiationSunlit(leafAreaIndex, solarElevationAngle, shortWaveRadiationDirect, shortWaveRadiationDiffuse);
				shortwaveCanopySun = ((solarElevationAngle>0)?shortwaveCanopySun:0);
	
			/*	double shortwaveCanopyShade = radiationMethods.computeAbsordebRadiationShadow(leafAreaIndex, solarElevationAngle, shortWaveRadiationDirect, shortWaveRadiationDiffuse);
				shortwaveCanopyShade = ((solarElevationAngle>0)?shortwaveCanopyShade:0);*/

				double incidentSolarRadiationSoil = shortWaveRadiationDirect + shortWaveRadiationDiffuse - shortwaveCanopySun;// - shortwaveCanopyShade;
				solarElevationAngle =(solarElevationAngle<0)?0:solarElevationAngle;
				
				// WIND
				WindProfile windVelocityProfile = new WindProfile();
				double windInCanopy = windVelocityProfile.computeWindProfile(windVelocity, canopyHeight);
				double windSoil = windVelocityProfile.computeWindProfile(windVelocity, 0.1);
				
				// LAYER SOIL
	            double evaporation = computeEvaporation(incidentSolarRadiationSoil, windSoil, airTemperature, relativeHumidity, atmosphericPressure, soilFlux);
	            evaporation=(evaporation<0)?0:evaporation;//evaporation)*(time/latentHeatEvaporation);
	            

				////////////////////////////////////////
				////////////////////////////////////////
				///////////////  SUN LAYER  ////////////
				////////////////////////////////////////
				////////////////////////////////////////	       
	            
	        	// FIRST ITERATION ENERGY BALANCE SUN
				// Initialization of the residual of the energy balance
	            double energyBalanceResidualSun = 0;
								
				double vaporPressureDew = pressure.computeVapourPressureDewPoint(airTemperature);		
				double vapourPressureDeficit = pressure.computeVapourPressureDeficit(vaporPressure, vaporPressureDew);


	            double stressRadiationSun = environmentalStress.computeRadiationStress(shortwaveCanopySun, alpha, theta);
	          //  double stressRadiationShade = environmentalStress.computeRadiationStress(shortwaveCanopyShade, alpha, theta);

	            double stressTemperature = environmentalStress.computeTemperatureStress(airTemperature, Tl, Th, T0);
	            double stressVPD = environmentalStress.computeVapourPressureStress(vapourPressureDeficit, VPD0);
	            double stressWater = environmentalStress.computeFAOWaterStress(soilMoisture, waterFieldCapacity, waterWiltingPoint, rootsDepth, depletionFraction);
	            
	            double stressSun = stressRadiationSun;// * stressTemperature * stressWater * stressVPD;
	         //   System.out.println("stress sun     "+ stressSun);
	        //    double stressShade = stressRadiationShade;// * stressTemperature * stressWater * stressVPD;
	          //  System.out.println("stress shade     "+ stressShade);

				// Compute the area in sunlight
	            double areaCanopySun = leafAreaIndex;//radiationMethods.computeSunlitLeafAreaIndex(leafAreaIndex, solarElevationAngle);
				// Compute the leaf temperature in sunlight				
	            double leafTemperatureSun =  computeSurfaceTemperature(shortwaveCanopySun, energyBalanceResidualSun, sensibleHeatTransferCoefficient,airTemperature,
						areaCanopySun,stressSun,latentHeatTransferCoefficient,delta,vaporPressure,saturationVaporPressure,leafSide,longWaveRadiation);
				// Compute the net longwave radiation in sunlight
				double netLongWaveRadiationSun = areaCanopySun*radiationMethods.computeLongWaveRadiationBalance(leafSide, longWaveEmittance, airTemperature, leafTemperatureSun, stefanBoltzmannConstant);
				// Compute the latent heat flux from the sunlight area
				double latentHeatFluxSun 	= stressSun*areaCanopySun*latentHeat.computeLatentHeatFlux(delta,  leafTemperatureSun,  airTemperature,  
						latentHeatTransferCoefficient,sensibleHeatTransferCoefficient,  vaporPressure,  saturationVaporPressure);
				// Compute the sensible heat flux from the sunlight area
				double sensibleHeatFluxSun = areaCanopySun*sensibleHeat.computeSensibleHeatFlux(sensibleHeatTransferCoefficient, leafTemperatureSun, airTemperature);
				
				// Compute the residual of the energy balance for the sunlight area								
				energyBalanceResidualSun = computeEnergyBalance(shortwaveCanopySun, energyBalanceResidualSun, netLongWaveRadiationSun, latentHeatFluxSun, sensibleHeatFluxSun);
				//System.out.println(energyBalanceResidualSun);
				// Solve it in iterative way
				if (doIterative == true) {
					double iteration = 0;
					while (Math.abs(energyBalanceResidualSun) > 10) {
						leafTemperatureSun =  computeSurfaceTemperature(shortwaveCanopySun, energyBalanceResidualSun, sensibleHeatTransferCoefficient,airTemperature,
								areaCanopySun,stressSun,latentHeatTransferCoefficient,delta,vaporPressure,saturationVaporPressure,leafSide,longWaveRadiation);
						
						netLongWaveRadiationSun = areaCanopySun*radiationMethods.computeLongWaveRadiationBalance(leafSide, longWaveEmittance, airTemperature, leafTemperatureSun, stefanBoltzmannConstant);
						
						latentHeatFluxSun 	= stressSun*areaCanopySun*latentHeat.computeLatentHeatFlux(delta,  leafTemperatureSun,  airTemperature,  latentHeatTransferCoefficient,
								sensibleHeatTransferCoefficient,  vaporPressure,  saturationVaporPressure);
						
						sensibleHeatFluxSun = areaCanopySun*sensibleHeat.computeSensibleHeatFlux(sensibleHeatTransferCoefficient, leafTemperatureSun, airTemperature);
						
						energyBalanceResidualSun = computeEnergyBalance(shortwaveCanopySun, energyBalanceResidualSun, netLongWaveRadiationSun, latentHeatFluxSun, sensibleHeatFluxSun);
						iteration++;

					}
				}
				////////////////////////////////////////
				////////////////////////////////////////
				//////////////  SHADE LAYER  ///////////
				////////////////////////////////////////
				////////////////////////////////////////
				
				
				// FIRST ITERATION ENERGY BALANCE SHADE
				// Initialization of the residual of the energy balance
	        /*    double energyBalanceResidualShade = 0;

				// Compute the area in shadow
				double areaCanopyShade = leafAreaIndex - areaCanopySun;				
				// Compute the leaf temperature in shadow
				double leafTemperatureShade =  computeSurfaceTemperature(shortwaveCanopyShade, energyBalanceResidualShade, sensibleHeatTransferCoefficient,airTemperature,
						areaCanopyShade,stressShade,latentHeatTransferCoefficient,delta,vaporPressure,saturationVaporPressure,leafSide,longWaveRadiation);
				// Compute the net longwave radiation in shade
				double netLongWaveRadiationShade = radiationMethods.computeLongWaveRadiationBalance(leafSide, longWaveEmittance, airTemperature, leafTemperatureShade, stefanBoltzmannConstant);
				// Compute the latent heat flux from the shaded area
				double latentHeatFluxShade 	= latentHeat.computeLatentHeatFlux(delta,  leafTemperatureShade,  airTemperature,  latentHeatTransferCoefficient,
						sensibleHeatTransferCoefficient,  vaporPressure,  saturationVaporPressure);
				// Compute the sensible heat flux from the shaded area				
				double sensibleHeatFluxShade = sensibleHeat.computeSensibleHeatFlux(sensibleHeatTransferCoefficient, leafTemperatureShade, airTemperature);
				
				// Compute the residual of the energy balance for the shaded area				
				energyBalanceResidualShade = computeEnergyBalance(shortwaveCanopyShade, energyBalanceResidualShade, netLongWaveRadiationShade, latentHeatFluxShade, sensibleHeatFluxShade);

				if (doIterative == true) {
					double iteration = 0;
					while (Math.abs(energyBalanceResidualShade) > 10) {
						leafTemperatureShade =  computeSurfaceTemperature(shortwaveCanopyShade, energyBalanceResidualShade, sensibleHeatTransferCoefficient,airTemperature,
								areaCanopyShade,stressShade,latentHeatTransferCoefficient,delta,vaporPressure,saturationVaporPressure,leafSide,longWaveRadiation);
						
						netLongWaveRadiationShade = areaCanopyShade*radiationMethods.computeLongWaveRadiationBalance(leafSide, longWaveEmittance, airTemperature, leafTemperatureShade, stefanBoltzmannConstant);
						
						latentHeatFluxShade 	= stressShade*areaCanopyShade*latentHeat.computeLatentHeatFlux(delta,  leafTemperatureShade,  airTemperature,  latentHeatTransferCoefficient,
								sensibleHeatTransferCoefficient,  vaporPressure,  saturationVaporPressure);
						
						sensibleHeatFluxShade = areaCanopyShade*sensibleHeat.computeSensibleHeatFlux(sensibleHeatTransferCoefficient, leafTemperatureShade, airTemperature);
						
						energyBalanceResidualShade = computeEnergyBalance(shortwaveCanopyShade, energyBalanceResidualShade, netLongWaveRadiationShade, latentHeatFluxShade, sensibleHeatFluxShade);
						iteration++;
					//	System.out.println("Actual iteration2	"+iteration);
				//		System.out.println("initial residual2	"+energyBalanceResidualShade);


					}
				}*/				
				latentHeatFluxSun=(latentHeatFluxSun<0)?0:latentHeatFluxSun;
				totalTranspiration = (latentHeatFluxSun+evaporation);//*(time/latentHeatEvaporation);  //				
				
				if (doFullPrint == true) {			
					storeResultFull((Integer)ID, latentHeatFluxSun, totalTranspiration, sensibleHeatFluxSun,
							leafTemperatureSun,shortwaveCanopySun, areaCanopySun,evaporation);
				}
			else {
				storeResult((Integer)ID,latentHeatFluxSun, totalTranspiration);
				}
			}
		step++;	
			}
		


	private void storeResultFull(int ID,double latentHeatSun, 
			double totalTranspiration, 
			double sensibleHeatFluxLight, 
			double leafTemperatureSun, 
			double radiationCanopyInLight, 
			double leafInSunlight, double evaporation) 
			throws SchemaException {		
		
		outLatentHeat.put(ID, new double[]{latentHeatSun});
		//outLatentHeatShade.put(ID, new double[]{latentHeatShadow});
		outTranspiration.put(ID, new double[]{totalTranspiration});
		
		outSensibleHeat.put(ID, new double[]{sensibleHeatFluxLight});
		//outSensibleHeatShade.put(ID, new double[]{sensibleHeatFluxShadow});

		outLeafTemperature.put(ID, new double[]{leafTemperatureSun});
		//outLeafTemperatureShade.put(ID, new double[]{leafTemperatureShadow});

		outRadiation.put(ID, new double[]{radiationCanopyInLight});
	//	outRadiationShade.put(ID, new double[]{radiationCanopyInShadow});
		outCanopy.put(ID, new double[]{leafInSunlight});
		
		outEvaporation.put(ID, new double[]{evaporation});
		}
	private void storeResult(int ID,double latentHeatSun, double totalTranspiration) 
			throws SchemaException {
		outLatentHeat.put(ID, new double[]{latentHeatSun});
	//	outLatentHeatShade.put(ID, new double[]{latentHeatShadow});
		outTranspiration.put(ID, new double[]{totalTranspiration});
		}
	
	/*private Point[] getPoint(Coordinate coordinate, CoordinateReferenceSystem sourceCRS, CoordinateReferenceSystem targetCRS)
			throws Exception{
		Point[] point = new Point[] { GeometryUtilities.gf().createPoint(coordinate) };
		CrsUtilities.reproject(sourceCRS, targetCRS, point);
		return point;
	}*/
	private double computeEvaporation( double netRadiation, double windVelocity, double airTemperature, double relativeHumidity, 
    		double atmosphericPressure, double soilHeatFlux) {
		netRadiation = netRadiation * 86400/1E6;
		atmosphericPressure = atmosphericPressure/1000;
		airTemperature = airTemperature-273.15;
        // Computation of Delta [KPa °C-1]
        double denDelta = Math.pow((airTemperature + 237.3), 2);
        double expDelta = (17.27 * airTemperature) / (airTemperature + 237.3);
        double numDelta = 4098 * (0.6108 * Math.exp(expDelta));
        double delta = numDelta / denDelta;
        // Computation of Psicrometric constant [kPa °C-1]
        double psychrometricConstant = 0.665 * 0.001 * atmosphericPressure;
        // Computation of mean saturation vapour pressure [kPa]
        double saturationVaporPressure = 0.6108 * Math.exp(expDelta);
        // Computation of average hourly actual vapour pressure [kPa]
        double vaporPressure = saturationVaporPressure * relativeHumidity / 100;
        // Computation of ET [mm day-1]
        double num = 0.408 * delta * (netRadiation - soilHeatFlux) + (900 * psychrometricConstant * windVelocity * (saturationVaporPressure - vaporPressure)) / (airTemperature + 273);
        double den = delta + psychrometricConstant * (1 + 0.34 * windVelocity);
        double result = (num / den);//*(2.45*1E6)/86400;
        result = (result <0)?0:result;
        return result;
    }
    public double computeSurfaceTemperature(
    double shortWaveRadiation,
    double residual,
    double sensibleHeatTransferCoefficient,
    double airTemperature,
    double surfaceArea,
	double stress,
	double latentHeatTransferCoefficient,
	double delta,
	double vaporPressure,
	double saturationVaporPressure,
	int	side,
	double longWaveRadiation){
	double surfaceTemperature1 = (shortWaveRadiation - residual + sensibleHeatTransferCoefficient*airTemperature*surfaceArea +
						stress * latentHeatTransferCoefficient*(delta*airTemperature + vaporPressure - saturationVaporPressure)*surfaceArea  +
						side * longWaveRadiation * 4 *surfaceArea);
	double surfaceTemperature2 =(1/(sensibleHeatTransferCoefficient*surfaceArea +
			stress*latentHeatTransferCoefficient * delta *surfaceArea +
			side * longWaveRadiation/airTemperature * 4*surfaceArea));
	double surfaceTemperature = surfaceTemperature1*surfaceTemperature2;
	return surfaceTemperature;	
	}
    public double computeEnergyBalance(double shortWaveRadiation,double residual,
    		double longWaveRadiation,double latentHeatFlux,double sensibleHeatFlux){
    		double energyResidual = shortWaveRadiation - residual - longWaveRadiation - latentHeatFlux - sensibleHeatFlux;
    		return energyResidual;	
    }
	
		
}
