/*
 * Copyright (C) 2009 Peter Monks (pmonks@gmail.com)
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package com.google.code.log4mongo;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.ErrorCode;
import org.apache.log4j.spi.LocationInfo;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

import com.mongodb.BasicDBList;
import com.mongodb.Mongo;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;


/**
 * Log4J Appender that writes log events into the MongoDB document oriented database.  Log events are fully parsed and stored
 * as structured records in MongoDB (this appender does not require, nor use a Log4J layout).
 * 
 * The appender does <u>not</u> create any indexes on the data that's stored - it is assumed that if query performance is
 * required those would be created externally (eg. in the mongodb shell or an external reporting application).
 * 
 * An example BSON structure for a single log entry is as follows:
 * 
 * <pre>
 * {
 *   "_id"        : ObjectId("f1c0895fd5eee04a445deb00"),
 *   "timestamp"  : "Thu Oct 22 2009 16:46:29 GMT-0700 (Pacific Daylight Time)",
 *   "level"      : "ERROR",
 *   "thread"     : "main",
 *   "message"    : "Error entry",
 *   "fileName"   : "TestMongoDbAppender.java",
 *   "method"     : "testLogWithChainedExceptions",
 *   "lineNumber" : "147",
 *   "class"      : {
 *                    "fullyQualifiedClassName" : "com.google.code.log4mongo.TestMongoDbAppender",
 *                    "package"                 : [ "com", "google", "code", "log4mongo" ],
 *                    "className"               : "TestMongoDbAppender"
 *                  },
 *   "throwables" : [
 *                    {
 *                      "message"    : "I'm an innocent bystander.",
 *                      "stackTrace" : [
 *                                       {
 *                                         "fileName"   : "TestMongoDbAppender.java",
 *                                         "method"     : "testLogWithChainedExceptions",
 *                                         "lineNumber" : 147,
 *                                         "class"      : {
 *                                                          "fullyQualifiedClassName" : "com.google.code.log4mongo.TestMongoDbAppender",
 *                                                          "package"                 : [ "com", "google", "code", "log4mongo" ],
 *                                                          "className"               : "TestMongoDbAppender"
 *                                                        }
 *                                       },
 *                                       {
 *                                         "method"     : "invoke0",
 *                                         "lineNumber" : -2,
 *                                         "class"      : {
 *                                                          "fullyQualifiedClassName" : "sun.reflect.NativeMethodAccessorImpl",
 *                                                          "package"                 : [ "sun", "reflect" ],
 *                                                          "className"               : "NativeMethodAccessorImpl"
 *                                                        }
 *                                       },
 *                                       ... 8< ...
 *                                     ]
 *                    },
 *                    {
 *                      "message" : "I'm the real culprit!",
 *                      "stackTrace" : [
 *                                       {
 *                                         "fileName" : "TestMongoDbAppender.java",
 *                                         "method" : "testLogWithChainedExceptions",
 *                                         "lineNumber" : 145,
 *                                         "class" : {
 *                                                     "fullyQualifiedClassName" : "com.google.code.log4mongo.TestMongoDbAppender",
 *                                                     "package"                 : [ "com", "google", "code", "log4mongo" ],
 *                                                     "className"               : "TestMongoDbAppender"
 *                                                   }
 *                                       },
 *                                       ... 8< ...
 *                                     ]
 *                    }
 *                  ]
 * }
 * </pre>
 *
 * @author Peter Monks (peter.monks@alfresco.com)
 * @see http://logging.apache.org/log4j/1.2/apidocs/org/apache/log4j/Appender.html
 * @see http://www.mongodb.org/
 * @version $Id$
 */
