
# Earnstone Geocandra

### Description
Geocandra is a REST based geo-location database service built on top of cassandra.
Although Geocandra can be used as an in-memory service it is designed to be run as
a REST service allowing easy access from clients like javascript, php, java, etc.
Some of it's main strengths are querying for nearest places based on GPS coordinates
and finding the nearest place given an IP address.

## ---(4/2/2011) THIS IS AN ACTIVE WORK IN PROGRESS SOME LINKS AND METHODS MAY BE INCOMLETE---

### How to Use Geocandra
Download the complete application (geocandra-0.1-all.zip) from our repository located 
[here](https://github.com/earnstone/maven-repo/tree/master/releases/com/earnstone/geo/geocandra/0.1). 
Extract the files and start/stop the server using the following commands 

    Usage: server.sh [-d] {start|stop|run|restart|check|supervise} [ CONFIGS ... ] 
    $./bin/server.sh start
    $./bin/server.sh stop

To check on the server status navigate to `http://localhost:43622`

A REST client (GeocandraClient) for java can be found in the geocandra-client.jar.  

For multi-machine configuration make sure to change the data center and worker ids 
located in ./config/geocandra.properties
