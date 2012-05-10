package com.earnstone.geo;

import junit.framework.Assert;

import org.junit.Test;

public class PlaceMapTest {
	
	@Test 
	public void list() {
		
		PlaceMap list = new PlaceMap();
		
		Place place1 = new Place();
		place1.setId(1l);
		list.add(place1);
		
		Place place2 = new Place();
		place2.setId(2l);
		list.add(place2);
						
		Place place3 = new Place();
		place3.setId(3l);
		list.add(place3);
	
		String json = list.toJson();
		PlaceMap rlist = PlaceMap.fromString(json);
		
		Assert.assertEquals(3, rlist.size());
		Assert.assertEquals(place1.getId(), rlist.get(place1.getId()).getId());
		Assert.assertEquals(place2.getId(), rlist.get(place2.getId()).getId());
		Assert.assertEquals(place3.getId(), rlist.get(place3.getId()).getId());
	}
}
