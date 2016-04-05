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
package etp;

import static org.jgrasstools.gears.libs.modules.JGTConstants.isNovalue;

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

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.jgrasstools.gears.libs.modules.JGTConstants;
import org.jgrasstools.gears.libs.modules.JGTModel;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

@Description("Calculate evapotraspiration based on the Priestley Taylor model")
@Author(name = "Giuseppe Formetta, Silvia Franceschi and Andrea Antonello", contact = "maryban@hotmail.it")
@Keywords("evapotraspiration, hydrology")
@Label("")
@Name("ptetp")
@Status(Status.CERTIFIED)
@License("General Public License Version 3 (GPLv3)")

public class OmsPriestleyTaylorEtpModel extends JGTModel {

	@Description("The net Radiation at the grass surface in W/m2 for the current hour.")
	@In
	@Unit("Watt m-2 ")
	public HashMap<Integer, double[]> inNetradiation;

	@Description("The daily net Radiation default value in case of missing data.")
	@In
	@Unit("Watt m-2")
	public double defaultDailyNetradiation = 300.0;

	@Description("The hourly net Radiation default value in case of missing data.")
	@In
	@Unit("Watt m-2")
	public double defaultHourlyNetradiation = 100.0;

	@Description("Switch that defines if it is hourly.")
	@In
	public boolean doHourly;

	@Description("The mean hourly air temperature.")
	@In
	@Unit("C")
	public HashMap<Integer, double[]> inTemp;

	@Description("The alpha.")
	@In
	@Unit("m")
	public double pAlpha = 0;

	@Description("The g morning.")
	@In
	public double pGmorn = 0;

	@Description("The g nigth.")
	@In
	public double pGnight = 0;

	@Description("The temperature default value in case of missing data.")
	@In
	@Unit("C")
	public double defaultTemp = 15.0;

	@Description("The atmospheric pressure in KPa.")
	@In
	@Unit("KPa")
	public HashMap<Integer, double[]> inPressure;

	@Description("The pressure default value in case of missing data.")
	@In
	@Unit("KPa")
	public double defaultPressure = 100.0;

	@Description("The mean hourly air temperature.")
	@In
	public String tStartDate;
	
	@Description("The mean hourly air temperature.")
	@In
	public SimpleFeatureCollection inStations;
	
	@Description("The mean hourly air temperature.")
	@In
	public String fStationsid;

	@Description(" The vetor containing the id of the station")
	Object []idStations;
	
	@Description("the linked HashMap with the coordinate of the stations")
	LinkedHashMap<Integer, Coordinate> stationCoordinates;

	@Description("The reference evapotranspiration.")
	@Unit("mm hour-1")
	@Out
	public HashMap<Integer, double[]> outPTEtp;

	private DateTimeFormatter formatter = JGTConstants.utcDateFormatterYYYYMMDDHHMM;

	int step;

	@Execute
	public void process() throws Exception {
		checkNull(inTemp);

		DateTime startDateTime = formatter.parseDateTime(tStartDate);
		DateTime date=(doHourly==false)?startDateTime.plusDays(step):startDateTime.plusHours(step).plusMinutes(30); 

		outPTEtp = new HashMap<Integer, double[]>();


		// starting from the shp file containing the stations, get the coordinate
		//of each station
		stationCoordinates = getCoordinate(inStations, fStationsid);

		//create the set of the coordinate of the station, so we can 
		//iterate over the set
		Set<Integer> stationCoordinatesIdSet = stationCoordinates.keySet();

		// trasform the list of idStation into an array
		idStations= stationCoordinatesIdSet.toArray();

		// iterate over the list of the stations
		for (int i=0;i<idStations.length;i++){

				double temp = defaultTemp;
				double t = inTemp.get(idStations[i])[0];
				if (!isNovalue(t)) {
					temp = t;
				}

				double netradiation = defaultHourlyNetradiation;
				if (inNetradiation != null) netradiation  = inNetradiation.get(idStations[i])[0];
				
				if (!isNovalue(netradiation )) {
					if (doHourly == true) {
						netradiation =netradiation  * 0.0864 / 24.0;
					} else {
						netradiation = netradiation  * 0.0864;
					}
				}


				double pressure = defaultPressure;
				if (inPressure != null) {
					double p = inPressure.get(idStations[i])[0];
					if (isNovalue(p)) {
						pressure = defaultPressure;
					} else {
						pressure = p;
					}
				}

				int ora = date.getHourOfDay();
				boolean isLigth = false;
				if (ora > 6 && ora < 18) {
					isLigth = true;
				}

				double etp = (isNovalue(netradiation))?0:compute(pGmorn, pGnight, pAlpha, netradiation, temp, pressure, isLigth, doHourly);
				outPTEtp.put((Integer) idStations[i], new double[]{etp});
			}
			step++;
		}

		private double compute( double ggm, double ggn, double alpha, double NetRad, double AirTem, double AtmPres, boolean islight,
				boolean ishourlyo ) {
			double result = 0;
			if (ishourlyo == true) {
				double den_Delta = (AirTem + 237.3) * (AirTem + 237.3);
				double exp_Delta = (17.27 * AirTem) / (AirTem + 237.3);
				double num_Delta = 4098 * (0.6108 * Math.exp(exp_Delta));
				double Delta = num_Delta / den_Delta;

				double lambda = 2.501 - 0.002361 * AirTem;
				double gamma = 0.001013 * AtmPres / (0.622 * lambda);

				double coeff_G;
				if (islight == true) {
					coeff_G = ggm;
				} else {
					coeff_G = ggn;
				}

				double G = coeff_G * NetRad;

				result = (alpha) * Delta * (NetRad - G) / ((gamma + Delta) * lambda);

			} else {
				double den_Delta = (AirTem + 237.3) * (AirTem + 237.3);
				double exp_Delta = (17.27 * AirTem) / (AirTem + 237.3);
				double num_Delta = 4098 * (0.6108 * Math.exp(exp_Delta));
				double Delta = num_Delta / den_Delta;

				double lambda = 2.501 - 0.002361 * AirTem;
				double gamma = 0.001013 * AtmPres / (0.622 * lambda);

				result = (alpha) * Delta * (NetRad) / ((gamma + Delta) * lambda);

			}
			return result;
		}

		/**
		 * Gets the coordinate given the shp file and the field name in the shape with the coordinate of the station.
		 *
		 * @param collection is the shp file with the stations
		 * @param idField is the name of the field with the id of the stations 
		 * @return the coordinate of each station
		 * @throws Exception the exception in a linked hash map
		 */
		private LinkedHashMap<Integer, Coordinate> getCoordinate(SimpleFeatureCollection collection, String idField)
				throws Exception {
			LinkedHashMap<Integer, Coordinate> id2CoordinatesMap = new LinkedHashMap<Integer, Coordinate>();
			FeatureIterator<SimpleFeature> iterator = collection.features();
			Coordinate coordinate = null;
			try {
				while (iterator.hasNext()) {
					SimpleFeature feature = iterator.next();
					int stationNumber = ((Number) feature.getAttribute(idField)).intValue();
					coordinate = ((Geometry) feature.getDefaultGeometry()).getCentroid().getCoordinate();
					id2CoordinatesMap.put(stationNumber, coordinate);
				}
			} finally {
				iterator.close();
			}

			return id2CoordinatesMap;
		}
	}