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

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Properties;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import me.prettyprint.cassandra.serializers.LongSerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.service.CassandraHostConfigurator;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.beans.HColumn;
import me.prettyprint.hector.api.beans.Row;
import me.prettyprint.hector.api.beans.Rows;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.Mutator;
import me.prettyprint.hector.api.query.ColumnQuery;
import me.prettyprint.hector.api.query.MultigetSliceQuery;
import me.prettyprint.hector.api.query.QueryResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.io.IOUtils;

import com.earnstone.id.Generator;
import com.earnstone.index.IndexItem;
import com.earnstone.index.ShardedLongIndex;
import com.earnstone.perf.AvgCallsPerTimeCounter;
import com.earnstone.perf.AvgTimeCounter;
import com.earnstone.perf.Counter;
import com.earnstone.perf.IncrementCounter;
import com.earnstone.perf.LastAccessTimeCounter;
import com.earnstone.perf.PerfUtils.TimePrecision;
import com.earnstone.perf.Registry;

@Path("/")
public class GeocandraService implements IGeocandra {

	private static final Logger log = LoggerFactory.getLogger(GeocandraService.class);

	private static Generator generator;

	private static Cluster cluster;
	private static Keyspace keyspace;
	private static String columnFamily;
	private static String indexColumnFamily;
	private static ShardedLongIndex zcurveIndex;
	private static int zcurveSameIndexLimit;

	private final static String ColumnPlace = "Place";
	private final static String IndexZCurveName = "ZCurve";

	private final static StringSerializer ss = StringSerializer.get();
	private final static LongSerializer ls = LongSerializer.get();

	private static String CounterCategory = "GeocandraService";
	private static IncrementCounter errorCounter;
	private static LastAccessTimeCounter lastErrorCounter;
	private static AvgTimeCounter getNearestAvgTime;
	private static AvgCallsPerTimeCounter getNearestCalls;
	private static AvgTimeCounter getPlacesAvgTime;
	private static AvgCallsPerTimeCounter getPlacesCalls;
	

	public GeocandraService() {
		initialize(GeocandraServer.properties);
	}

	public GeocandraService(Properties properties) {
		initialize(properties);
	}

	private void initialize(Properties properties) {
		try {
			synchronized (GeocandraService.class) {
				if (cluster == null) {

					CassandraHostConfigurator config = new CassandraHostConfigurator(properties.getProperty("geocandra.cassandra.hosts"));
					loadCassandraSettings(config, properties.getProperty("geocandra.cassandra.connection.settings"));

					cluster = HFactory.getOrCreateCluster(properties.getProperty("geocandra.cassandra.cluster.name"), config);
					keyspace = HFactory.createKeyspace(properties.getProperty("geocandra.cassandra.keyspace.name"), cluster);
					columnFamily = properties.getProperty("geocandra.cassandra.columnfamily.name");
					indexColumnFamily = properties.getProperty("geocandra.cassandra.columnfamily.index.name");
					zcurveSameIndexLimit = Integer.parseInt(properties.getProperty("geocandra.zcurve.same.index.limit"));

					zcurveIndex = new ShardedLongIndex(cluster, keyspace, indexColumnFamily, IndexZCurveName);

					int dataCenterId = Integer.parseInt(properties.getProperty("geocandra.eid.datacenter.id"));
					int workerId = Integer.parseInt(properties.getProperty("geocandra.eid.worker.id"));
					generator = new Generator(dataCenterId, workerId);

					errorCounter = new IncrementCounter();
					errorCounter.setCategory(CounterCategory);
					errorCounter.setName("Errors counted");
					errorCounter.setGroup("");
					Registry.register(errorCounter);

					lastErrorCounter = new LastAccessTimeCounter();
					lastErrorCounter.setCategory(CounterCategory);
					lastErrorCounter.setName("Errors last reported (hours ago)");
					lastErrorCounter.setGroup("");
					lastErrorCounter.setTimePrecision(TimePrecision.Hour);
					Registry.register(lastErrorCounter);
					
					getPlacesAvgTime = new AvgTimeCounter();
					getPlacesAvgTime.setCategory(CounterCategory);
					getPlacesAvgTime.setName("getPlaces average time in method (sec)");
					getPlacesAvgTime.setGroup("");
					getPlacesAvgTime.setTimePrecision(TimePrecision.Sec);
					Registry.register(getPlacesAvgTime);
					
					getPlacesCalls = new AvgCallsPerTimeCounter();
					getPlacesCalls.setCategory(CounterCategory);
					getPlacesCalls.setName("getPlaces average calls (per sec)");
					getPlacesCalls.setGroup("");
					getPlacesCalls.setTimePrecision(TimePrecision.Sec);
					Registry.register(getPlacesCalls);
					
					getNearestAvgTime = new AvgTimeCounter();
					getNearestAvgTime.setCategory(CounterCategory);
					getNearestAvgTime.setName("getNearest average time in method (sec)");
					getNearestAvgTime.setGroup("");
					getNearestAvgTime.setTimePrecision(TimePrecision.Sec);
					Registry.register(getNearestAvgTime);
					
					getNearestCalls = new AvgCallsPerTimeCounter();
					getNearestCalls.setCategory(CounterCategory);
					getNearestCalls.setName("getNearest average calls (per sec)");
					getNearestCalls.setGroup("");
					getNearestCalls.setTimePrecision(TimePrecision.Sec);
					Registry.register(getNearestCalls);								
				}
			}
		}
		catch (Exception ex) {
			throw handleException("Error while initializing GeocandraService", ex);
		}
	}

