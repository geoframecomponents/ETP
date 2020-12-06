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
import oms3.annotations.In;
import oms3.annotations.Keywords;
import oms3.annotations.Label;
import oms3.annotations.License;
import oms3.annotations.Name;
//import oms3.annotations.Out;
import oms3.annotations.Status;
import oms3.annotations.Unit;
//import oms3.annotations.Execute;
//import oms3.annotations.override;
//import java.io.*; 
@Description("Calculate evapotraspiration based on the Priestley-Taylor model")
@Author(name = "Michele Bottazzi", contact = "michele.bottazzi@gmail.com")
@Keywords("evapotraspiration, hydrology")
@Label("")
@Name("ptet")
@Status(Status.CERTIFIED)
@License("General Public License Version 3 (GPLv3)")
//abstract
//public abstract class PriestleyTaylor implements Evapotranspiration{
class PriestleyTaylor{
	private double alpha;
	private double airTemperature;
	private double atmosphericPressure;
	private double netRadiation;
	private double soilHeatFlux;
	
	public void setNumber(double alpha, double airTemperature, double atmosphericPressure,
			double netRadiation, double soilHeatFlux) {
		this.alpha = alpha;
		this.airTemperature=airTemperature;
		this.atmosphericPressure=atmosphericPressure;
		this.netRadiation=netRadiation;
		this.soilHeatFlux=soilHeatFlux;
		}
	public double doET( ) {
		double denDelta = Math.pow((airTemperature + 237.3), 2);
		double expDelta = (17.27 * airTemperature) / (airTemperature + 237.3);
		double numDelta = 4098 * (0.6108 * Math.exp(expDelta));
		double delta = numDelta / denDelta;
		double psychrometricConstant = 0.665 * 0.001 * atmosphericPressure;
		double result = ((alpha/2.45) * delta * (netRadiation - soilHeatFlux)) / (psychrometricConstant + delta); //* (1E6)/86400;
		return result;  // -----> [mm/day]
		}
	}
	

