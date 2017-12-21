package etp;
import static java.lang.Math.exp;
import static java.lang.Math.pow;
import static java.lang.Math.abs;
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

import org.jgrasstools.gears.libs.modules.JGTModel;
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
@Description("Calculates evapotranspiration at daily timestep using Schimanski & Or equation")
@Author(name = "Michele Bottazzi", contact = "michele.bottazzi@gmail.com")
@Keywords("Evapotranspiration, Hydrology")
@Label("")
@Name("")
@Status(Status.CERTIFIED)
@License("General Public License Version 3 (GPLv3)")
public class OmsSchymanskiOrET extends JGTModel {
	@Description("Air temperature.")
	@In
	@Unit("K")
	public HashMap<Integer, double[]> inAirTemperature;
	@Description("The air temperature default value in case of missing data.")
	@In
	@Unit("K")
	public double defaultAirTemperature = 15.0+273.15;
	
	@Description("The wind speed.")
	@In
	@Unit("km hr-1")
	public HashMap<Integer, double[]> inWindVelocity;
	@Description("The wind default value in case of missing data.")
	@In
	@Unit("km hr-1")
	public double defaultWindVelocity = 5.0;
	
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
	public double defaultShortWaveRadiation = 30.0;
	
	@Description("The long wave radiation at the surface.")
	@In
	@Unit("W m-2")
	public HashMap<Integer, double[]> inLongWaveRadiation;
//	@Description("The long wave radiation default value in case of missing data.")
//	@In
//	@Unit("W m-2")
	//public double defaultLongWaveRadiation = 6.0;
	
	@Description("The atmospheric pressure.")
	@In
	@Unit("hPa")
	public HashMap<Integer, double[]> inAtmosphericPressure;
	@Description("The atmospheric pressure default value in case of missing data.")
	@In
	@Unit("hPa")
	public double defaultAtmosphericPressure = 100.0;
	
	@Description("The soilflux.")
	@In
	@Unit("W m-2")
	public HashMap<Integer, double[]> inSoilFlux;
	@Description("The soilflux default value in case of missing data.")
	@In
	@Unit("W m-2")
	public double defaultSoilFlux = 0.0;
	
	double waterMolarMass = 0.018;
	double latentHeatEvaporation = 2.45 * pow(10,6);
	double molarGasConstant = 8.314472;
	double nullValue = -9999.0;
	double stefanBoltzmannConstant = 5.670373 * pow(10,-8);
	
