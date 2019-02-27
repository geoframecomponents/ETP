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
 *
 */
package etpSurfaces;
import org.joda.time.DateTime;
import etpClasses.*;

public class MultiLayersCanopy implements TranspiringSurface{
	
	SensibleHeatMethods sensibleHeat 	= new SensibleHeatMethods();
	LatentHeatMethods latentHeat 		= new  LatentHeatMethods();
	RadiationMethod radiationProperties = new RadiationMethod();
	SolarGeometry solarGeometry 		= new SolarGeometry();
	
	double delta;
	double airTemperature;
	double surfaceIrradiatedTemperature = airTemperature; 
	double surfaceShadedTemperature = airTemperature; 
	
	double latentHeatTransferCoefficient;
	double sensibleHeatTransferCoefficient; 
	
	double vaporPressure;
	double saturationVaporPressure;
	
	double shortWaveRadiationDirect; 
	double shortWaveRadiationDiffuse;
	double longWaveRadiation;
	double soilHeatFlux;
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
	
	double stressSun;
	double stressSh;

//	double solarElevationAngle;
	//double shortWaveRadiationDiffuse;
	
	public void setDelta(double delta){ this.delta = delta;}
	public void setAirTemperature(double airTemperature){ this.airTemperature = airTemperature; }
	public void setSurfaceTemperature(double leafTemperature){ this.surfaceIrradiatedTemperature = leafTemperature;} 
	
	public void setStressSun(double stressSun){ this.stressSun = stressSun;} 
	public void setStressSh(double stressSh){ this.stressSh = stressSh;} 


	
	public void setLatentHeatTransferCoefficient(double latentHeatTransferCoefficient){ this.latentHeatTransferCoefficient = latentHeatTransferCoefficient;} 
	public void setSensibleHeatTransferCoefficient(double sensibleHeatTransferCoefficient){ this.sensibleHeatTransferCoefficient = sensibleHeatTransferCoefficient;}
	
	public void setVaporPressure(double vaporPressure){ this.vaporPressure = vaporPressure; }
	public void setSaturationVaporPressure(double saturationVaporPressure){ this.saturationVaporPressure = saturationVaporPressure;}
	
	public void setDirectShortWave(double shortWaveRadiationDirect){ this.shortWaveRadiationDirect = shortWaveRadiationDirect; }
	public void setDiffuseShortWave(double shortWaveRadiationDiffuse){ this.shortWaveRadiationDiffuse = shortWaveRadiationDiffuse; }

	public void setLongWaveRadiation(double longWaveRadiation){ this.longWaveRadiation = longWaveRadiation; }
	public void setSoilHeatFlux(double soilHeatFlux){ this.soilHeatFlux = soilHeatFlux; }

	public void setSide(double side){ this.side = side;}
	public void setLeafAreaIndex(double leafAreaIndex){ this.leafAreaIndex = leafAreaIndex;}
	
	public void setDate(DateTime date){ this.date = date;}
	public void setDoHourly(boolean doHourly){ this.doHourly = doHourly;}
	public void setTimeStep(double time) {this.time = time;} 

	public void setLatitude(double latitude){ this.latitude = latitude;}
	public void setLongitude(double longitude){ this.longitude = longitude;}
	
	//double areaInSunlight;
	//double solarElevationAngle = solarGeometry.getSolarElevationAngle(date, latitude, longitude, doHourly);
	//this.solarElevationAngle = solarElevationAngle;

	//double giamporpio = radiationProperties.computeSunlitLeafAreaIndex(leafAreaIndex, solarElevationAngle);
	//public

	
	///public double incidentSolarRadiation() {		// TODO Auto-generated method stub
//		return 0;
//	}

	
	public MultiLayersCanopy () {};/*double delta, double leafTemperature, double airTemperature, double latentHeatTransferCoefficient,
						  double sensibleHeatTransferCoefficient, double vaporPressure, double saturationVaporPressure,
						  double shortWaveRadiation, double longWaveRadiation, double side, DateTime date, double latitude,
						  double longitude,boolean doHourly, double leafAreaIndex){
		
		this.delta = delta;
		this.leafTemperature = leafTemperature; 
		this.airTemperature = airTemperature;
		this.latentHeatTransferCoefficient = latentHeatTransferCoefficient;
		this.sensibleHeatTransferCoefficient = sensibleHeatTransferCoefficient; 
		this.vaporPressure = vaporPressure;
		this.saturationVaporPressure = saturationVaporPressure;
		this.shortWaveRadiation = shortWaveRadiation;
		this.longWaveRadiation = longWaveRadiation;
		this.side = side;
		this.date = date;
		this.latitude = latitude;
		this.longitude = longitude;
		this.doHourly = doHourly;
		this.leafAreaIndex = leafAreaIndex;*/

