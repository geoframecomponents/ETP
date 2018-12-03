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

public class CanopyModel {
	
/*	public static RadiationMethods defineRadiativeProperties(String type, double diffuseExtinctionCoefficient,	double leafScatteringCoefficient,
			double canopyReflectionCoefficientDiffuse, double leafSide,double longWaveEmittance, double airTemperature, double leafTemperature, 
			double stefanBoltzmannConstant,	double leafAreaIndex, double solarElevationAngle, double shortWaveRadiationDirect, 
			double shortWaveRadiationDiffuse, double delta, double latentHeatTransferCoefficient, double sensibleHeatTransferCoefficient, 
			double vaporPressure, double saturationVaporPressure, double shortWaveRadiation, double longWaveRadiation, double side){};*/
	
	
	
	public static TranspiringSurface createTheCanopy(String type, double delta, double leafTemperature, double airTemperature, 
			double stressSun, double stressSh,
			double latentHeatTransferCoefficient, double sensibleHeatTransferCoefficient, double vaporPressure, 
			double saturationVaporPressure,	double shortWaveRadiation, double longWaveRadiation, double side, DateTime date, 
			double latitude,double longitude,boolean doHourly, double leafAreaIndex){
		TranspiringSurface transpiratingSurface=null;
		
		if ("FlatSurface".equals(type)) {		
			transpiratingSurface= new FlatSurface();/*delta, leafTemperature, airTemperature, latentHeatTransferCoefficient,
					sensibleHeatTransferCoefficient, vaporPressure, saturationVaporPressure,
					shortWaveRadiation, longWaveRadiation, side);*/				
			}
		
		if ("Grassland".equals(type)) {        	
			transpiratingSurface=new Grassland ();/*delta, leafTemperature, airTemperature, latentHeatTransferCoefficient,
					sensibleHeatTransferCoefficient, vaporPressure, saturationVaporPressure,
					shortWaveRadiation, longWaveRadiation, side);*/	        	
			}
		
		if ("MultiLayersCanopy".equals(type)) {	        	
			transpiratingSurface=new MultiLayersCanopy();/*delta, leafTemperature, airTemperature, latentHeatTransferCoefficient,
					sensibleHeatTransferCoefficient, vaporPressure, saturationVaporPressure,
					shortWaveRadiation, longWaveRadiation, side, date, latitude, longitude, doHourly, leafAreaIndex);*/
			}
		return transpiratingSurface;
			
		}
	/*public static RadiationMethods defineRadiativeProperties(String type, double diffuseExtinctionCoefficient,	double leafScatteringCoefficient,
			double canopyReflectionCoefficientDiffuse, double leafSide,double longWaveEmittance, double airTemperature, double leafTemperature, 
			double stefanBoltzmannConstant,	double leafAreaIndex, double solarElevationAngle, double shortWaveRadiationDirect, 
			double shortWaveRadiationDiffuse, double delta, double latentHeatTransferCoefficient, double sensibleHeatTransferCoefficient, 
			double vaporPressure, double saturationVaporPressure, double shortWaveRadiation, double longWaveRadiation, double side){*/
	//	RadiationMethods radiationMethods=null;
		//if ("MultiLayersCanopy".equals(type)) {	        	
		/*	radiationMethods=new MultiLayersCanopy(delta, leafTemperature, airTemperature, latentHeatTransferCoefficient,
					sensibleHeatTransferCoefficient, vaporPressure, saturationVaporPressure,
					shortWaveRadiation, longWaveRadiation, side);*/
		//	}
		//return radiationMethods;
	//}

	}

