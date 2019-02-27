/*
 * GNU GPL v3 License
 *
 * Copyright 2018 Michele Bottazzi
 *
 * This program is free software: you can redistribute it and/or modify
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
package etpSurfaces;

import org.joda.time.DateTime;

public interface TranspiringSurface {
	abstract public double irradiatedSurface();
	abstract public double computeLatentHeatIrradiatedSurface ();
	abstract public double computeSurfaceTemperatureIrradiatedSurface ();
	abstract public double computeSensibleHeatFluxIrradiatedSurface ();
	
	abstract public double shadedSurface();
	abstract public double computeLatentHeatFluxShadedSurface ();
	abstract public double computeSurfaceTemperatureShadedSurface ();
	abstract public double computeSensibleHeatFluxShadedSurface ();
	abstract public double incidentSolarRadiation ();
	abstract public double shadedSolarRadiation ();
	
	void setDelta(double delta);
	void setAirTemperature(double airTemperature);
	void setSurfaceTemperature(double surfaceTemperature);
	
	void setStressSun(double stressSun);
	void setStressSh(double stressSh);
	
	void setLatentHeatTransferCoefficient(double latentHeatTransferCoefficient);
	void setSensibleHeatTransferCoefficient(double sensibleHeatTransferCoefficient);
	
	void setVaporPressure(double vaporPressure);
	void setSaturationVaporPressure(double saturationVaporPressure);
	
	void setDirectShortWave(double shortWaveRadiationDirect);
	void setDiffuseShortWave(double shortWaveRadiationDiffuse);

	void setLongWaveRadiation(double longWaveRadiation);
	void setSoilHeatFlux(double soilHeatFlux);

	
	void setSide(double side);
	void setLeafAreaIndex(double leafAreaIndex);
	
	void setDate(DateTime date);
	void setDoHourly(boolean doHourly);
	void setTimeStep(double time);
	
	void setLatitude(double latitude);
	void setLongitude(double longitude);

	


}
