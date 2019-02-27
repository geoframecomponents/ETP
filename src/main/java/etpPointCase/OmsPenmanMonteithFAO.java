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
package etpPointCase;

import static java.lang.Math.pow;

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

import org.jgrasstools.gears.libs.modules.JGTConstants;
import org.jgrasstools.gears.libs.modules.JGTModel;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;

@Description("Calculates evapotranspiration at hourly timestep using FAO Penman-Monteith equation")
@Author(name = "Giuseppe Formetta, Silvia Franceschi and Andrea Antonello", contact = "maryban@hotmail.it")
@Keywords("Evapotranspiration, Hydrology")
@Label("")
@Name("")
@Status(Status.CERTIFIED)
@License("General Public License Version 3 (GPLv3)")

public class OmsPenmanMonteithFAO extends JGTModel {

    @Description("The net Radiation at the grass surface in W/m2 for the current hour.")
    @In
    @Unit("MJ m-2 hour-1")
    public HashMap<Integer, double[]> inNetRadiation;

    @Description("The net Radiation default value in case of missing data.")
    @In
    @Unit("MJ m-2 hour-1")
    public double defaultNetRadiation = 2.0;

    @Description("The average hourly wind speed.")
    @In
    @Unit("m s-1")
    public HashMap<Integer, double[]> inWindVelocity;

    @Description("The wind default value in case of missing data.")
    @In
    @Unit("m s-1")
    public double defaultWindVelocity = 0.5;

    @Description("The mean hourly air temperature.")
    @In
    @Unit("C")
    public HashMap<Integer, double[]> inAirTemperature;

    @Description("The temperature default value in case of missing data.")
    @In
    @Unit("C")
    public double defaultAirTemperature = 15.0;

    @Description("The average air hourly relative humidity.")
    @In
    @Unit("%")
    public HashMap<Integer, double[]> inRelativeHumidity;

    @Description("The humidity default value in case of missing data.")
    @In
    @Unit("%")
    public double defaultRelativeHumidity = 70.0;

    @Description("The atmospheric pressure in kPa.")
    @In
    @Unit("KPa")
    public HashMap<Integer, double[]> inAtmosphericPressure;

    @Description("The pressure default value in case of missing data.")
    @In
    @Unit("KPa")
    public double defaultAtmosphericPressure;
    
	@Description("The soilflux.")
	@In
	@Unit("W m-2")
	public HashMap<Integer, double[]> inSoilFlux;
    
	@Description("The soilflux default value in case of missing data.")
	@In
	@Unit("W m-2")
	public double defaultSoilFlux = 0.0;

    @Description("The reference evapotranspiration.")
    @Unit("mm hour-1")
    @Out
    public HashMap<Integer, double[]> outEvapotranspirationFao;
    
    @Description("The latent heat.")
    @Unit("W m-2")
    @Out
    public HashMap<Integer, double[]> outLatentHeatFao;
        
	@Description("Switch that defines if it is hourly.")
	@In
	public boolean doHourly;
	
	@Description("The mean hourly air temperature.")
	@In
	public String tStartDate;
	
	@Description("The first day of the simulation.")
	@In
	public int temporalStep;
	
	@In
	public HashMap<Integer, double[]> inStress;

	@Description("The crop coefficient.")
	@Unit("[-]")
	@In
	public double cropCoefficient;
	
	@Description("the water content at wilting point.")
	@Unit("[m3 m-3]")
	@In
	public double waterWiltingPoint;
	
	@Description("the water content at field capacity.")
	@Unit("[m3 m-3]")
	@In
	public double waterFieldCapacity;
	
	@Description("the rooting depth.")
	@Unit("[m]")
	@In
	public double rootsDepth;
	
	@Description("average fraction of Total Available Soil Water (TAW) that can be depleted from the root zone before moisture stress (reduction in ET) occurs [0-1].")
	@In
	public double depletionFraction;
	
	@Description("The soil mosture.")
	@Unit("[m3 m-3]")
	@In
	public HashMap<Integer, double[]> inSoilMosture;
	
	@Description("The default value of soil mosture.")
	@Unit("[m3 m-3]")
	@In
	public double defaultSoilMosture = 0.3;
	
