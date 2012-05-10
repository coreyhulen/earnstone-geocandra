/*
 * Geocandra: Earnstone geo-location database
 * 
 * Copyright 2011 Corey Hulen, Earnstone Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package com.earnstone.geo;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class GeoPoint {

	private double latitude;
	private double longitude;

	public GeoPoint() {

	}

	public GeoPoint(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public static GeoPoint fromString(String content) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			return mapper.readValue(content, GeoPoint.class);
		}
		catch (Exception e) {
			return null;
		}
	}

	public String toJson() {
		try {
			ObjectMapper mapper = new ObjectMapper();
			return mapper.writeValueAsString(this);
		}
		catch (Exception e) {
			return null;
		}
	}

	@JsonIgnore
	public long getZCurve() {
		// Some assumptions made while computing zcurves.
		// As far as I know the fifth decimal place for GPS coords
		// is approximately 1.1 meters which should be good enough
		// for our index, hence the X 100000.
		// the +180.0 is to handle negative values since the lat
		// and long ranges are -180 to 180.
		
		assert(latitude > -180.0 && latitude < 180.0);
		assert(longitude > -180.0 && longitude < 180.0);		

		long zcurve = 0;
		int ilat = (int) (((float)latitude + 180.0) * 100000.0);
		int ilong = (int) (((float)longitude + 180.0) * 100000.0);

		for (int i = Integer.SIZE - 1; i > -1; i--) {
			int latMost = (ilat >>> i) & 0x0001;
			int longMost = ilong >>> i & 0x0001;

			zcurve <<= 1;
			zcurve |= latMost;

			zcurve <<= 1;
			zcurve |= longMost;
		}

		return zcurve;
	}

	public static GeoPoint getPointForZCurve(long zcurve) {

		int ilat = 0;
		int ilong = 0;

		for (int i = Long.SIZE - 1; i > -1; i--) {

			long latMost = (zcurve >>> i) & 0x00000001;
			i--;
			long longMost = (zcurve >>> i) & 0x00000001;

			ilat <<= 1;
			ilat |= latMost;

			ilong <<= 1;
			ilong |= longMost;
		}

		GeoPoint point = new GeoPoint();
		point.latitude = (((double) ilat) / 100000.0) - 180.0;
		point.longitude = (((double) ilong) / 100000.0) - 180.0;

		return point;
	}

	public double getDistance(GeoPoint point) {
		if (point == null)
			return Double.MAX_VALUE;

		return distance(this, point);
	}

	public static double distance(GeoPoint point1, GeoPoint point2) {
		double distance = (Math.sin(Math.toRadians(point1.latitude)) * Math.sin(Math.toRadians(point2.latitude)) + Math.cos(Math.toRadians(point1.latitude))
				* Math.cos(Math.toRadians(point2.latitude)) * Math.cos(Math.toRadians(point1.longitude - point2.longitude)));

		return distance >= 1.0d ? 0d : (Math.toDegrees(Math.acos(distance))) * 69.09;
	}
}
