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

import org.codehaus.jackson.map.ObjectMapper;

public class IdList extends ArrayList<Long> {

	private static final long serialVersionUID = 3153353757019730799L;

	public IdList() {
	}

	IdList(Collection<Long> c) {
		super(c);
	}

	public static IdList fromString(String content) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			return mapper.readValue(content, IdList.class);
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
}
