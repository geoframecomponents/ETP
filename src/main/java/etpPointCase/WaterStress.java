package etpPointCase;

import oms3.annotations.Author;
import oms3.annotations.Description;
import oms3.annotations.Keywords;
import oms3.annotations.Label;
import oms3.annotations.License;
import oms3.annotations.Name;
import oms3.annotations.Status;

@Description("Calculate evapotraspiration based on the Priestley-Taylor model")
@Author(name = "Michele Bottazzi", contact = "michele.bottazzi@gmail.com")
@Keywords("evapotraspiration, hydrology")
@Label("")
@Name("ptet")
@Status(Status.CERTIFIED)
@License("General Public License Version 3 (GPLv3)")

public class WaterStress {
	private double depletionFraction;
	private double soilMosture;
	private double rootsDepth;
	private double waterFieldCapacity;
	private double waterWiltingPoint;
	public double totalAvailableWater;
	public double readilyAvailableWater;
	
	public void setParams(double depletionFraction, double soilMosture, double rootsDepth, double waterFieldCapacity, double waterWiltingPoint) {
		this.depletionFraction = depletionFraction;
		this.soilMosture = soilMosture;
		this.rootsDepth = rootsDepth;
		this.waterFieldCapacity = waterFieldCapacity;
		this.waterWiltingPoint = waterWiltingPoint;
	}
	
	public double totalAvailableWater ( ) {
		double TAW = 1000*(waterFieldCapacity - waterWiltingPoint) * rootsDepth;
		this.totalAvailableWater = TAW;
		System.out.println("the TAW inside");
		System.out.println(TAW);
		return TAW;  
		}
	public double rootZoneDepletation ( ) {
		double RZD = 1000*(waterFieldCapacity - soilMosture) * rootsDepth;
		return RZD;  
		}
	public double readilyAvailableWater ( ) {
		double RAW = this.totalAvailableWater * depletionFraction;
		this.readilyAvailableWater = RAW;
		System.out.println("the RAW inside");
		System.out.println(RAW);
		System.out.println("------------");

		return RAW;  
		}
//	public double waterStressCoefficient ( ) {
//		double WSC = (rootZoneDepletation<this.readilyAvailableWater)? 1:(totalAvailableWater - rootZoneDepletation) / (totalAvailableWater - readilyAvailableWater);
//		this.totalAvailableWater * depletionFraction;
//		this.readilyAvailableWater = RAW;
//		return RAW;  
//		}
	
//    double readilyAvailableWater = totalAvailableWater * depletionFraction;
//	double rootZoneDepletation = 1000 * (waterFieldCapacity - soilMosture) * rootsDepth;
//	double waterStressCoefficient=(rootZoneDepletation<readilyAvailableWater)? 1:(totalAvailableWater - rootZoneDepletation) / (totalAvailableWater - readilyAvailableWater);
//
//	
//public double windAtZ_AboveGroundSurface ( ) {
//	double windAtZ = windSpeed * (Math.log(67.8*height - 5.42))/4.87;
//	return windAtZ;  // -----> [mm/day]
//	}
}