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

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class Place {

	public enum PlaceType {
		City, Address, Location, Custom
	}

	private Long id;
	private String placeType;
	private String placeSubType;
	private String name;
	private String addressLine1;
	private String addressLine2;
	private String city;
	private String stateCode;
	private String state;
	private String zipCode;
	private String countyCode;
	private String county;
	private String country;

	private GeoPoint point;

	private String customData;
	private Boolean deleted;

	public Place() {

	}

	public Place(Long id, Boolean deleted) {
		this.id = id;
		this.deleted = deleted;
	}

	public static Place fromString(String content) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			return mapper.readValue(content, Place.class);
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

	public void verify() throws GeocandraException {
		if (this.placeType == null) {
			throw new GeocandraException(String.format("Verify Place (%s) - PlaceType cannot be null", this.id));
		}

		try {
			PlaceType.valueOf(this.placeType);
		}
		catch (Exception ex) {
			throw new GeocandraException(String.format("Verify Place (%s) - PlaceType unknown", this.id));
		}

		if (this.getPoint() == null) {
			throw new GeocandraException(String.format("Verify Place (%s) - GeoPoint cannot be null", this.id));
		}
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getId() {
		return id;
	}

	public void setPlaceType(String placeType) {
		this.placeType = placeType;
	}

	public String getPlaceType() {
		return placeType;
	}

	public void setPlaceSubType(String placeSubType) {
		this.placeSubType = placeSubType;
	}

	public String getPlaceSubType() {
		return placeSubType;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setAddressLine1(String addressLine1) {
		this.addressLine1 = addressLine1;
	}

	public String getAddressLine1() {
		return addressLine1;
	}

	public void setAddressLine2(String addressLine2) {
		this.addressLine2 = addressLine2;
	}

	public String getAddressLine2() {
		return addressLine2;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getCity() {
		return city;
	}

	public void setStateCode(String stateCode) {
		this.stateCode = stateCode;
	}

	public String getStateCode() {
		return stateCode;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getState() {
		return state;
	}

	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}

	public String getZipCode() {
		return zipCode;
	}

	public void setCountyCode(String countyCode) {
		this.countyCode = countyCode;
	}

	public String getCountyCode() {
		return countyCode;
	}

	public void setCounty(String county) {
		this.county = county;
	}

	public String getCounty() {
		return county;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getCountry() {
		return country;
	}

	public void setPoint(GeoPoint point) {
		this.point = point;
	}

	public GeoPoint getPoint() {
		return point;
	}

	public void setCustomData(String customData) {
		this.customData = customData;
	}

	public String getCustomData() {
		return customData;
	}

	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}

	public Boolean isDeleted() {
		return deleted;
	}
}