	@Override
	public double irradiatedSurface() {
		double solarElevationAngle = solarGeometry.getSolarElevationAngle(date, latitude,longitude, doHourly, time);
		//solarElevationAngle = ((solarElevationAngle>0)?solarElevationAngle:0);
		this.solarElevationAngle = solarElevationAngle;
		double areaInSunlight	=((solarElevationAngle>0)?radiationProperties.computeSunlitLeafAreaIndex(leafAreaIndex, solarElevationAngle):0);
		this.surfaceInSunlight = areaInSunlight;
		return surfaceInSunlight;
	}
	@Override
	public double shadedSurface() {
		double areaInShadow = leafAreaIndex - surfaceInSunlight;
		this.surfaceInShadow = areaInShadow;
		return surfaceInShadow;
	}

	
	@Override
	public double computeLatentHeatIrradiatedSurface() {
		// Computation of the latent heat flux from leaf [J m-2 s-1]
		double latentHeatLight =  stressSun*surfaceInSunlight*latentHeat.computeLatentHeatFlux(delta, surfaceIrradiatedTemperature, airTemperature, latentHeatTransferCoefficient, sensibleHeatTransferCoefficient, vaporPressure, saturationVaporPressure);
		//double latentHeatLight = surfaceInSunlight*(sensibleHeatTransferCoefficient* (delta * (surfaceIrradiatedTemperature - airTemperature) + saturationVaporPressure - vaporPressure))/(sensibleHeatTransferCoefficient/latentHeatTransferCoefficient);
		return latentHeatLight;	
	}
	@Override
	public double computeLatentHeatFluxShadedSurface() {
		double latentHeatShadow = stressSh*surfaceInShadow*latentHeat.computeLatentHeatFlux(delta, surfaceShadedTemperature, airTemperature, latentHeatTransferCoefficient, sensibleHeatTransferCoefficient, vaporPressure, saturationVaporPressure);
		return latentHeatShadow;
	}
			
			
	
	@Override
	public double computeSurfaceTemperatureIrradiatedSurface() {
		//double reducedShortWave = shortWaveCanopyLight/surfaceInSunlight;
		//double reducedShortWave2 = (Double.isNaN(reducedShortWave)?0:reducedShortWave);
		double surfaceTemperatureIrradiated1 = (shortWaveCanopyLight  - soilHeatFlux + 
				sensibleHeatTransferCoefficient*airTemperature*surfaceInSunlight +
				stressSun * 
				latentHeatTransferCoefficient*(delta*airTemperature + vaporPressure - saturationVaporPressure)*surfaceInSunlight  + 
				side * longWaveRadiation * 4 *surfaceInSunlight);
		double surfaceTemperatureIrradiated2 =(1/(
				sensibleHeatTransferCoefficient*surfaceInSunlight + 
				stressSun*latentHeatTransferCoefficient * delta *surfaceInSunlight +	
				side * longWaveRadiation/airTemperature * 4*surfaceInSunlight));
		double surfaceTemperatureIrradiated = surfaceTemperatureIrradiated1*surfaceTemperatureIrradiated2;
		this.surfaceIrradiatedTemperature = surfaceTemperatureIrradiated;
		return surfaceIrradiatedTemperature;	
	}
	@Override
	public double computeSurfaceTemperatureShadedSurface() {
		//double reducedShortWave = shortWaveCanopyShadow/surfaceInShadow;
		latentHeatTransferCoefficient = ((shortWaveCanopyShadow==0)?0:latentHeatTransferCoefficient);
		//double reducedShortWave2 = (Double.isNaN(reducedShortWave)?0:reducedShortWave);

		double surfaceTemperatureShaded1 = (shortWaveCanopyShadow + 
				sensibleHeatTransferCoefficient*airTemperature	*surfaceInShadow +
				stressSh*latentHeatTransferCoefficient*(delta*airTemperature + vaporPressure - saturationVaporPressure)*surfaceInShadow 
				+side * longWaveRadiation * 4 *surfaceInShadow);
		//System.out.print("SW in Sh in canopy"		+shortWaveCanopyShadow);
	//	System.out.print("LW in sh in canopy"		+side * longWaveRadiation * 4 *surfaceInShadow);
		double surfaceTemperatureShaded2 =(1/(
				sensibleHeatTransferCoefficient *surfaceInShadow + 
				stressSh*latentHeatTransferCoefficient * delta *surfaceInShadow +	
				side * longWaveRadiation/airTemperature * 4*surfaceInShadow));
		double surfaceTemperatureShaded = surfaceTemperatureShaded1*surfaceTemperatureShaded2;
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
		double sensibleHeatShadow = surfaceInShadow*sensibleHeat.computeSensibleHeatFlux(sensibleHeatTransferCoefficient, surfaceShadedTemperature, airTemperature);				//sensibleHeatTransferCoefficient * (leafTemperature - airTemperature);
		//System.out.print("HL in sh in canopy"		+sensibleHeatShadow);

		return sensibleHeatShadow;			
	}
	
			
	@Override
	public double incidentSolarRadiation() {
		double incidentSolarRadiationLight = radiationProperties.computeAbsordebRadiationSunlit(leafAreaIndex, solarElevationAngle, shortWaveRadiationDirect, shortWaveRadiationDiffuse);
		incidentSolarRadiationLight = ((solarElevationAngle>0)?incidentSolarRadiationLight:0);
		this.shortWaveCanopyLight = incidentSolarRadiationLight;
		return shortWaveCanopyLight;
	}
	@Override
	public double shadedSolarRadiation() {
		double incidentSolarRadiationShadow = radiationProperties.computeAbsordebRadiationShadow(leafAreaIndex, solarElevationAngle, shortWaveRadiationDirect, shortWaveRadiationDiffuse);
		incidentSolarRadiationShadow = ((solarElevationAngle>0)?incidentSolarRadiationShadow:0);
		this.shortWaveCanopyShadow = incidentSolarRadiationShadow;
		return shortWaveCanopyShadow;
	}


}