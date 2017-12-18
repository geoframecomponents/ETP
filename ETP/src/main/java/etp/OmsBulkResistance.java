package etp;

import java.util.Map.Entry;
import java.util.HashMap;
import java.util.Set;
////////////////////
import org.jgrasstools.gears.libs.modules.JGTModel;
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
@Description("Calculates surface resistance")
@Author(name = "Michele Bottazzi", contact = "michele.bottazzi@gmail.com")
@Keywords("Evapotranspiration, Hydrology")
@Label("")
@Name("")
@Status(Status.CERTIFIED)
@License("General Public License Version 3 (GPLv3)")

public class OmsBulkResistance extends JGTModel {
	
	@Description("The maximum daily air temperature.")
    @In
    @Unit("C")
    public HashMap<Integer, double[]> inLeafStomatalResistance;
    @Description("The maximum daily air temperature.")
    @In
    @Unit("C")
    public double defaultLeafStomatalResistance = 100.0;
    @Description("The maximum daily air temperature.")
    @In
    @Unit("C")
    public HashMap<Integer, double[]> inLeafAreaIndex;
    @Description("The maximum daily air temperature.")
    @In
    @Unit("C")
    public double defaultLeafAreaIndex = 24*0.12;

    @Description("The maximum daily air temperature.")
    @In
    @Unit("C")
    public HashMap<Integer, double[]> inActiveLeafConstant;
    @Description("The maximum daily air temperature.")
    @In
    @Unit("C")
    public double defaultActiveLeafConstant = 0.5;  

    @Description("The reference evapotranspiration.")
    @Unit("mm day-1")
    @Out
    public HashMap<Integer, double[]> outBulkResistance;
    @Execute
    public void process() throws Exception {
    	outBulkResistance = new HashMap<Integer, double[]>();
        Set<Entry<Integer, double[]>> entrySet = inLeafStomatalResistance.entrySet();
        for( Entry<Integer, double[]> entry : entrySet ) {
            Integer basinId = entry.getKey();
     
            double leafStomatalResistance = defaultLeafStomatalResistance;
            if (inLeafStomatalResistance != null) {leafStomatalResistance = inLeafStomatalResistance.get(basinId)[0];}
            double leafAreaIndex = defaultLeafAreaIndex;
            if (inLeafAreaIndex != null) {leafAreaIndex = inLeafAreaIndex.get(basinId)[0];}
            double activeLeafConstant = defaultActiveLeafConstant;
            if (inActiveLeafConstant != null) {activeLeafConstant = inActiveLeafConstant.get(basinId)[0];}
            
            double surfaceResistance = compute(leafStomatalResistance,leafAreaIndex,activeLeafConstant);
            outBulkResistance.put(basinId, new double[]{surfaceResistance});
            }
        }
    private double compute(double leafStomatalResistance, double leafAreaIndex, double activeLeafConstant) {
    	double laiActive = leafAreaIndex*activeLeafConstant;
    	double result = leafStomatalResistance/laiActive;
    	return result;
    	}
    }
