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
package etpRasterCase;

import static org.jgrasstools.gears.libs.modules.JGTConstants.isNovalue;

import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import javax.media.jai.iterator.RandomIterFactory;
import javax.media.jai.iterator.WritableRandomIter;
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

import org.geotools.coverage.grid.GridCoverage2D;
import org.jgrasstools.gears.libs.modules.JGTConstants;
import org.jgrasstools.gears.libs.modules.JGTModel;
import org.jgrasstools.gears.utils.RegionMap;
import org.jgrasstools.gears.utils.coverage.CoverageUtilities;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;

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
	public GridCoverage2D inNetradiationGrid;


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


	@Description("The mean hourly air temperature. ")
	@In
	public GridCoverage2D inTemperatureGrid;


	@Description("The alpha.")
	@In
	@Unit("m")
	public double pAlpha = 0;

	@Description("The coefficient for the soil heat flux during daylight")
	@In
	public double pGmorn = 0;

	@Description("The coefficient for the soil heat flux during nighttime")
	@In
	public double pGnight = 0;

	@Description("The temperature default value in case of missing data.")
	@In
	@Unit("C")
	public double defaultTemp = 15.0;


	@Description("The atmospheric pressure in KPa.")
	@In
	public GridCoverage2D inPressureGrid;


	@Description("The pressure default value in case of missing data.")
	@In
	@Unit("KPa")
	public double defaultPressure = 100.0;

	@Description("Start date of simulation")
	@In
	public String tStartDate;


	@Description("The reference evapotranspiration.")
	@Out
	public GridCoverage2D outETpDataGrid;

	private DateTimeFormatter formatter = JGTConstants.utcDateFormatterYYYYMMDDHHMM;

	int step;
	WritableRaster NetradiationMap;
	WritableRaster TemperatureMap;
	WritableRaster PressureMap;

	@Execute
	public void process() throws Exception {
		checkNull(inTemperatureGrid);

		// transform the GrifCoverage2D maps into writable rasters
		 NetradiationMap=mapsReader(inNetradiationGrid);
		 TemperatureMap=mapsReader(inTemperatureGrid);
		if (inPressureGrid!= null) PressureMap=mapsReader(inPressureGrid);

		// get the dimension of the maps
		RegionMap regionMap = CoverageUtilities.getRegionParamsFromGridCoverage(inNetradiationGrid);
		int cols = regionMap.getCols();
		int rows = regionMap.getRows();


		// create the output maps with the right dimensions
		WritableRaster outETpWritableRaster= CoverageUtilities.createDoubleWritableRaster(cols, rows, null, null, null);
		WritableRandomIter ETpIter = RandomIterFactory.createWritable(outETpWritableRaster, null);

		DateTime startDateTime = formatter.parseDateTime(tStartDate);
		DateTime date=(doHourly==false)?startDateTime.plusDays(step):startDateTime.plusHours(step).plusMinutes(30); 

		// iterate over the entire domain and compute for each pixel the SWE
		for( int r = 1; r < rows - 1; r++ ) {
			for( int c = 1; c < cols - 1; c++ ) {

				// get the exact value of the variable in the pixel i, j 



				double temp = defaultTemp;
				temp=TemperatureMap.getSampleDouble(c, r, 0);
				if (!isNovalue(temp)) {
					temp = defaultTemp;
				}


				double netradiation=defaultHourlyNetradiation;
				netradiation=NetradiationMap.getSampleDouble(c, r, 0);
				netradiation=(isNovalue(netradiation))?defaultHourlyNetradiation:netradiation;

				if (!isNovalue(netradiation )) {
					if (doHourly == true) {
						netradiation =netradiation  * 0.0864 / 24.0;
					} else {
						netradiation = netradiation  * 0.0864;
					}
				}


				double pressure = defaultPressure;
				if (inPressureGrid!= null) {
					double p=PressureMap.getSampleDouble(c, r, 0);
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

				double etp = (netradiation<0)?0:compute(pGmorn, pGnight, pAlpha, netradiation, temp, pressure, isLigth, doHourly);
				etp=(etp<0)?0:etp;

				ETpIter.setSample(c, r, 0, etp);


			}
		}

		CoverageUtilities.setNovalueBorder(outETpWritableRaster);
		outETpDataGrid = CoverageUtilities.buildCoverage("ETP", outETpWritableRaster, 
				regionMap, inNetradiationGrid.getCoordinateReferenceSystem());
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
	 * Maps reader transform the GrifCoverage2D in to the writable raster and
	 * replace the -9999.0 value with no value.
	 *
	 * @param inValues: the input map values
	 * @return the writable raster of the given map
	 */
	private WritableRaster mapsReader ( GridCoverage2D inValues){	
		RenderedImage inValuesRenderedImage = inValues.getRenderedImage();
		WritableRaster inValuesWR = CoverageUtilities.replaceNovalue(inValuesRenderedImage, -9999.0);
		inValuesRenderedImage = null;
		return inValuesWR;
	}


}