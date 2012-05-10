package com.earnstone.geo;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;

import junit.framework.Assert;

import me.prettyprint.cassandra.model.BasicColumnFamilyDefinition;
import me.prettyprint.cassandra.service.ThriftCfDef;
import me.prettyprint.cassandra.testutils.EmbeddedServerHelper;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.ddl.ColumnFamilyDefinition;
import me.prettyprint.hector.api.ddl.ComparatorType;
import me.prettyprint.hector.api.ddl.KeyspaceDefinition;
import me.prettyprint.hector.api.factory.HFactory;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.earnstone.geo.Place.PlaceType;

public class GeocandraServiceInMemoryTest {

	GeoPoint pattersonCA = new GeoPoint(37.4957, -121.2546);
	GeoPoint tracyCA = new GeoPoint(37.7051, -121.4802);
	GeoPoint paloAltoCA = new GeoPoint(37.4135, -122.1312);
	GeoPoint austinTx = new GeoPoint(30.2700, -97.8662);
	GeoPoint fargoND = new GeoPoint(46.9346, -97.2297);
	GeoPoint sanLuisObispoCA = new GeoPoint(35.2678, -120.6591);

	private static final Logger log = LoggerFactory.getLogger(GeocandraService.class);

	protected static EmbeddedServerHelper embedded;
	protected static IGeocandra service;

	public static void initAndStartEmbeddedCassandra(Properties properties) throws Exception {
		if (embedded == null) {
			embedded = new EmbeddedServerHelper();
			embedded.setup();

			Cluster cluster = HFactory.getOrCreateCluster(properties.getProperty("geocandra.cassandra.cluster.name"), properties.getProperty("geocandra.cassandra.hosts"));

			BasicColumnFamilyDefinition columnFamilyDefinition1 = new BasicColumnFamilyDefinition();
			columnFamilyDefinition1.setKeyspaceName(properties.getProperty("geocandra.cassandra.keyspace.name"));
			columnFamilyDefinition1.setName(properties.getProperty("geocandra.cassandra.columnfamily.name"));
			columnFamilyDefinition1.setComparatorType(ComparatorType.UTF8TYPE);
			ColumnFamilyDefinition cfDef1 = new ThriftCfDef(columnFamilyDefinition1);

			BasicColumnFamilyDefinition columnFamilyDefinition2 = new BasicColumnFamilyDefinition();
			columnFamilyDefinition2.setKeyspaceName(properties.getProperty("geocandra.cassandra.keyspace.name"));
			columnFamilyDefinition2.setName(properties.getProperty("geocandra.cassandra.columnfamily.index.name"));
			columnFamilyDefinition2.setComparatorType(ComparatorType.LONGTYPE);
			ColumnFamilyDefinition cfDef2 = new ThriftCfDef(columnFamilyDefinition2);

			KeyspaceDefinition keyspaceDefinition = HFactory.createKeyspaceDefinition(properties.getProperty("geocandra.cassandra.keyspace.name"), "org.apache.cassandra.locator.SimpleStrategy", 1,
					Arrays.asList(cfDef1, cfDef2));
			cluster.addKeyspace(keyspaceDefinition);
		}
	}

	@BeforeClass
	public static void setup() throws Exception {
		Properties properties = new Properties();
		InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("geocandra.properties");
		properties.load(in);
		in.close();

		initAndStartEmbeddedCassandra(properties);

		service = new GeocandraService(properties);
	}

	@AfterClass
	public static void teardown() throws Exception {
		//embedded.teardown();
	}

	@Test
	public void savePlace() {
		Place place1 = new Place();
		place1.setId(null);
		place1.setName("name");
		place1.setPlaceType(PlaceType.City.toString());
		place1.setPoint(new GeoPoint(0.0, 0.0));

		Long id = service.savePlace(place1);
		Assert.assertNotNull(id);

		place1.setId(id);

		Place rplace1 = service.getPlace(place1.getId());
		Assert.assertEquals("name", rplace1.getName());

		place1.setName("update");
		service.savePlace(place1);
		rplace1 = service.getPlace(place1.getId());
		Assert.assertEquals("update", rplace1.getName());
	}

	@Test
	public void savePlaceInvalidProperties() {

		log.error("TESTING ERROR HANDLING **EXPECT 5 ERRORS IN LOGS INCLUDING THIS ONE**");

		Place place1 = new Place();
		place1.setPlaceType(null);
		place1.setPoint(new GeoPoint(0.0, 0.0));

		try {
			service.savePlace(place1);
			Assert.fail("Expected null PlaceType to fail");
		}
		catch (Exception ex) {
		}

		place1.setPlaceType(PlaceType.City.toString() + "_INVALID");

		try {
			service.savePlace(place1);
			Assert.fail("Expected invalid PlaceType to fail");
		}
		catch (Exception ex) {
		}

		place1.setPlaceType(PlaceType.Location.toString());
		place1.setPoint(null);

		try {
			service.savePlace(place1);
			Assert.fail("Expected null point to fail");
		}
		catch (Exception ex) {
		}
	}

