package com.google.code.log4mongo;

import org.apache.log4j.spi.LoggingEvent;

import com.mongodb.DBObject;

/**
 * Interface implemented by classes that create a BSON representation of a
 * Log4J LoggingEvent. LoggingEventBsonifierImpl is the default implementation.
 */
public interface LoggingEventBsonifier
{
    DBObject bsonify(LoggingEvent loggingEvent);
}
