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
package etpClasses;

public interface RadiationMethods {
	
	abstract public double getSolarElevationAngle();
	abstract public double computeLongWaveRadiationBalance ();
	abstract public double computeAbsordebRadiationSunlit ();
	abstract public double computeAbsordebRadiationShadow ();
	abstract public double computeSunlitLeafAreaIndex ();

}
