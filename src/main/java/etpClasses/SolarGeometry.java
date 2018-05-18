package etpClasses;
import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;


import org.joda.time.DateTime;

public class SolarGeometry {	
	public double getSolarElevationAngle(DateTime date, double latitude, boolean doHourly) {
		// from Iqbal, M. (2012). An introduction to solar radiation.
		int dayOfTheYear = date.getDayOfYear();
		double dayAngle = 2*PI*(dayOfTheYear-1)/365;
		double equationOfTime = 0.017 + 0.4281*cos(dayAngle) - 7.351*sin(dayAngle) - 3.349*cos(2*dayAngle) - 9.7331*sin(2*dayAngle);
		double solarNoon = 12 + (4*(15-latitude)-equationOfTime)/60;
		double hour=(doHourly==true)? (double)date.getMillisOfDay() / (1000 * 60 * 60):12.5;
		double hourAngleOfSun = PI*(hour - solarNoon)/12;
		double solarDeclinationAngle = -23.4*PI*cos(2*PI*(dayOfTheYear+10)/365)/180;
		double solarElevationAngle = sin(latitude)*sin(solarDeclinationAngle)+cos(latitude)*cos(solarDeclinationAngle)*cos(hourAngleOfSun);
		
		//double ss = Math.acos(-Math.tan(delta) * Math.tan(latitude));
	//	double sunrise = 12 * (1.0 - ss / Math.PI);
		/*double sunset = 12 * (1.0 + ss / Math.PI);

		if (hour > (sunrise) && hour < (sunset) & (hour - sunrise) < 0.01) hour = hour + 0.1;
		if (hour > (sunrise) && hour < (sunset) & (sunset - hour) < 0.01) hour = hour - 0.1;*/

		//the hour angle is zero at noon and has the following value in radians at any time t
		// given in hours and decimal fraction:
	//	double solarElevationAngle=(hour/ 12.0 - 1.0) * Math.PI;
		return solarElevationAngle;

	}

}
