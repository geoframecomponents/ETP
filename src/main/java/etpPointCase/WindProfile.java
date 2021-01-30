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

public class WindProfile {
	private double height;
	private double windSpeed;
	private double atmosphericPressure;
	private double netRadiation;
	private double soilHeatFlux;
	
	public void setParams(double height, double windSpeed) {
		this.height = height;
		this.windSpeed=windSpeed;
	}
	
public double windAtZ_AboveGroundSurface ( ) {
	double windAtZ = windSpeed * (Math.log(67.8*height - 5.42))/4.87;
	return windAtZ;  // -----> [mm/day]
	}
}