	private static void loadCassandraSettings(CassandraHostConfigurator config, String settings) {
		try {
			String[] keyvalues = settings.split(";");

			for (String keyValueStr : keyvalues) {
				String[] keyValue = keyValueStr.split("=");

				Method method = null;
				for (Method temp : config.getClass().getMethods()) {
					if (temp.getName().equals("set" + keyValue[0])) {
						method = temp;
						break;
					}
				}

				Object value = null;

				if (method.getParameterTypes()[0].getName().equals("int"))
					value = Integer.parseInt(keyValue[1]);
				else if (method.getParameterTypes()[0].getName().equals("long"))
					value = Integer.parseInt(keyValue[1]);
				else if (method.getParameterTypes()[0].getName().equals("boolean"))
					value = Boolean.parseBoolean(keyValue[1]);

				method.invoke(config, new Object[] { value });
			}
		}
		catch (Exception ex) {
			throw new IllegalArgumentException("Error with loading cassandra settings", ex);
		}
	}

	@GET
	@Produces(MediaType.TEXT_HTML)
	public String getServerInfo() {

		InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("server-info.html");

		if (in != null) {
			try {
				String template = IOUtils.toString(in);

				String version = this.getClass().getPackage().getImplementationVersion();
				template = template.replace("{version}", version == null ? "Developer Build" : version);

				double heapSize = ((double) Runtime.getRuntime().totalMemory() / 1024.0d) / 1024.0d;
				double heapMaxSize = ((double) Runtime.getRuntime().maxMemory() / 1024.0d) / 1024.0d;
				DecimalFormat format = new DecimalFormat("#.#");
				template = template.replace("{memory}", String.format("%s / %s", format.format(heapSize), format.format(heapMaxSize)));

				StringBuilder counterHtml = new StringBuilder();
				List<Counter> counters = Registry.listCounters(CounterCategory, null);
				for (Counter counter : counters) {
					counterHtml.append("<tr><td class='perf-name'>").append(counter.getName()).append("</td><td class='perf-value'>").append(counter.getDisplayValue()).append("</td></tr>");
				}

				template = template.replace("{perf}", counterHtml.toString());

				return template;
			}
			catch (IOException e) {
				handleException("Error loading server-info.html.", e);
			}
		}

		return "<html><body>Couldn't load server-info.html template</body></html>";
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Path("/savePlace")
	public Long savePlace(@FormParam("place") Place place) {
		try {

			if (place.getId() == null)
				place.setId(generator.nextId());

			place.verify();
			place.setDeleted(null);

			Mutator<Long> mutator = HFactory.createMutator(keyspace, ls);
			mutator.insert(place.getId(), columnFamily, HFactory.createStringColumn(ColumnPlace, place.toJson()));
			zcurveIndex.addToIndex(place.getPoint().getZCurve(), place.getId());

			return place.getId();
		}
		catch (Exception ex) {
			throw handleException("An error occured while in savePlace", ex);
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/deletePlace")
	public Long deletePlace(@QueryParam("id") Long id) {
		try {
			Place place = getPlace(id);

			if (place != null && place.isDeleted() == null) {
				Mutator<Long> mutator = HFactory.createMutator(keyspace, ls);
				mutator.delete(id, columnFamily, null, ls);
				zcurveIndex.removeValueAtIndex(place.getPoint().getZCurve(), id);
			}

			return id;
		}
		catch (Exception ex) {
			throw handleException("An error occured while in deletePlace", ex);
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/getPlace")
	public Place getPlace(@QueryParam("id") Long id) {
		try {
			ColumnQuery<Long, String, String> query = HFactory.createColumnQuery(keyspace, ls, ss, ss);
			query.setColumnFamily(columnFamily).setKey(id).setName(ColumnPlace);
			QueryResult<HColumn<String, String>> result = query.execute();

			if (result.get() == null || result.get().getValue() == null || result.get().getValue().length() == 0)
				return new Place(id, true);
			else
				return Place.fromString(result.get().getValue());
		}
		catch (Exception ex) {
			throw handleException("An error occured while in getPlace", ex);
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/getPlaces")
	public PlaceMap getPlaces(@FormParam("ids") IdList ids) {
		try {
			long startTime = System.currentTimeMillis();
			PlaceMap list = new PlaceMap();
			MultigetSliceQuery<Long, String, String> query = HFactory.createMultigetSliceQuery(keyspace, ls, ss, ss);
			query.setColumnFamily(columnFamily);
			query.setKeys(ids);
			query.setColumnNames(ColumnPlace);

			QueryResult<Rows<Long, String, String>> result = query.execute();
			Rows<Long, String, String> rows = result.get();

			for (Row<Long, String, String> row : rows) {
				HColumn<String, String> column = row.getColumnSlice().getColumnByName(ColumnPlace);

				if (column == null || column.getValue() == null || column.getValue().length() == 0)
					list.add(new Place(row.getKey(), true));
				else
					list.add(Place.fromString(column.getValue()));
			}
			
			getPlacesAvgTime.addTime(startTime);
			getPlacesCalls.incrementCall();

			return list;
		}
		catch (Exception ex) {
			throw handleException("An error occured while in getPlaces", ex);
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/getNearest")
	public PlaceList getNearest(@FormParam("point") GeoPoint point, @FormParam("limit") Integer limit) {
		try {
			long startTime = System.currentTimeMillis();
			long zcurve = point.getZCurve();
			List<IndexItem<Long>> items = zcurveIndex.getValueRangesForIndex(zcurve, false, limit, zcurveSameIndexLimit);
			items.addAll(zcurveIndex.getValueRangesForIndex(zcurve, true, limit, zcurveSameIndexLimit));

			IdList ids = new IdList();
			for (IndexItem<Long> item : items) {
				if (item.getValues().size() >= zcurveSameIndexLimit)
					log.error("INTERNAL ERROR:  Too many Place.Points have the same zcurve.  Increase 'geocandra.zcurve.same.index.limit' in the properties file.");

				for (Long id : item.getValues()) {
					ids.add(id);
				}
			}

			PlaceList list = getPlaces(ids).toPlaceList();
			list.sortByDistanceFrom(point);
			list.truncateTo(limit);

			getNearestAvgTime.addTime(startTime);
			getNearestCalls.incrementCall();
			
			return list;
		}
		catch (Exception ex) {
			throw handleException("An error occured while in getNearest", ex);
		}
	}

	public static GeocandraException handleException(String msg, Exception ex) {
		if (errorCounter != null)
			errorCounter.increment();
		if (lastErrorCounter != null)
			lastErrorCounter.updateLastAccess();

		if (ex instanceof GeocandraException) {
			log.error(msg + " - " + ex.getMessage());
			return (GeocandraException) ex;
		}
		else {
			log.error(msg, ex);
			return new GeocandraException(msg);
		}
	}
}
