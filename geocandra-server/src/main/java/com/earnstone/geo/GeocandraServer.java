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

import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.sun.jersey.spi.container.servlet.ServletContainer;

public class GeocandraServer {

	private static final Logger log = LoggerFactory.getLogger(GeocandraServer.class);
	
	public static int DefaultPort = 43622;	
	public static Server server;
	public static Properties properties;

	public static synchronized void initialize(Properties properties) throws Exception {
		if (server == null) {
			log.info("Initializing GeocandraServer");

			GeocandraServer.properties = properties;
			
			// The will cause the jersey servlet to stop logging
			// those stupid info messages to stderr
			java.util.logging.Logger jerseyLogger = java.util.logging.Logger.getLogger("com.sun.jersey");
			jerseyLogger.setLevel(java.util.logging.Level.SEVERE);

			int port = Integer.parseInt(properties.getProperty("geocandra.server.port", Integer.toString(DefaultPort)));
			server = new Server(port);

			ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
			context.setContextPath("/");
			server.setHandler(context);

			ServletHolder sh = new ServletHolder(ServletContainer.class);
			sh.setInitParameter("javax.ws.rs.Application", "com.earnstone.geo.GeocandraApplication");					
			sh.setInitParameter("com.sun.jersey.config.property.packages", "com.earnstone.geo");			
			context.addServlet(sh, "/*");

			log.info("Finished initializing GeocandraServer");
		}
	}

	public static synchronized void start() throws Exception {
		server.start();
		log.info("Starting GeocandraServer");
	}

	public static synchronized void stop() throws Exception {
		server.stop();
		log.info("Stopping GeocandraServer");
		server = null;
	}

	public static void main(String[] args) throws Exception {

		InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("geocandra.properties");

		if (in == null) {
			log.error("Couldn't find geocandra.properties resource.");
			log.error("GeocandraServer was not started");
		}
		else {
			properties = new Properties();
			properties.load(in);
			in.close();

			initialize(properties);
			start();
			server.join();
		}
	}
}
