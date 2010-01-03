Description
-----------
This library provides a Log4J Appender [1] that writes log events to the
MongoDB document oriented database [2].


Author
------
Peter Monks (pmonks@gmail.com)


Pre-requisites
--------------
JDK 1.6+
MongoDB Server v1.0+ (tested with 1.2.1) (required for unit tests)
MongoDB Java Driver v1.0+ (tested with 1.2)
Log4J 1.2+ (tested with 1.2.15 - note: won't work on earlier versions due to
            log4j API changes)


Installation / Configuration
----------------------------
1. Start a local MongoDB server running on the default port - this is required
   for the unit tests

2. Build the JAR file using Maven2 ("mvn clean package")

3. Deploy the produced JAR file, along with the MongoDB Java Driver JAR, into
   the classpath of your Java application

4. Configure log4j as usual, referring to the log4j.properties.sample file for
   the specific properties of this appender


Todos
-----
* Confirm that dates are being written correctly.  They appear to be getting
  converted to Strings (via Date.toString() - blech!!) before being written.

* Clean up BSONification code - currently it's functional but skanky.
  Consider using daybreak for this [4].

* Add unit tests
  * test contents of logged events, not just document counts
  * authentication
  * exceptions (including nested exceptions) are stored correctly


References
----------
[1] http://logging.apache.org/log4j/1.2/index.html
[2] http://www.mongodb.org/
[3] http://github.com/mongodb/mongo-java-driver/downloads
[4] http://github.com/maxaf/daybreak
