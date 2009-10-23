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
MongoDB Java Driver v0.11+
Log4J 1.2+ (tested with 1.2.15)


Installation / Configuration
----------------------------
1. Build the JAR file using Maven2
   You will likely have to install the MongoDB Java Driver into a local Maven
   repository manually.  It is available from [3].
2. Deploy the produced JAR file, along with the MongoDB Java Driver JAR, into
   the classpath your own Java application.
3. Configure log4j as usual, referring to the log4j.properties.sample file for
   the specific properties of this appender.


Todos
-----
* Confirm that dates are being written correctly.  They appear to be getting
  converted to Strings (via Date.toString() - blech!!) before being written.

* Clean up BSONification code - it's currently functional but inconsistent.
  Consider using daybreak [4].


References
----------
[1] http://logging.apache.org/log4j/1.2/index.html
[2] http://www.mongodb.org/
[3] http://github.com/mongodb/mongo-java-driver/downloads
[4] http://github.com/maxaf/daybreak
