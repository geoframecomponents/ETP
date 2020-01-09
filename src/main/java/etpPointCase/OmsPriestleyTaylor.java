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

import org.jgrasstools.gears.libs.modules.JGTConstants;
import org.jgrasstools.gears.libs.modules.JGTModel;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;

import com.vividsolutions.jts.geom.Coordinate;

@Description("Calculate evapotraspiration based on the Priestley Taylor model")
@Author(name = "Giuseppe Formetta, Silvia Franceschi and Andrea Antonello", contact = "maryban@hotmail.it")
@Keywords("evapotraspiration, hydrology")
@Label("")
@Name("ptetp")
@Status(Status.CERTIFIED)
@License("General Public License Version 3 (GPLv3)")

public class OmsPriestleyTaylor extends JGTModel {

	@Description("Switch that defines if it is hourly.")
	@In
	public boolean doHourly;

	@Description("The mean hourly air temperature.")
    @In
    @Unit("C")
    public HashMap<Integer, double[]> inAirTemperature;

    @Description("The temperature default value in case of missing data.")
    @In
    @Unit("C")
    public double defaultAirTemperature = 15.0;

	@Description("The alpha parameter.")
	@In
	@Unit("-")
	public double alpha;

	@Description("The coefficient for the soil heat flux during daylight")
	@In
	public double soilFluxParameterDay;

	@Description("The coefficient for the soil heat flux during nighttime")
	@In
	public double soilFluxParameterNight;
	
	@Description("The net Radiation at the grass surface in W/m2 for the current hour.")
    @In
    @Unit("MJ m-2 hour-1")
    public HashMap<Integer, double[]> inNetRadiation;

    @Description("The net Radiation default value in case of missing data.")
    @In
    @Unit("MJ m-2 hour-1")
    public double defaultNetRadiation = 2.0;

	@Description("The hourly net Radiation default value in case of missing data.")
	@In
	@Unit("Watt m-2")
	public double defaultHourlyNetradiation = 0.0;

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

	@Description("The mean hourly air temperature.")
	@In
	public String tStartDate;
	
	double latentHeatEvaporation = 2.45*pow(10,6);

    double nullValue = -9999.0;

	
	@Description("the linked HashMap with the coordinate of the stations")
	LinkedHashMap<Integer, Coordinate> stationCoordinates;

	@Description("The latent heat.")
	@Unit("W/m2")
	@Out
	public HashMap<Integer, double[]> outLatentHeatPt;
	
	@Description("The reference evapotranspiration.")
	@Unit("mm hour-1")
	@Out
	public HashMap<Integer, double[]> outEvapotranspirationPt;
	
	@Description("The first day of the simulation.")
	@In
	public int temporalStep;

	private DateTimeFormatter formatter = JGTConstants.utcDateFormatterYYYYMMDDHHMM;

	int step;
	public int time;


	@Execute
	public void process() throws Exception {
		//checkNull(inTemp);
		
		if (doHourly == true) {
			time =temporalStep*60;

			} else {
			time = 86400;
			}
		DateTime startDateTime = formatter.parseDateTime(tStartDate);
		DateTime date=(doHourly==false)?startDateTime.plusDays(step).plusHours(12):startDateTime.plusMinutes(temporalStep*step);

		outLatentHeatPt = new HashMap<Integer, double[]>();
		outEvapotranspirationPt = new HashMap<Integer, double[]>();

		Set<Entry<Integer, double[]>> entrySet = inAirTemperature.entrySet();
		for( Entry<Integer, double[]> entry : entrySet ) {
            Integer basinId = entry.getKey();


            double airTemperature = inAirTemperature.get(basinId)[0];
			if (airTemperature == (nullValue)) {airTemperature = defaultAirTemperature;}		
			
			double netRadiation = inNetRadiation.get(basinId)[0];
			if (netRadiation == (nullValue)) {netRadiation = defaultNetRadiation;}
			netRadiation = netRadiation * 86400/1E6;
				
			double soilFlux = defaultSoilFlux;
			if (inSoilFlux != null){soilFlux = inSoilFlux.get(basinId)[0];}
			if (soilFlux == nullValue) {soilFlux = defaultSoilFlux;}
			soilFlux = soilFlux * 86400/1E6;			
						
			double atmosphericPressure = defaultAtmosphericPressure;
			if (inAtmosphericPressure != null){atmosphericPressure = inAtmosphericPressure.get(basinId)[0]/1000;}
			if (atmosphericPressure == (nullValue/1000)) {atmosphericPressure = defaultAtmosphericPressure;}
//			soilFlux = soilFlux * 86400/1E6;			
//			
//			double atmosphericPressure = inAtmosphericPressure.get(basinId)[0]/1000;
//			if (atmosphericPressure == (nullValue/1000)) {atmosphericPressure = defaultAtmosphericPressure;}		
	        //System.out.println("soilFlux    "+soilFlux);	
			//double atmosphericPressure = inAtmosphericPressure.get(basinId)[0];
	        //System.out.println("atmosphericPressure    "+inAtmosphericPressure.get(basinId)[0]);
		//	if (atmosphericPressure == (nullValue)) {atmosphericPressure = defaultAtmosphericPressure;}

			int hourOfDay = date.getHourOfDay();
		
			boolean isLigth = false;
			if (hourOfDay > 6 && hourOfDay < 18) {
				isLigth = true;
			}
			double soilFluxparameter;
			if (isLigth == true) {
				soilFluxparameter = soilFluxParameterDay;
			} else {
				soilFluxparameter = soilFluxParameterNight;
			}
	        double soilHeatFlux = (soilFlux==defaultSoilFlux)?(soilFluxparameter * netRadiation):soilFlux;			
	       // System.out.println("netRadiation    "+netRadiation);
	      //  System.out.println("airTemperature    "+airTemperature);

	       // System.out.println("soilHeatFlux    "+soilHeatFlux);

	        //ystem.out.println("netRadiation    "+netRadiation);	      

	        double etp = (netRadiation<0)?0:computeEvapotranspirationPt(alpha, netRadiation, airTemperature, atmosphericPressure, soilHeatFlux);
		    //System.out.println("etp    "+etp); ///2450000

	        etp=(etp<0)?0:etp;
	        //etp = etp*time;
	        outEvapotranspirationPt.put((Integer)  basinId, new double[]{etp * time / 86400});
	        outLatentHeatPt.put((Integer)  basinId, new double[]{etp * latentHeatEvaporation / 86400});
		   // System.out.println("time    "+time + "     " + etp*time/86400); //mm/timestep ///2450000

	       // *(2.45*1E6)/86400;
			}
			step++;
		}
		private double computeEvapotranspirationPt( double alpha, double netRadiation, double airTemperature, double atmosphericPressure, double soilHeatFlux) {
				double denDelta = Math.pow((airTemperature + 237.3), 2);
		        double expDelta = (17.27 * airTemperature) / (airTemperature + 237.3);
		        double numDelta = 4098 * (0.6108 * Math.exp(expDelta));
		        double delta = numDelta / denDelta;
		        double psychrometricConstant = 0.665 * 0.001 * atmosphericPressure;
				double result = ((alpha/2.45) * delta * (netRadiation - soilHeatFlux)) / (psychrometricConstant + delta); //* (1E6)/86400;
				return result;  // -----> [mm/day]
		}

}