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

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class GeocandraClient implements IGeocandra {

	private static final String DefaultUrl = "http://localhost:43622/";

	private String connectionUrl;
	private Client client;

	private WebResource wr;

	public GeocandraClient() {
		ClientConfig cc = new DefaultClientConfig();
		cc.getClasses().add(org.codehaus.jackson.jaxrs.JacksonJsonProvider.class);
		client = Client.create(cc);	
		setConnectionUrl(DefaultUrl);
	}	

	public void setConnectionUrl(String connectionUrl) {
		this.connectionUrl = connectionUrl;
		wr = client.resource(connectionUrl);
	}

	public String getConnectionUrl() {
		return connectionUrl;
	}

	public Long savePlace(Place place) {
		MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
		formData.add("place", place.toJson());
		return wr.path("/savePlace").type(MediaType.APPLICATION_FORM_URLENCODED).post(Long.class, formData);
	}

	public Long deletePlace(Long id) {
		return wr.path("/deletePlace").queryParam("id", id.toString()).get(Long.class);
	}

	public Place getPlace(Long id) {
		return wr.path("/getPlace").queryParam("id", id.toString()).get(Place.class);
	}

	public PlaceMap getPlaces(IdList ids) {
		MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
		formData.add("ids", ids.toJson());
		return wr.path("/getPlaces").type(MediaType.APPLICATION_FORM_URLENCODED).post(PlaceMap.class, formData);
	}

	public PlaceList getNearest(GeoPoint point, Integer limit) {
		MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
		formData.add("point", point.toJson());
		formData.add("limit", Integer.toString(limit));
		return wr.path("/getNearest").type(MediaType.APPLICATION_FORM_URLENCODED).post(PlaceList.class, formData);
	}
}