public class MongoDbAppender
    extends AppenderSkeleton
{
    private final static String DEFAULT_MONGO_DB_HOSTNAME        = "localhost";
    private final static int    DEFAULT_MONGO_DB_PORT            = 27017;
    private final static String DEFAULT_MONGO_DB_DATABASE_NAME   = "log4mongo";
    private final static String DEFAULT_MONGO_DB_COLLECTION_NAME = "logevents";
    
    private String hostname       = DEFAULT_MONGO_DB_HOSTNAME;
    private int    port           = DEFAULT_MONGO_DB_PORT;
    private String databaseName   = DEFAULT_MONGO_DB_DATABASE_NAME;
    private String collectionName = DEFAULT_MONGO_DB_COLLECTION_NAME;
    private String userName       = null;
    private String password       = null;
    
    private DBCollection collection = null;
    

    /**
     * @see org.apache.log4j.Appender#requiresLayout()
     */
    public boolean requiresLayout()
    {
        return(false);
    }

 
    /**
     * @see org.apache.log4j.AppenderSkeleton#activateOptions()
     */
    @Override
    public void activateOptions()
    {
        try
        {
            Mongo mongo    = new Mongo(hostname, port);
            DB    database = mongo.getDB(databaseName);
            
            if (userName != null && userName.trim().length() > 0)
            {
                if (!database.authenticate(userName, password))
                {
                    throw new RuntimeException("Unable to authenticate with MongoDB server.");
                }
            }
            
            collection = database.getCollection(collectionName);
        }
        catch (Exception e)
        {
            errorHandler.error("Unexpected exception while initialising MongoDbAppender.", e, ErrorCode.GENERIC_FAILURE);
        }
    }

    
    /**
     * @see org.apache.log4j.AppenderSkeleton#append(org.apache.log4j.spi.LoggingEvent)
     */
    @Override
    protected void append(final LoggingEvent loggingEvent)
    {
        DBObject bson = bsonifyLoggingEvent(loggingEvent);
        
        if (bson != null)
        {
            collection.insert(bson);
        }
    }
    
    
    /**
     * BSONifies a single Log4J LoggingEvent object.
     * 
     * @param loggingEvent The LoggingEvent object to BSONify <i>(may be null)</i>.
     * @return The BSONified equivalent of the LoggingEvent object <i>(may be null)</i>.
     */
    private DBObject bsonifyLoggingEvent(final LoggingEvent loggingEvent)
    {
        DBObject result = null;

        if (loggingEvent != null)
        {
            result = new BasicDBObject();
            
            result.put("timestamp", new Date(loggingEvent.getTimeStamp()));
            nullSafePut(result, "level",   loggingEvent.getLevel().toString());
            nullSafePut(result, "thread",  loggingEvent.getThreadName());
            nullSafePut(result, "message", loggingEvent.getMessage());
            
            addLocationInformation(result, loggingEvent.getLocationInformation());
            addThrowableInformation(result, loggingEvent.getThrowableInformation());
        }
        
        return(result);
    }
    

    /**
     * Adds the LocationInfo object to an existing BSON object. 
     * 
     * @param bson         The BSON object to add the location info to <i>(must not be null)</i>.
     * @param locationInfo The LocationInfo object to add to the BSON object <i>(may be null)</i>.
     */
    private void addLocationInformation(DBObject bson, final LocationInfo locationInfo)
    {
        if (locationInfo != null)
        {
            nullSafePut(bson, "fileName",   locationInfo.getFileName());
            nullSafePut(bson, "method",     locationInfo.getMethodName());
            nullSafePut(bson, "lineNumber", locationInfo.getLineNumber());
            nullSafePut(bson, "class",      bsonifyClassName(locationInfo.getClassName()));
        }
    }
    
    
    /**
     * Adds the ThrowableInformation object to an existing BSON object.
     * 
     * @param bson          The BSON object to add the throwable info to <i>(must not be null)</i>.
     * @param throwableInfo The ThrowableInformation object to add to the BSON object <i>(may be null)</i>.
     */
    private void addThrowableInformation(DBObject bson, final ThrowableInformation throwableInfo)
    {
        if (throwableInfo != null)
        {
            Throwable currentThrowable = throwableInfo.getThrowable();
            List      throwables       = new BasicDBList();
            
            while (currentThrowable != null)
            {
                DBObject throwableBson = bsonifyThrowable(currentThrowable);
                
                if (throwableBson != null)
                {
                    throwables.add(throwableBson);
                }
                
                currentThrowable = currentThrowable.getCause();
            }
            
            if (throwables.size() > 0)
            {
                bson.put("throwables", throwables);
            }
        }
    }
    

    /**
     * BSONifies the given Throwable.
     * 
     * @param throwable The throwable object to BSONify <i>(may be null)</i>.
     * @return The BSONified equivalent of the Throwable object <i>(may be null)</i>.
     */
    private DBObject bsonifyThrowable(final Throwable throwable)
    {
        DBObject result = null;
       
        if (throwable != null)
        {
            result = new BasicDBObject();
            
            nullSafePut(result, "message",    throwable.getMessage());
            nullSafePut(result, "stackTrace", bsonifyStackTrace(throwable.getStackTrace()));
        }
        
        return(result);
    }
    
    
    /**
     * BSONifies the given stack trace.
     * 
     * @param stackTrace The stack trace object to BSONify <i>(may be null)</i>.
     * @return The BSONified equivalent of the stack trace object <i>(may be null)</i>.
     */
    private DBObject bsonifyStackTrace(final StackTraceElement[] stackTrace)
    {
        BasicDBList result = null;
        
        if (stackTrace != null && stackTrace.length > 0)
        {
            result = new BasicDBList();
            
            for (StackTraceElement element : stackTrace)
            {
                DBObject bson = bsonifyStackTraceElement(element);
                
                if (bson != null)
                {
                    result.add(bson);
                }
            }
        }
        
        return(result);
    }
    
    
    
    /**
     * BSONifies the given stack trace element.
     * 
     * @param element The stack trace element object to BSONify <i>(may be null)</i>.
     * @return The BSONified equivalent of the stack trace element object <i>(may be null)</i>.
     */
    private DBObject bsonifyStackTraceElement(final StackTraceElement element)
    {
        DBObject result = null;
        
        if (element != null)
        {
            result = new BasicDBObject();
            
            nullSafePut(result, "fileName",   element.getFileName());
            nullSafePut(result, "method",     element.getMethodName());
            nullSafePut(result, "lineNumber", element.getLineNumber());
            nullSafePut(result, "class",      bsonifyClassName(element.getClassName()));
        }
        
        return(result);
    }
    
    
    /**
     * BSONifies the given class name.
     * 
     * @param className The class name to BSONify <i>(may be null)</i>.
     * @return The BSONified equivalent of the class name <i>(may be null)</i>.
     */
    private DBObject bsonifyClassName(final String className)
    {
        DBObject result = null;
        
        if (className != null && className.trim().length() > 0)
        {
            result = new BasicDBObject();
            
            result.put("fullyQualifiedClassName", className);
            
            List     packageComponents   = new BasicDBList();
            String[] packageAndClassName = className.split("\\.");
            
            packageComponents.addAll(Arrays.asList(Arrays.copyOf(packageAndClassName, packageAndClassName.length - 1)));
            
            if (packageComponents.size() > 0)
            {
                result.put("package",   packageComponents);
            }
            
            result.put("className", packageAndClassName[packageAndClassName.length - 1]);
        }
        
        return(result);
    }


    /**
     * Adds the given value to the given key, except if it's null (in which case this method does nothing).
     * 
     * @param bson  The BSON object to add the key/value to <i>(must not be null)</i>.
     * @param key   The key of the object <i>(must not be null)</i>.
     * @param value The value of the object <i>(may be null)</i>.
     */
    private void nullSafePut(DBObject bson, final String key, final Object value)
    {
        if (value != null)
        {
            if (value instanceof String)
            {
                String stringValue = (String)value;
                
                if (stringValue.trim().length() > 0)
                {
                    bson.put(key, stringValue);
                }
            }
            else
            {
                bson.put(key, value);
            }
        }
    }
    
    
    /**
     * @see org.apache.log4j.Appender#close()
     */
    public void close()
    {
        collection = null;
    }

    
    /**
     * @return The hostname of the MongoDB server <i>(will not be null, empty or blank)</i>.
     */
    public String getHostname()
    {
        return(hostname);
    }


    /**
     * @param hostname The MongoDB hostname to set <i>(must not be null, empty or blank)</i>.
     */
    private void setHostname(final String hostname)
    {
        // PRECONDITIONS
        assert hostname != null             : "hostname must not be null";
        assert hostname.trim().length() > 0 : "hostname must not be empty or blank";
        
        // Body
        this.hostname = hostname;
    }
    
    
    /**
     * @return The port of the MongoDB server <i>(will be > 0)</i>.
     */
    public int getPort()
    {
        return(port);
    }


    /**
     * @param port The port to set <i>(must be > 0)</i>.
     */
    private void setPort(final int port)
    {
        // PRECONDITIONS
        assert port > 0 : "port must be > 0";
        
        // Body
        this.port = port;
    }
    
    
    /**
     * @return The database used in the MongoDB server <i>(will not be null, empty or blank)</i>.
     */
    public String getDatabaseName()
    {
        return(databaseName);
    }


    /**
     * @param databaseName The database to use in the MongoDB server <i>(must not be null, empty or blank)</i>.
     */
    public void setDatabaseName(final String databaseName)
    {
        // PRECONDITIONS
        assert databaseName != null             : "database must not be null";
        assert databaseName.trim().length() > 0 : "database must not be empty or blank";
        
        // Body
        this.databaseName = databaseName;
    }


    /**
     * @return The collection used within the database in the MongoDB server <i>(will not be null, empty or blank)</i>.
     */
    public String getCollectionName()
    {
        return(collectionName);
    }


    /**
     * @param collectionName The collection used within the database in the MongoDB server <i>(must not be null, empty or blank)</i>.
     */
    public void setCollectionName(final String collectionName)
    {
        // PRECONDITIONS
        assert collectionName != null             : "collection must not be null";
        assert collectionName.trim().length() > 0 : "collection must not be empty or blank";
        
        // Body
        this.collectionName = collectionName;
    }


    /**
     * @return The userName used to authenticate with MongoDB <i>(may be null)</i>.
     */
    public String getUserName()
    {
        return(userName);
    }


    /**
     * @param userName The userName to use when authenticating with MongoDB <i>(may be null)</i>.
     */
    public void setUserName(final String userName)
    {
        this.userName = userName;
    }


    /**
     * @return The password used to authenticate with MongoDB <i>(may be null)</i>.
     */
    public String getPassword()
    {
        return(password);
    }


    /**
     * @param password The password to use when authenticating with MongoDB <i>(may be null)</i>.
     */
    public void setPassword(final String password)
    {
        this.password = password;
    }
    

}
