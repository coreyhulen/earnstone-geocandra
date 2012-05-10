package com.earnstone.geo;

import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

public class GeoPointTest {

	GeoPoint pattersonCA = new GeoPoint(37.4957, -121.2546);
	GeoPoint tracyCA = new GeoPoint(37.7051, -121.4802);
	GeoPoint paloAltoCA = new GeoPoint(37.4135,-122.1312);
	GeoPoint austinTx = new GeoPoint(30.2700,-97.8662);
	GeoPoint fargoND = new GeoPoint(46.9346,-97.2297);
	GeoPoint sanLuisObispoCA = new GeoPoint(35.2678,-120.6591);
	
	@Test
	public void toFromJson01() {
		GeoPoint point = new GeoPoint(1.0, 2.0);
		String json = point.toJson();
		GeoPoint rpoint = GeoPoint.fromString(json);
		Assert.assertEquals(point.getLatitude(), rpoint.getLatitude());
		Assert.assertEquals(point.getLongitude(), rpoint.getLongitude());
	}

	@Test
	public void basicZCurves() {
		testZCurve(new GeoPoint(0.0, 0.0));
		testZCurve(new GeoPoint(1.0, 1.0));		
		testZCurve(new GeoPoint(-1.0, -1.0));		
		testZCurve(new GeoPoint(1.0, -1.0));
		testZCurve(new GeoPoint(-1.0, 1.0));	
		
		testZCurve(new GeoPoint(179.99999, 179.99999));
		testZCurve(new GeoPoint(-179.99999, 179.99999));
		testZCurve(new GeoPoint(179.99999, -179.99999));
		testZCurve(new GeoPoint(-179.99999, -179.99999));

		testZCurve(pattersonCA);
		testZCurve(tracyCA);
		testZCurve(paloAltoCA);
	}
	
	public static void testZCurve(GeoPoint point) {
		long curve = point.getZCurve();
		GeoPoint rpoint = GeoPoint.getPointForZCurve(curve);
		Assert.assertEquals(point.getLatitude(), rpoint.getLatitude(), 0.00002);
		Assert.assertEquals(point.getLongitude(), rpoint.getLongitude(), 0.00002);		
	}

	@Test
	public void sortedzCurves() {
		

		
		GeoPoint orgin = new GeoPoint(0.0, 0.0);
		List<GeoPoint> points = Arrays.asList(new GeoPoint(1.0, 1.0), new GeoPoint(2.0, 2.0), new GeoPoint(3.0, 3.0));
		Assert.assertEquals(0, findNearestIndex(orgin, points));
		
		points = Arrays.asList(new GeoPoint(-1.0, -1.0), new GeoPoint(-2.0, -2.0), new GeoPoint(-3.0, -3.0));
		Assert.assertEquals(0, findNearestIndex(orgin, points));
				
		points = Arrays.asList(new GeoPoint(0.0, 2.0), new GeoPoint(2.0, 0.0), new GeoPoint(0.0, 1.0));
		Assert.assertEquals(2, findNearestIndex(orgin, points));
		
		points = Arrays.asList(new GeoPoint(0.0, -2.0), new GeoPoint(-2.0, 0.0), new GeoPoint(0.0, -1.0));
		Assert.assertEquals(2, findNearestIndex(orgin, points));		
		
		points = Arrays.asList(new GeoPoint(2.0, 2.0), new GeoPoint(-2.0, 2.0), new GeoPoint(2.0, -2.0), new GeoPoint(-2.0, -2.0), new GeoPoint(1.0, 1.0));
		Assert.assertEquals(4, findNearestIndex(orgin, points));
		
		points = Arrays.asList(new GeoPoint(2.0, 2.0), new GeoPoint(-2.0, 2.0), new GeoPoint(2.0, -2.0), new GeoPoint(-2.0, -2.0), new GeoPoint(1.0, -1.0));
		Assert.assertEquals(4, findNearestIndex(orgin, points));
		
		points = Arrays.asList(new GeoPoint(2.0, 2.0), new GeoPoint(-2.0, 2.0), new GeoPoint(2.0, -2.0), new GeoPoint(-2.0, -2.0), new GeoPoint(-1.0, 1.0));
		Assert.assertEquals(4, findNearestIndex(orgin, points));
		
		points = Arrays.asList(new GeoPoint(2.0, 2.0), new GeoPoint(-2.0, 2.0), new GeoPoint(2.0, -2.0), new GeoPoint(-2.0, -2.0), new GeoPoint(-1.0, -1.0));				
		Assert.assertEquals(4, findNearestIndex(orgin, points));		
		
		// Need to investigate this more.  Tracy should be the closest point it's zcurve is
		// the next in line, but palo alto is before it and has a closer zcurve but is 
		// farther in distance.
		
//		System.out.println("pat " + pattersonCA.getZCurve());
//		System.out.println("tra " + tracyCA.getZCurve());
//		System.out.println("aus " + austinTx.getZCurve());
//		System.out.println("far " + fargoND.getZCurve());
//		System.out.println("san " + sanLuisObispoCA.getZCurve());
//		System.out.println("pal " + paloAltoCA.getZCurve());
		
//		san 617669628832459
//		pal 617696206675325
//		pat 617701771785293
//		tra 617792831688941
//		aus 621574973934213
//		far 624368798934580
		
//		points = Arrays.asList(tracyCA, austinTx, fargoND, sanLuisObispoCA, paloAltoCA);				
//		Assert.assertEquals(1, findNearestIndex(pattersonCA, points));		
	}

	public int findNearestIndex(GeoPoint orgin, List<GeoPoint> points) {
		
		long orginZcurve = orgin.getZCurve();
		int index = -1;
		long currentDistance = Long.MAX_VALUE;
		
		for (int i = 0; i < points.size(); i++) {
			long z = points.get(i).getZCurve();
			if (currentDistance > Math.abs(orginZcurve - z)) {
				index = i;
				currentDistance = Math.abs(orginZcurve - z);
			}			 
		}
					
		return index;
	}

	@Test
	public void basicDistances() {
		GeoPoint point1 = new GeoPoint(1.0, 1.0);
		GeoPoint point2 = null;

		double distance1 = point1.getDistance(point2);
		Assert.assertEquals(Double.MAX_VALUE, distance1);

		point2 = new GeoPoint(1.0, 1.0);
		double distance2 = point1.getDistance(point2);
		Assert.assertEquals(0.0, distance2);

		point2 = new GeoPoint(2.0, 1.0);
		double distance3 = point1.getDistance(point2);
		Assert.assertEquals(69.09d, distance3, 0.02);

		point2 = new GeoPoint(1.0, 2.0);
		double distance4 = point1.getDistance(point2);
		Assert.assertEquals(69.09d, distance4, 0.02);
		
		double distance5 = pattersonCA.getDistance(tracyCA);
		Assert.assertEquals(19.02d, distance5, 0.02);
		
		double distance6 = pattersonCA.getDistance(paloAltoCA);
		Assert.assertEquals(48.41d, distance6, 0.02);		
	}
}
