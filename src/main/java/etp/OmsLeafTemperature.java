package etp;

//import static java.lang.Math.exp;
//import static java.lang.Math.pow;
//import static java.lang.Math.log10;
//import static java.lang.Math.PI;
//import static java.lang.Math.abs;
//
//import java.util.HashMap;
//import java.util.Set;
//import java.util.Map.Entry;
import org.jgrasstools.gears.libs.modules.JGTModel;

public class OmsLeafTemperature extends JGTModel {
//    public HashMap<Integer, double[]> inAirTemperature;
//    public HashMap<Integer, double[]> inShortWave;
//    public HashMap<Integer, double[]> inLongWave;
//    public HashMap<Integer, double[]> inWindVelocity;
//    public HashMap<Integer, double[]> inRelativeHumidity;
//    public HashMap<Integer, double[]> outLeafTemperature;
//    
//    public double defaultAirTemperature = 15.0;
//    public double defaultWindVelocity = 5.0;
//    public double defaultShortWave = 50.0;
//    public double defaultLongWave = 50.0;
//    public double defaultRelativeHumidity = 70.0;
//    public double criticalReynoldsNumber = 3000; 	//fixed
//    public double prandtlNumber = 0.71; 			// fixed
//    public double waterMolarMass = 0.018; 			// fixed
//    public double latentHeatEvaporation = 2.45 * pow(10,6); 			// fixed
//    public int leafSide = 2; // 2 if hypostamatous leaf 
//   	
//  
//    
//    public void process() throws Exception {
//    	outLeafTemperature = new HashMap<Integer, double[]>();
//        Set<Entry<Integer, double[]>> entrySet = inAirTemperature.entrySet();
//        for( Entry<Integer, double[]> entry : entrySet ) {
//            Integer basinId = entry.getKey();
//            double airTemperature = defaultAirTemperature;
//            if (inAirTemperature != null) {airTemperature = inAirTemperature.get(basinId)[0];}
//            double shortWave = defaultShortWave;
//            if (inShortWave != null) {shortWave = inShortWave.get(basinId)[0];}
//            double longWave = defaultLongWave;
//            if (inLongWave != null) {longWave = inLongWave.get(basinId)[0];}
//            double netRadiation = shortWave - longWave;
//            double windVelocity = defaultWindVelocity;
//            if (inWindVelocity != null) {windVelocity = inWindVelocity.get(basinId)[0];}
//            double relativeHumidity = defaultRelativeHumidity;
//            if (inRelativeHumidity != null) {relativeHumidity = inRelativeHumidity.get(basinId)[0];}
//            
//            double pWA = saturationPressure(airTemperature);
//            double pA = vapourPressure(relativeHumidity, pWA);
//            double delta = deltaSaturation(airTemperature);
//            // Compute the terms needed to obtain ch
//        	double kinematicViscosity = computeKinematicViscosity (airTemperature);
//           	double reynoldsNumber = computeReynoldsNumber(windVelocity,leafLength,kinematicViscosity);
//           	double nusseltNumber = computeNusseltNumber(reynoldsNumber,criticalReynoldsNumber,prandtlNumber);
//           	double thermalConductivity = computeThermalConductivity (airTemperature); 
//           	double heatTransferCoefficient = computeheatTransferCoefficient(nusseltNumber,leafLength,thermalConductivity);
//           	double sensibleHeatTransferCoefficient = leafSide * heatTransferCoefficient;
//           	////
//            outLeafTemperature.put(basinId, new double[]{});
//            }
//        }
//    /*
//     * Method to determine Ch term
//     */
//	private double computeThermalConductivity (double airTemperature) { 
//		// Formula from Monteith & Unsworth, 2007
//		double thermalConductivity = (6.84 * pow(10,-5)) * airTemperature + 5.62 * pow(10,-3);
//		return thermalConductivity;
//		}
//	
//	private double computeKinematicViscosity (double airTemperature) { 
//		// Formula from Monteith & Unsworth, 2007
//		double kinematicViscosity = (9 * pow(10,-8)) * airTemperature - 1.13 * pow(10,-5);
//		return kinematicViscosity;
//		}
//	
//	private double computeReynoldsNumber (double windVelocity,double leafLength,double kinematicViscosity) {
//		double reynoldsNumber = (windVelocity * leafLength)/ kinematicViscosity;
//		return reynoldsNumber;
//		}
//	
//	private double computeNusseltNumber (double reynoldsNumber, double criticalReynoldsNumber, double prandtlNumber) {
//		// from Incropera et al, 2006
//		double c3 = criticalReynoldsNumber - reynoldsNumber;
//		double c2 = (reynoldsNumber + criticalReynoldsNumber - abs(c3))/2;
//		double c1 = 0.037 * pow(c2,4/5) - 0.664 * pow(c2,1/2);
//		double nusseltNumber = (0.037 * pow(reynoldsNumber,4/5) - c1) * pow(prandtlNumber,1/3);
//		return nusseltNumber;
//		}
//	
//	private double computeheatTransferCoefficient (double nusseltNumber, double leafLength, double thermalConductivity) { 
//		// Formula from Schymanski and Or, 2017
//		double heatTransferCoefficient = (thermalConductivity * nusseltNumber)/leafLength;
//		return heatTransferCoefficient;
//		}
//	
//	/*
//	 * Pressure Terms
//	 */
//    private double saturationPressure (double airTemperature) {
//    	//water vapor pressure using Goff/WMO
//    	double firstTerm = 10.79574 * (1 - (273.16 / airTemperature));
//    	double secondTerm = -5.02800 * log10(airTemperature/273.16);
//    	double thirdTerm =  1.50475 * pow(10,-4) * (1 - pow(10,-8.2969*(airTemperature/273.16-1)));
//    	double fourthTerm = 0.42873 * pow(10,-3) * pow(10,(4.76955*(1-273.16/airTemperature)) - 1);
//    	double fifthTerm = 0.78614;
//    	double exponent = firstTerm + secondTerm + thirdTerm + fourthTerm + fifthTerm;
//    	double saturationPressure = pow(10, exponent);
//    	return saturationPressure;
//    	}
//    private double vapourPressure (double relativeHumidity, double saturationPressure) {
//    	//water pressure computed using Goff/WMO formula
//    	double vapourPressure = (relativeHumidity * saturationPressure)/100;
//    	return vapourPressure;
//    	}
//    ///////////////////////////////////
//    private double deltaSaturation (double airTemperature){
//        double denDelta = pow(airTemperature + 237.3, 2);
//        double expDelta = (17.27 * airTemperature) / (airTemperature + 237.3);
//        double numDelta = 4098 * (0.6108 * exp(expDelta));
//        double deltaSaturation = numDelta / denDelta;
//        return deltaSaturation;
//        }
    }
