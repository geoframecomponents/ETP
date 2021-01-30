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
//import static java.lang.Math.pow;
//import java.util.HashMap;
//import java.util.LinkedHashMap;
//import java.util.Map.Entry;
//import java.util.Set;

import oms3.annotations.Author;
import oms3.annotations.Description;
//import oms3.annotations.Execute;
//import oms3.annotations.In;
import oms3.annotations.Keywords;
import oms3.annotations.Label;
import oms3.annotations.License;
import oms3.annotations.Name;
//import oms3.annotations.Out;
import oms3.annotations.Status;
//import oms3.annotations.Unit;


//import org.jgrasstools.gears.libs.modules.JGTConstants;
//import org.jgrasstools.gears.libs.modules.JGTModel;
//import org.joda.time.DateTime;
//import org.joda.time.format.DateTimeFormatter;

//import com.vividsolutions.jts.geom.Coordinate;

@Description("Calculate evapotraspiration based on the Priestley Taylor model")
@Author(name = "Michele Bottazzi", contact = "michele.bottazzi@gmail.com")
@Keywords("evapotraspiration, hydrology")
@Label("")
@Name("ptet")
@Status(Status.CERTIFIED)
@License("General Public License Version 3 (GPLv3)")

public class FAOComponent{
	public static void main(String[] args) {
		double airTemperature=280;
		double atmosphericPressure=280;
		double netRadiation=280;
		double relativeHumidity=280;
		double soilHeatFlux=280;
		double windVelocity=280;
	
	
	PenmanMonteithFAO FAO = new PenmanMonteithFAO();
	FAO.setNumber(airTemperature, atmosphericPressure, netRadiation, relativeHumidity, soilHeatFlux, windVelocity);
	double pmfaoetp = FAO.doET();
	System.out.println(pmfaoetp);

	
	}
}