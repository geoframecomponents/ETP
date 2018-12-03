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
import etpClasses.*;


public class Grassland implements TranspiringSurface{

	SensibleHeatMethods sensibleHeat 	= new SensibleHeatMethods();
	LatentHeatMethods latentHeat 		= new  LatentHeatMethods();
	RadiationMethod radiationProperties = new RadiationMethod();
	SolarGeometry solarGeometry 		= new SolarGeometry();
	
	double delta;
	double surfaceIrradiatedTemperature; 
	double surfaceShadedTemperature;
	double airTemperature;
	
	double latentHeatTransferCoefficient;
	double sensibleHeatTransferCoefficient; 
	
	double vaporPressure;
	double saturationVaporPressure;
	
	double shortWaveRadiationDirect; 
	double shortWaveRadiationDiffuse;
	double longWaveRadiation;
	double shortWaveCanopyLight; 
	double shortWaveCanopyShadow; 

	double side;
	double leafAreaIndex;
	
	double surfaceInSunlight;
	double surfaceInShadow;

	DateTime date;
	boolean doHourly;
	double time;

	double latitude;
	double longitude;
	double solarElevationAngle;

//	double solarElevationAngle;
	//double shortWaveRadiationDiffuse;
	
	public void setDelta(double delta){ this.delta = delta;}
	public void setAirTemperature(double airTemperature){ this.airTemperature = airTemperature; }
	public void setSurfaceTemperature(double leafTemperature){ this.surfaceIrradiatedTemperature = leafTemperature;} 
	
	public void setLatentHeatTransferCoefficient(double latentHeatTransferCoefficient){ this.latentHeatTransferCoefficient = latentHeatTransferCoefficient;} 
	public void setSensibleHeatTransferCoefficient(double sensibleHeatTransferCoefficient){ this.sensibleHeatTransferCoefficient = sensibleHeatTransferCoefficient;}
	
	public void setVaporPressure(double vaporPressure){ this.vaporPressure = vaporPressure; }
	public void setSaturationVaporPressure(double saturationVaporPressure){ this.saturationVaporPressure = saturationVaporPressure;}
	
	public void setDirectShortWave(double shortWaveRadiationDirect){ this.shortWaveRadiationDirect = shortWaveRadiationDirect; }
	public void setDiffuseShortWave(double shortWaveRadiationDiffuse){ this.shortWaveRadiationDiffuse = shortWaveRadiationDiffuse; }

	public void setLongWaveRadiation(double longWaveRadiation){ this.longWaveRadiation = longWaveRadiation; }
	
	public void setSide(double side){ this.side = side;}
	public void setLeafAreaIndex(double leafAreaIndex){ this.leafAreaIndex = leafAreaIndex;}
	
	public void setDate(DateTime date){ this.date = date;}
	public void setDoHourly(boolean doHourly){ this.doHourly = doHourly;}
	public void setTimeStep(double time) {this.time = time;}
	
	public void setLatitude(double latitude){ this.latitude = latitude;}
	public void setLongitude(double longitude){ this.longitude = longitude;}
	
	
	public Grassland (){
		
	}

	@Override
	public double irradiatedSurface() {
		double areaInSunlight = leafAreaIndex;
		this.surfaceInSunlight = areaInSunlight;
		return surfaceInSunlight;
	}
	@Override
	public double shadedSurface() {
		double areaInShadow = 0;
		this.surfaceInShadow = areaInShadow;
		return surfaceInShadow;
	}

	
	@Override
	public double computeLatentHeatIrradiatedSurface() {
		// Computation of the latent heat flux from leaf [J m-2 s-1]
		double latentHeatLight = surfaceInSunlight*latentHeat.computeLatentHeatFlux(delta, surfaceIrradiatedTemperature, airTemperature, latentHeatTransferCoefficient, sensibleHeatTransferCoefficient, vaporPressure, saturationVaporPressure);
		//double latentHeatLight = surfaceInSunlight*(sensibleHeatTransferCoefficient* (delta * (surfaceIrradiatedTemperature - airTemperature) + saturationVaporPressure - vaporPressure))/(sensibleHeatTransferCoefficient/latentHeatTransferCoefficient);
		return latentHeatLight;	
	}
	@Override
	public double computeLatentHeatFluxShadedSurface() {
		double latentHeatShadow = surfaceInShadow*latentHeat.computeLatentHeatFlux(delta, surfaceShadedTemperature, airTemperature, latentHeatTransferCoefficient, sensibleHeatTransferCoefficient, vaporPressure, saturationVaporPressure);
		return latentHeatShadow;
	}
			
	
	@Override
	public double computeSurfaceTemperatureIrradiatedSurface() {
		double surfaceTemperatureIrradiated1 = (shortWaveCanopyLight/surfaceInSunlight + sensibleHeatTransferCoefficient*airTemperature +
				latentHeatTransferCoefficient*(delta*airTemperature + vaporPressure - saturationVaporPressure) + 
				side * longWaveRadiation * 4 );
		double surfaceTemperatureIrradiated2 =(1/(sensibleHeatTransferCoefficient + latentHeatTransferCoefficient * delta +	
				side * longWaveRadiation/airTemperature * 4));
		double surfaceTemperatureIrradiated = surfaceTemperatureIrradiated1*surfaceTemperatureIrradiated2;
		this.surfaceIrradiatedTemperature = surfaceTemperatureIrradiated;
		return surfaceIrradiatedTemperature;	
	}
	@Override
	public double computeSurfaceTemperatureShadedSurface() {
		double surfaceTemperatureShaded = 0;
		this.surfaceShadedTemperature = surfaceTemperatureShaded;
		return surfaceShadedTemperature;	
	}
	
	
	@Override
	public double computeSensibleHeatFluxIrradiatedSurface() {
		//double sensibleHeatFlux = sensibleHeatTransferCoefficient * (leafTemperature - airTemperature);
		double sensibleHeatLight = surfaceInSunlight*sensibleHeat.computeSensibleHeatFlux(sensibleHeatTransferCoefficient, surfaceIrradiatedTemperature, airTemperature);				//sensibleHeatTransferCoefficient * (leafTemperature - airTemperature);
		return sensibleHeatLight;	
	}
	@Override
	public double computeSensibleHeatFluxShadedSurface() {
		double sensibleHeatShadow = 0;
		return sensibleHeatShadow;			
	}
	
			
	@Override
	public double incidentSolarRadiation() {
		double incidentSolarRadiationLight = radiationProperties.computeAbsordebRadiationSunlit(leafAreaIndex, solarElevationAngle, shortWaveRadiationDirect, shortWaveRadiationDiffuse);
		this.shortWaveCanopyLight = incidentSolarRadiationLight;
		return shortWaveCanopyLight;
	}
	@Override
	public double shadedSolarRadiation() {
		double incidentSolarRadiationShadow = 0;
		//		Sunlit(leafAreaIndex, solarElevationAngle, shortWaveRadiationDirect, shortWaveRadiationDiffuse);
		this.shortWaveCanopyShadow = incidentSolarRadiationShadow;
		return shortWaveCanopyShadow;
	}


}