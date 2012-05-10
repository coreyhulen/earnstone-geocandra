package com.earnstone.geo;

import java.io.InputStream;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * Because of the common interface between the client and server this class just
 * uses the in-memory tests remotely.
 */
public class GeocandraServiceRemoteTest extends GeocandraServiceInMemoryTest {

	@BeforeClass
	public static void setup() throws Exception {
		
		Properties properties = new Properties();
		InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("geocandra.properties");
		properties.load(in);
		in.close();
		
		initAndStartEmbeddedCassandra(properties);
		
		GeocandraServer.initialize(properties);
		GeocandraServer.start();

		service = new GeocandraClient();
	}

	@AfterClass
	public static void teardown() throws Exception {
		GeocandraServer.stop();
		//embedded.teardown();
	}
}
