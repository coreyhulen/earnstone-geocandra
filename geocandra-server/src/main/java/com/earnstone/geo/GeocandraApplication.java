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

import java.util.HashSet;
import java.util.Set;

public final class GeocandraApplication extends javax.ws.rs.core.Application {

	@Override
	public Set<Class<?>> getClasses() {
		Set<Class<?>> serviceClasses = new HashSet<Class<?>>();
		serviceClasses.add(GeocandraService.class);
		return serviceClasses;
	}

	@Override
	public Set<Object> getSingletons() {
		HashSet<Object> singletons = new HashSet<Object>();
		singletons.add(new org.codehaus.jackson.jaxrs.JacksonJsonProvider());
		return singletons;
	}
}
