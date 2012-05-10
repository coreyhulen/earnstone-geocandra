package com.earnstone.geo;

import junit.framework.Assert;

import org.junit.Test;

public class PlaceListTest {
	
	@Test 
	public void list() {
		
		PlaceList list = new PlaceList();
		
		Place place1 = new Place();
		place1.setId(1l);
		list.add(place1);
		
		Place place2 = new Place();
		place2.setId(2l);
		list.add(place2);
		
		list.add(null);
		
		Place place3 = new Place();
		place3.setId(3l);
		list.add(place3);
	
		String json = list.toJson();
		PlaceList rlist = PlaceList.fromString(json);
		
		Assert.assertEquals(4, rlist.size());
		Assert.assertEquals(place1.getId(), rlist.get(0).getId());
		Assert.assertEquals(null, rlist.get(2));
		Assert.assertEquals(3, rlist.indexOf(place3.getId()));
	}
	
	@Test	
	public void truncate() {		
		PlaceList list = new PlaceList();
		list.add(new Place());
		list.get(0).setPoint(new GeoPoint(5.0, 5.0));
		list.add(new Place());
		list.get(1).setPoint(new GeoPoint(4.0, 4.0));
		list.add(new Place());
		list.get(2).setPoint(new GeoPoint(3.0, 3.0));
		list.add(new Place());
		list.get(3).setPoint(new GeoPoint(1.0, 1.0));
		list.add(new Place());				
		list.get(4).setPoint(new GeoPoint(2.0, 2.0));
		
		list.truncateTo(10);
		Assert.assertEquals(5, list.size());
		
		list.truncateTo(4);
		Assert.assertEquals(4, list.size());
		Assert.assertEquals(5.0, list.get(0).getPoint().getLatitude());
		
		list.truncateTo(2);
		Assert.assertEquals(2, list.size());
		Assert.assertEquals(5.0, list.get(0).getPoint().getLatitude());
		
		list.truncateTo(1);
		Assert.assertEquals(1, list.size());
		Assert.assertEquals(5.0, list.get(0).getPoint().getLatitude());
		
		list.truncateTo(0);
		Assert.assertEquals(0, list.size());
	}
	
	@Test	
	public void sortByDistance() {		
		PlaceList list = new PlaceList();
		list.add(new Place());
		list.get(0).setPoint(new GeoPoint(5.0, 5.0));
		list.add(new Place());
		list.get(1).setPoint(new GeoPoint(4.0, 4.0));
		list.add(new Place());
		list.get(2).setPoint(new GeoPoint(3.0, 3.0));
		list.add(new Place());
		list.get(3).setPoint(new GeoPoint(1.0, 1.0));
		list.add(new Place());				
		list.get(4).setPoint(new GeoPoint(2.0, 2.0));
		
		list.sortByDistanceFrom(new GeoPoint(0,0));
		Assert.assertEquals(1.0, list.get(0).getPoint().getLatitude());
		Assert.assertEquals(1.0, list.get(0).getPoint().getLongitude());
	}
}
