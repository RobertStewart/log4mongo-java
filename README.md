Log4mongo-java
================
[Project site](http://log4mongo.org/display/PUB/Log4mongo+for+Java)

[Source code on GitHub](http://github.com/log4mongo/log4mongo-java)

# Description
This library provides Log4J Appenders [1] that write log events to the
MongoDB document oriented database [2].

* MongoDbAppender - Stores a BSONified version of the Log4J LoggingEvent
* ExtendedMongoDbAppender - Extends MongoDbAppender by allowing you to add top level elements
* MongoDbPatternLayoutAppender - Uses standard Log4J pattern layout, parser
    and converter classes to store a log message as a custom-formatted document
    
More details are at the [Project site](http://log4mongo.org/display/PUB/Log4mongo+for+Java)

# Authors
* Peter Monks (pmonks@gmail.com)
* Robert Stewart (robert@wombatnation.com)

# Contributors
* Jozef Sevcik (sevcik@styxys.com)
* Zach Bailey (znbailey@gmail.com)
* Gabriel Eisbruch (gabrieleisbruch@gmail.com)
* cskinfill
* Mick Knutson
* Jay Patel

# Pre-requisites
* JDK 1.8+
* MongoDB Server v3.0+ (tested with 3.4.1)
* MongoDB Java Driver v3.0+ (tested with 3.4.1)
* Log4J 1.2+ (tested with 1.2.17 - note: tests won't work on earlier versions due to Log4J API changes)
* Privateer (used only in unit tests - a copy is in the lib dir, in case you can't get it
from the central Maven repo)

## Additional dependency notes:
* Version 0.3.2 of log4mongo-java will work with MongoDB Java driver versions only up
to 2.0. The 2.1 driver includes a source compatible, but binary incompatible, change to
a DBCollection.insert() method used by log4mongo-java.

	
# Installation / Build / Configuration
If you downloaded a pre-built jar file, skip step 4.

1. Start local MongoDB servers running as a replica set. This is required for the replica set
part of the unit tests. The --smallfiles arg makes the unit tests run about twice as fast,
since databases are created and dropped several times, though it generally should not
be used in production. The --noprealloc and --nojournal options are also to speed up tests
and should not generally be used in production.
    
        $ sudo mkdir -p /data/r{0,1,2}
        $ sudo chown -r `whoami` /data
        $ mongod --replSet foo --smallfiles --noprealloc --nojournal --port 27017 --dbpath /data/r0
        $ mongod --replSet foo --smallfiles --noprealloc --nojournal --port 27018 --dbpath /data/r1
        $ mongod --replSet foo --smallfiles --noprealloc --nojournal --port 27019 --dbpath /data/r2
    
2. If this is the first time you have set up this replica set, you'll need to initiate it from the mongo shell:

        $ mongo
        > config = {"_id": "foo", members:[{_id: 0, host: '127.0.0.1:27017'},{_id: 1, host: '127.0.0.1:27018'},{_id: 2, host: '127.0.0.1:27019', arbiterOnly: true}]}
        > rs.initiate(config)

3. Wait about a minute until the replica set is established. You can run rs.status() in the mongo shell to look for direct confirmation it is ready.

4. Build the JAR file using Maven2. The following command will run all the unit tests.

        $ mvn clean package

5. Deploy the target/log4mongo-java-x.y.jar file, along with the Log4J and MongoDB
Java driver jars, into the classpath of your Java application

6. Configure log4j as usual, referring to the log4j.properties.sample file for
the specific configuration properties the appender supports. The Java package for
the classes changed to org.log4mongo in the 0.7 release, so make sure you specify
the fully qualified class name of the appender class correctly.

The TestMongoDbAppenderHosts test case tests logging to replica sets. See notes in that test case
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