	// TODO Add the elevation value in case of missing P data
	@Description("The reference evapotranspiration.")
	@Unit("mm day-1")
	@Out
	public HashMap<Integer, double[]> outSOEt;
	@Execute
	public void process() throws Exception {
		outSOEt = new HashMap<Integer, double[]>();
		Set<Entry<Integer, double[]>> entrySet = inAirTemperature.entrySet();
		for( Entry<Integer, double[]> entry : entrySet ) {
			Integer basinId = entry.getKey();    
			double relativeHumidity = inRelativeHumidity.get(basinId)[0];
			if (relativeHumidity == nullValue) {relativeHumidity = defaultRelativeHumidity;}
			
			double airTemperature = inAirTemperature.get(basinId)[0]+273.0;
			
			if (airTemperature == (nullValue+273.0)) {airTemperature = defaultAirTemperature;}		
			
			Leaf leaf = new Leaf();
			leaf.length = 0.05;	leaf.side = 2;	leaf.emissivity = 1.0;	leaf.temperature = airTemperature + 2.0;
			double leafTemperature = leaf.temperature;   
			
			double shortWaveRadiation = inShortWaveRadiation.get(basinId)[0];
			if (shortWaveRadiation == nullValue) {shortWaveRadiation = defaultShortWaveRadiation;}    
			
			double longWaveRadiation = inLongWaveRadiation.get(basinId)[0];
			if (longWaveRadiation == nullValue) {longWaveRadiation = 1 * stefanBoltzmannConstant * pow (airTemperature, 4);}//defaultLongWaveRadiation;}
			
			double windVelocity = inWindVelocity.get(basinId)[0];
			if (windVelocity == nullValue) {windVelocity = defaultWindVelocity;}   
			
			double atmosphericPressure = inAtmosphericPressure.get(basinId)[0];
			if (atmosphericPressure == nullValue) {atmosphericPressure = defaultAtmosphericPressure;}		
			
			double saturationVaporPressure = computeSaturationVaporPressure(airTemperature);
			double vaporPressure = relativeHumidity * saturationVaporPressure/100.0;
			
			double delta = computeDelta (airTemperature);
			SensibleHeatTransferCoefficient cH = new SensibleHeatTransferCoefficient();
			LatentHeatTransferCoefficient cE = new LatentHeatTransferCoefficient();
			
			double convectiveTransferCoefficient = cH.computeConvectiveTransferCoefficient(airTemperature, windVelocity, leaf.length);
			double sensibleHeatTransferCoefficient = cH.computeSensibleHeatTransferCoefficient(convectiveTransferCoefficient, leaf.side);
			double latentHeatTransferCoefficient = cE.computeLatentHeatTransferCoefficient(airTemperature, atmosphericPressure, leaf.side, convectiveTransferCoefficient);
			
			double residual = 1.0;
			double latentHeatFlux = 0;
			double sensibleHeatFlux = 0;
			double netLongWaveRadiation = 0;
			while(abs(residual) > pow(10,-1)) 
				{
				sensibleHeatFlux = computeSensibleHeatFlux(sensibleHeatTransferCoefficient, leafTemperature, airTemperature);
				latentHeatFlux = computeLatentHeatFlux(delta, leafTemperature, airTemperature, latentHeatTransferCoefficient, sensibleHeatTransferCoefficient, vaporPressure, saturationVaporPressure);
				netLongWaveRadiation = computeNetLongWaveRadiation(leaf.side,leaf.emissivity, airTemperature, leafTemperature);
				residual = (shortWaveRadiation - netLongWaveRadiation) - sensibleHeatFlux - latentHeatFlux;
				leafTemperature = computeLeafTemperature(leaf.side, leaf.emissivity,sensibleHeatTransferCoefficient,latentHeatTransferCoefficient,airTemperature,shortWaveRadiation,longWaveRadiation,vaporPressure, saturationVaporPressure,delta);
				}
			outSOEt.put(basinId, new double[]{latentHeatFlux});
			}
		}
	private double computeSaturationVaporPressure(double airTemperature) {
		double saturationVaporPressure = 611.0 * exp((waterMolarMass*latentHeatEvaporation/molarGasConstant)*((1.0/273.0)-(1.0/airTemperature)));
		return saturationVaporPressure;
	}
	private double computeDelta (double airTemperature) {
		double numerator = 611 * waterMolarMass * latentHeatEvaporation;
		double exponential = exp((waterMolarMass * latentHeatEvaporation / molarGasConstant)*((1/273.0)-(1/airTemperature)));
		double denominator = (molarGasConstant * pow(airTemperature,2));
		double delta = numerator * exponential / denominator;
		return delta;
	}
//	private double computeLongWaveRadiation(double side, double emissivity, double Temperature) {
//		double longWaveRadiation = 4 * side * emissivity * stefanBoltzmannConstant * (pow (Temperature, 4));
//		return longWaveRadiation;	
//	}
	private double computeNetLongWaveRadiation(double side, double emissivity, double airTemperature, double leafTemperature) {
		double longWaveRadiation = 4 * side * emissivity * stefanBoltzmannConstant * (((pow (airTemperature, 3))*leafTemperature - (pow (airTemperature, 4))));
		return longWaveRadiation;	
	}
	private double computeLatentHeatFlux(double delta, double leafTemperature, double airTemperature, double latentHeatTransferCoefficient,double sensibleHeatTransferCoefficient, double vaporPressure, double saturationVaporPressure) {
		double latentHeatFlux = sensibleHeatTransferCoefficient* (delta * (leafTemperature - airTemperature) + saturationVaporPressure - vaporPressure)/(sensibleHeatTransferCoefficient/latentHeatTransferCoefficient);
		return latentHeatFlux;	
	}
	private double computeSensibleHeatFlux(double sensibleHeatTransferCoefficient, double leafTemperature, double airTemperature) {
		double sensibleHeatFlux = sensibleHeatTransferCoefficient * (leafTemperature - airTemperature);
		return sensibleHeatFlux;	
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
				side * emissivity * stefanBoltzmannConstant * 4 * pow(airTemperature,4))*
				(1/(sensibleHeatTransferCoefficient + latentHeatTransferCoefficient * delta +	
				side * emissivity * stefanBoltzmannConstant * 4 * pow(airTemperature,3)));
		return leafTemperature;	
	}
}
