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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import org.codehaus.jackson.map.ObjectMapper;

public class PlaceList extends ArrayList<Place> {

	private static final long serialVersionUID = -8240502490470576709L;

	public PlaceList() {

	}

	PlaceList(Collection<Place> c) {
		super(c);
	}

	public void sortByDistanceFrom(final GeoPoint p) {

		Collections.sort(this, new Comparator<Place>() {
			public int compare(Place p1, Place p2) {

				double distance1 = p.getDistance(p1.getPoint());
				double distance2 = p.getDistance(p2.getPoint());

				if (distance1 > distance2)
					return 1;
				else if (distance1 < distance2)
					return -1;
				else
					return 0;
			}
		});
	}

	public void truncateTo(int limit) {
		
		for (int i = this.size() - 1; i >= limit; i--)
			this.remove(i);		
	}

	public static PlaceList fromString(String content) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			return mapper.readValue(content, PlaceList.class);
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

	public int indexOf(Long id) {

		for (int i = 0; i < this.size(); i++) {
			Place place = this.get(i);
			if (place != null && id.equals(place.getId()))
				return i;
		}

		return -1;
	}
}