	@Test
	public void deletePlace() {

		Place place1 = new Place();
		place1.setPlaceType(PlaceType.City.toString());
		place1.setPoint(new GeoPoint(0.0, 0.0));

		place1.setId(service.savePlace(place1));

		Long dId = service.deletePlace(place1.getId());
		Assert.assertNotNull(dId);

		Place rplace1 = service.getPlace(place1.getId());
		Assert.assertEquals(new Boolean(true), rplace1.isDeleted());

		Long id = 0l;
		dId = service.deletePlace(id);
		Assert.assertNotNull(dId);

		rplace1 = service.getPlace(id);
		Assert.assertEquals(new Boolean(true), rplace1.isDeleted());

		PlaceMap rlist1 = service.getPlaces(new IdList(Arrays.asList(place1.getId(), id)));
		Assert.assertEquals(2, rlist1.size());
		Assert.assertEquals(new Boolean(true), rlist1.get(place1.getId()).isDeleted());
		Assert.assertEquals(new Boolean(true), rlist1.get(id).isDeleted());
	}

	@Test
	public void getPlaces() {
		Place place1 = new Place();
		place1.setName("place1");
		place1.setPlaceType(PlaceType.City.toString());
		place1.setPoint(new GeoPoint(0.0, 0.0));

		Place place2 = new Place();
		place2.setName("place2");
		place2.setPlaceType(PlaceType.City.toString());
		place2.setPoint(new GeoPoint(0.0, 0.0));

		Place place3 = new Place();
		place3.setName("place3");
		place3.setPlaceType(PlaceType.City.toString());
		place3.setPoint(new GeoPoint(0.0, 0.0));

		place1.setId(service.savePlace(place1));
		place2.setId(service.savePlace(place2));
		place3.setId(service.savePlace(place3));
		service.deletePlace(place2.getId());

		PlaceMap rlist1 = service.getPlaces(new IdList(Arrays.asList(place1.getId(), place2.getId(), place3.getId())));
		Assert.assertEquals(3, rlist1.size());
		Assert.assertEquals(place1.getId(), rlist1.get(place1.getId()).getId());
		Assert.assertEquals(null, rlist1.get(place1.getId()).isDeleted());
		Assert.assertEquals(place2.getId(), rlist1.get(place2.getId()).getId());
		Assert.assertEquals((Boolean) true, rlist1.get(place2.getId()).isDeleted());
		Assert.assertEquals(place3.getId(), rlist1.get(place3.getId()).getId());
		Assert.assertEquals(null, rlist1.get(place3.getId()).isDeleted());
	}

	@Test
	public void getNearest() {
		Place place1 = new Place();
		place1.setPlaceType(PlaceType.City.toString());
		place1.setName("pattersonCA");
		place1.setPoint(pattersonCA);
		place1.setId(service.savePlace(place1));

		Place place2 = new Place();
		place2.setPlaceType(PlaceType.City.toString());
		place2.setName("tracyCA");
		place2.setPoint(tracyCA);
		place2.setId(service.savePlace(place2));

		Place place3 = new Place();
		place3.setPlaceType(PlaceType.City.toString());
		place3.setName("paloAltoCA");
		place3.setPoint(paloAltoCA);
		place3.setId(service.savePlace(place3));

		Place place4 = new Place();
		place4.setPlaceType(PlaceType.City.toString());
		place4.setName("austinTx");
		place4.setPoint(austinTx);
		place4.setId(service.savePlace(place4));

		Place place5 = new Place();
		place5.setPlaceType(PlaceType.City.toString());
		place5.setName("fargoND");
		place5.setPoint(fargoND);
		place5.setId(service.savePlace(place5));

		Place place6 = new Place();
		place6.setPlaceType(PlaceType.City.toString());
		place6.setName("sanLuisObispoCA");
		place6.setPoint(sanLuisObispoCA);
		place6.setId(service.savePlace(place6));

		PlaceList rlist = service.getNearest(pattersonCA, 1);
		Assert.assertEquals("pattersonCA", rlist.get(0).getName());

		rlist = service.getNearest(new GeoPoint(pattersonCA.getLatitude() + 0.0001, pattersonCA.getLongitude() + 0.0001), 1);
		Assert.assertEquals("pattersonCA", rlist.get(0).getName());

		rlist = service.getNearest(new GeoPoint(pattersonCA.getLatitude() + 0.0001, pattersonCA.getLongitude() - 0.0001), 1);
		Assert.assertEquals("pattersonCA", rlist.get(0).getName());

		rlist = service.getNearest(new GeoPoint(pattersonCA.getLatitude() - 0.0001, pattersonCA.getLongitude() + 0.0001), 1);
		Assert.assertEquals("pattersonCA", rlist.get(0).getName());

		rlist = service.getNearest(new GeoPoint(pattersonCA.getLatitude() - 0.0001, pattersonCA.getLongitude() - 0.0001), 1);
		Assert.assertEquals("pattersonCA", rlist.get(0).getName());
	}
}