	int step;
	public int time;
	
    double nullValue = -9999;
	double latentHeatEvaporation = 2.45*pow(10,6);

	private DateTimeFormatter formatter = JGTConstants.utcDateFormatterYYYYMMDDHHMM;

    @Execute
    public void process() throws Exception {
    	outEvapotranspirationFao = new HashMap<Integer, double[]>();
    	outLatentHeatFao = new HashMap<Integer, double[]>();
        
        if (doHourly == true) {
			time =temporalStep*60;

			} else {
			time = 86400;
			}
        double totalAvailableWater = 1000*(waterFieldCapacity - waterWiltingPoint)*rootsDepth;
        double readilyAvailableWater = totalAvailableWater * depletionFraction;
		DateTime startDateTime = formatter.parseDateTime(tStartDate);
		DateTime date=(doHourly==false)?startDateTime.plusDays(step).plusHours(12):startDateTime.plusMinutes(temporalStep*step);
        Set<Entry<Integer, double[]>> entrySet = inAirTemperature.entrySet();
		for( Entry<Integer, double[]> entry : entrySet ) {
            Integer basinId = entry.getKey();

            double airTemperature = inAirTemperature.get(basinId)[0];
			if (airTemperature == (nullValue)) {airTemperature = defaultAirTemperature;}		
			  	
			double netRadiation = inNetRadiation.get(basinId)[0];
			if (netRadiation == (nullValue)) {netRadiation = defaultNetRadiation;}
			netRadiation = netRadiation * 86400/1E6;

			double windVelocity = inWindVelocity.get(basinId)[0];
			if (windVelocity == (nullValue)) {windVelocity = defaultWindVelocity;}		
			
			double atmosphericPressure = inAtmosphericPressure.get(basinId)[0]/1000;
			if (atmosphericPressure == (nullValue/1000)) {atmosphericPressure = defaultAtmosphericPressure;}		

			double relativeHumidity = inRelativeHumidity.get(basinId)[0];
			if (relativeHumidity == (nullValue)) {relativeHumidity = defaultRelativeHumidity;}	

			double soilMosture = inSoilMosture.get(basinId)[0];
			if (soilMosture == (nullValue)) {soilMosture = defaultSoilMosture;}
			
			double soilFlux = defaultSoilFlux;
			if (inSoilFlux != null){soilFlux = inSoilFlux.get(basinId)[0];}
			if (soilFlux == nullValue) {soilFlux = defaultSoilFlux;}
			soilFlux = soilFlux * 86400/1E6;

			double rootZoneDepletation = 1000 * (waterFieldCapacity - soilMosture) * rootsDepth;

			double waterStressCoefficient=(rootZoneDepletation<readilyAvailableWater)? 1:(totalAvailableWater - rootZoneDepletation) / (totalAvailableWater - readilyAvailableWater);
			/*System.out.println("");
			System.out.println("soilMosture            "+soilMosture);
			System.out.println("totalAvailableWater    "+totalAvailableWater);
			System.out.println("rootZone               "+rootZoneDepletation);
			System.out.println("readilyAvailableWater  "+readilyAvailableWater);
			System.out.println("waterStressCoefficient "+waterStressCoefficient);*/	
			int hourOfDay = date.getHourOfDay();

			boolean islight = false;
			if (hourOfDay > 6 && hourOfDay < 18) {
				islight = true;
			}
			double soilFluxparameter;
			if (islight == true) {
				soilFluxparameter = 0.35;
				}
			else {
				soilFluxparameter = 0.75;
				}     
			double soilHeatFlux = (soilFlux==defaultSoilFlux)?(soilFluxparameter * netRadiation):soilFlux;			

            double etp = compute(netRadiation, windVelocity, airTemperature, relativeHumidity, atmosphericPressure, soilHeatFlux)*waterStressCoefficient*cropCoefficient;
            outLatentHeatFao.put(basinId, new double[]{etp * latentHeatEvaporation / 86400});
            outEvapotranspirationFao.put(basinId, new double[]{etp*time/86400});
        }
        step++;

    }

    private double compute( double netRadiation, double windVelocity, double airTemperature, double relativeHumidity, 
    		double atmosphericPressure, double soilHeatFlux) {

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
}
