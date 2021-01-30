package etpPointCase;

//import static java.lang.Math.pow;
//
//import java.util.HashMap;
//import java.util.LinkedHashMap;
//import java.util.Set;
//import java.util.Map.Entry;
//
//import org.jgrasstools.gears.libs.modules.JGTConstants;
//import org.joda.time.DateTime;
//import org.joda.time.format.DateTimeFormatter;
//
//import com.vividsolutions.jts.geom.Coordinate;

import oms3.annotations.Author;
import oms3.annotations.Description;
import oms3.annotations.Keywords;
import oms3.annotations.Label;
import oms3.annotations.License;
import oms3.annotations.Name;
import oms3.annotations.Status;
@Description("Calculates evapotranspiration at hourly timestep using FAO Penman-Monteith equation")
@Author(name = "Michele Bottazzi", contact = "michele.bottazzi@gmail.com")
@Keywords("evapotraspiration, hydrology")
@Label("")
@Name("ptet")
@Status(Status.CERTIFIED)
@License("General Public License Version 3 (GPLv3)")
//abstract
//public abstract class PriestleyTaylor implements Evapotranspiration{
class PenmanMonteithFAO{
	private double airTemperature;
	private double atmosphericPressure;
	private double netRadiation;
	private double relativeHumidity;
	private double soilHeatFlux;
	private double windVelocity;
	
	public void setNumber(double airTemperature, double atmosphericPressure,
			double netRadiation, double relativeHumidity, 
			double soilHeatFlux, double windVelocity) {
		this.airTemperature=airTemperature;
		this.atmosphericPressure=atmosphericPressure;
		this.netRadiation=netRadiation;
		this.relativeHumidity=relativeHumidity;
		this.soilHeatFlux=soilHeatFlux;
		this.windVelocity=windVelocity;
		}

	public double doET( ) {	
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
	

