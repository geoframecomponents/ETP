package etpClasses;

import static java.lang.Math.pow;

public class LongWaveRadiationBalance {
	
	public double computeLongWaveRadiationBalance(double leafSide, double longWaveEmittance, double airTemperature, double leafTemperature, double stefanBoltzmannConstant) {
		// Compute the net long wave radiation i.e. the incoming minus outgoing [J m-2 s-1]
		double longWaveRadiation = 4 * leafSide * longWaveEmittance * stefanBoltzmannConstant * (((pow (airTemperature, 3))*leafTemperature - (pow (airTemperature, 4))));
		return longWaveRadiation;	
	}
}
