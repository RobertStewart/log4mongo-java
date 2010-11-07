Log4mongo-java
================
[Project site](http://log4mongo.org/display/PUB/Log4mongo+for+Java)

[Source code on GitHub](http://github.com/log4mongo/log4mongo-java)

# Description
This library provides Log4J Appenders [1] that write log events to the
MongoDB document oriented database [2].

* MongoDbAppender - Stores a BSONified version of the Log4J LoggingEvent
* MongoDbPatternLayoutAppender - Uses standard Log4J pattern layout, parser
    and converter classes to store a log message as a custom-formatted document

More details are at the [Project site](http://log4mongo.org/display/PUB/Log4mongo+for+Java)

# Author
Peter Monks (pmonks@gmail.com)

# Contributors
* Jozef Sevcik (sevcik@styxys.com)
* Robert Stewart (robert@wombatnation.com)
* Zach Bailey (znbailey@gmail.com)
* Gabriel Eisbruch (gabrieleisbruch@gmail.com)


# Pre-requisites
* JDK 1.5+
* MongoDB Server v1.+ (tested with 1.6.3) (required for unit tests)
* MongoDB Java Driver v2.0+, but not 2.2 (tested with 2.1 and 2.3)
* Log4J 1.2+ (tested with 1.2.16 - note: tests won't work on earlier versions due to
log4j API changes)
* Privateer (used in unit tests - a copy is in the lib dir, in case you can't get it
from the central Maven repo)

## Additional notes:
* Version 0.3.2 of log4mongo-java will work with MongoDB Java driver versions only up
to 2.0. The 2.1 driver includes a source compatible, but binary incompatible, change to
a DBCollection.insert() method used by log4mongo-java.
* The MongoDB Java driver 2.2 includes a bug that causes a NullPointerException if you run
mongod not in a replica set configuration. The bug was fixed in the 2.3 driver.


# Installation / Configuration
1. Start a local MongoDB server running on the default port - this is required
   for the unit tests. The --smallfiles arg makes the unit tests run about twice as fast,
   since databases are created and dropped several times, though it generally should not
   be used in production.
       mongod --smallfiles --dbpath ./mongodata

2. Build the JAR file using Maven2
       mvn clean package

3. Deploy the target/log4mongo-java-x.y.jar file, along with the Log4J and MongoDB
   Java Driver jars, into the classpath of your Java application

4. Configure log4j as usual, referring to the log4j.properties.sample file for
   the specific configuration properties the appender supports

The TestMongoDbAppenderHosts test case tests replica sets. See notes in that test case
for starting multiple mongod instances as a replica set.

# ToDos
* More unit tests
  * connection failures
  
* Clean up BSONification code - currently it's functional but skanky.
  Consider using daybreak for this [4].

  
# Notes on Date Handling
MongoDB (actually BSON) supports datetimes as a native data type [5] 
and all drivers are supposed to handle conversion from client-native 
date type (java.util.Date in Java) to BSON representation of date in miliseconds
since the Unix epoch.

However, MongoDB built-in console (bin/mongo) does represent dates formatted,
even the dates were saved in native data type, which may be confusing [6].
See testTimestampStoredNatively in tests (TestMongoDbAppender.java) if you want to get an idea.

# References
* [1] http://logging.apache.org/log4j/1.2/index.html
* [2] http://www.mongodb.org/
* [3] http://github.com/mongodb/mongo-java-driver/downloads
* [4] http://github.com/maxaf/daybreak
* [5] http://bsonspec.org/#/specification
* [6] http://groups.google.com/group/mongodb-user/browse_thread/thread/e59cbc8c9ba30411/af061b4bdbce5287
