package com.earnstone.geo;

import junit.framework.Assert;

import org.junit.Test;

import com.earnstone.geo.Place.PlaceType;

public class PlaceTest {

	@Test
	public void toFromJson01() {		
		Place place = new Place();
		place.setId(1l);
		place.setPlaceType(PlaceType.City.toString());
		place.setPlaceSubType("setPlaceSubType");
		place.setName("setName");
		place.setAddressLine1("setAddressLine1");
		place.setAddressLine2("setAddressLine2");
		place.setCity("setCity");
		place.setStateCode("setStateCode");
		place.setState("setState");
		place.setZipCode("setZipCode");
		place.setCountyCode("setCountyCode");
		place.setCounty("setCounty");
		place.setCountry("setCountry");
		place.setPoint(new GeoPoint(1.0, 2.0));		
		place.setCustomData("setCustomData");	
		
		String json = place.toJson();				
		Place rplace = Place.fromString(json);
		
		Assert.assertEquals(place.getId(), rplace.getId());
		Assert.assertEquals(place.getPlaceType(), rplace.getPlaceType());
		Assert.assertEquals(place.getPlaceSubType(), rplace.getPlaceSubType());
		Assert.assertEquals(place.getName(), rplace.getName());
		Assert.assertEquals(place.getAddressLine1(), rplace.getAddressLine1());
		Assert.assertEquals(place.getAddressLine2(), rplace.getAddressLine2());
		Assert.assertEquals(place.getCity(), rplace.getCity());
		Assert.assertEquals(place.getStateCode(), rplace.getStateCode());
		Assert.assertEquals(place.getState(), rplace.getState());
		Assert.assertEquals(place.getZipCode(), rplace.getZipCode());
		Assert.assertEquals(place.getCountyCode(), rplace.getCountyCode());
		Assert.assertEquals(place.getCounty(), rplace.getCounty());
		Assert.assertEquals(place.getCountry(), rplace.getCountry());
		Assert.assertEquals(place.getPoint().getLatitude(), rplace.getPoint().getLatitude());
		Assert.assertEquals(place.getPoint().getLongitude(), rplace.getPoint().getLongitude());
		Assert.assertEquals(place.getCustomData(), rplace.getCustomData());		
	}	
	
	@Test
	public void toFromJson02() {		
		Place place = new Place();
		place.setId(1l);		
		place.setPlaceType(PlaceType.City.toString());		
		place.setPoint(new GeoPoint(1.0, 2.0));			
		
		String json = place.toJson();				
		Place rplace = Place.fromString(json);
		
		Assert.assertEquals(place.getId(), rplace.getId());
		Assert.assertEquals(place.getPlaceType(), rplace.getPlaceType());
		Assert.assertEquals(null, rplace.getPlaceSubType());
		Assert.assertEquals(null, rplace.getName());
		Assert.assertEquals(null, rplace.getAddressLine1());
		Assert.assertEquals(null, rplace.getAddressLine2());
		Assert.assertEquals(null, rplace.getCity());
		Assert.assertEquals(null, rplace.getStateCode());
		Assert.assertEquals(null, rplace.getState());
		Assert.assertEquals(null, rplace.getZipCode());
		Assert.assertEquals(null, rplace.getCountyCode());
		Assert.assertEquals(null, rplace.getCounty());
		Assert.assertEquals(null, rplace.getCountry());
		Assert.assertEquals(place.getPoint().getLatitude(), rplace.getPoint().getLatitude());
		Assert.assertEquals(place.getPoint().getLongitude(), rplace.getPoint().getLongitude());
		Assert.assertEquals(null, rplace.getCustomData());		
	}	
	
//	public static final long twepoch = 1288834974657L;
//	public static final long workerIdBits = 5L;
//	public static final long dataCenterIdBits = 5L;
//	public static final long maxWorkerId = -1L ^ (-1L << workerIdBits);
//	public static final long maxDataCenterId = -1L ^ (-1L << dataCenterIdBits);
//	public static final long sequenceBits = 12L;
//
//	public static final long workerIdShift = sequenceBits;
//	public static final long dataCenterIdShift = sequenceBits + workerIdBits;
//	public static final long timestampLeftShift = sequenceBits + workerIdBits + dataCenterIdBits;
//	public static final long sequenceMask = -1L ^ (-1L << sequenceBits);
//	
//	@Test
//	public void simpleTest() {
//		long id = (1 << timestampLeftShift) | (0 << dataCenterIdShift) | (0 << workerIdShift) | 0;
//		long currentId = ((System.currentTimeMillis() - twepoch) << timestampLeftShift) | (0 << dataCenterIdShift) | (0 << workerIdShift) | 0;
//	
//		System.out.println(currentId);
//		System.out.println(id);
//	}
}
