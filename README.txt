Description
-----------
This library provides a Log4J Appender [1] that writes log events to the
MongoDB document oriented database [2].


Author
------
Peter Monks (pmonks@gmail.com)

Contributors
------
Jozef Sevcik (sevcik@styxys.com)


Pre-requisites
--------------
JDK 1.6+
MongoDB Server v1.0+ (tested with 1.2.1, 1.4.x) (required for unit tests)
MongoDB Java Driver v1.0+ (tested with 1.2, 1.4)
Log4J 1.2+ (tested with 1.2.15 - note: won't work on earlier versions due to
            log4j API changes)


Installation / Configuration
----------------------------
1. Start a local MongoDB server running on the default port - this is required
   for the unit tests:
       mongod -dbpath ./mongodata

2. Build the JAR file using Maven2
       mvn clean package

3. Deploy the target/log4mongo-x.y.jar file, along with the Log4J and MongoDB
   Java Driver JARs, into the classpath of your Java application

4. Configure log4j as usual, referring to the log4j.properties.sample file for
   the specific configuration properties this appender supports


Todos
-----
* Add unit tests
  * authentication
  
* Clean up BSONification code - currently it's functional but skanky.
  Consider using daybreak for this [4].

  
Notes on Date handling
-----
MongoDB (actually BSON) supports datetimes as a native data type [5] 
and all drivers are supposed to handle conversion from client-native 
date type (java.util.Date in Java) to BSON representation of date in miliseconds
since the Unix epoch. 
However, MongoDB built-in console (bin/mongo) does represent dates formatted,
even the dates were saved in native data type, which may be confusing [6].
See testTimestampStoredNatively in tests (TestMongoDbAppender.java) if you want to get an idea.

References
----------
[1] http://logging.apache.org/log4j/1.2/index.html
[2] http://www.mongodb.org/
[3] http://github.com/mongodb/mongo-java-driver/downloads
[4] http://github.com/maxaf/daybreak
[5] http://bsonspec.org/#/specification
[6] http://groups.google.com/group/mongodb-user/browse_thread/thread/e59cbc8c9ba30411/af061b4bdbce5287
