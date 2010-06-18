/*
 * Copyright (C) 2010 Robert Stewart (robert@wombatnation.com)
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

/**
 * JUnit unit tests for PatternLayout style logging.
 * 
 * Since many of the tests depend on different Log4J property settings, each
 * test reconfigures an appender using a Properties object.
 * 
 * For some Log4J appenders (especially file appenders) it is common to use
 * blank space padding to get fields in adjacent rows to line up. For example,
 * %-5p is often used to make all log levels the same width in characters. Since
 * each value is stored in a separate property in the document, it usually
 * doesn't make sense to use blank space padding with MongoDbAppender.
 * 
 * Note: these tests require that a MongoDB server is running, and (by default)
 * assumes that server is listening on the default port (27017) on localhost.
 * 
 * @author Robert Stewart (robert@wombatnation.com)
 * @version $Id$
 */
public class TestMongoDbPatternLayout
{
    private final static Logger log = Logger.getLogger(TestMongoDbPatternLayout.class);

    public final static String TEST_MONGO_SERVER_HOSTNAME = "localhost";
    public final static int TEST_MONGO_SERVER_PORT = 27017;
    private final static String TEST_DATABASE_NAME = "log4mongotest";
    private final static String TEST_COLLECTION_NAME = "logeventslayout";

    private final Mongo mongo;
    private DBCollection collection;

    public TestMongoDbPatternLayout() throws Exception
    {
        mongo = new Mongo(TEST_MONGO_SERVER_HOSTNAME, TEST_MONGO_SERVER_PORT);
    }

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        Mongo mongo = new Mongo(TEST_MONGO_SERVER_HOSTNAME,
                TEST_MONGO_SERVER_PORT);
        mongo.dropDatabase(TEST_DATABASE_NAME);
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception
    {
        Mongo mongo = new Mongo(TEST_MONGO_SERVER_HOSTNAME,
                TEST_MONGO_SERVER_PORT);
        mongo.dropDatabase(TEST_DATABASE_NAME);
    }

    @Before
    public void setUp() throws Exception
    {
        // Ensure both the appender and the JUnit test use the same collection
        // object - provides consistency across reads (JUnit) & writes (Log4J)
        collection = mongo.getDB(TEST_DATABASE_NAME).getCollection(TEST_COLLECTION_NAME);
        collection.drop();

        mongo.getDB(TEST_DATABASE_NAME).requestStart();
    }

    @After
    public void tearDown() throws Exception
    {
        mongo.getDB(TEST_DATABASE_NAME).requestDone();
    }

    @Test
    public void testValidPatternLayout()
    {
        PropertyConfigurator.configure(getValidPatternLayoutProperties());

        MongoDbAppender appender = (MongoDbAppender) Logger.getRootLogger().getAppender("MongoDB");

        collection = mongo.getDB(TEST_DATABASE_NAME).getCollection(TEST_COLLECTION_NAME);
        appender.setCollection(collection);

        assertEquals(0L, countLogEntries());
        log.warn("Warn entry");
        assertEquals(1L, countLogEntries());
        assertEquals(1L, countLogEntriesAtLevel("WARN"));

        // verify log entry content
        DBObject entry = collection.findOne();
        assertNotNull(entry);
        assertEquals("WARN", entry.get("level"));
        assertEquals("Warn entry", entry.get("message"));
        // This is the custom info. In the pattern, the field is named "extra".
        assertEquals("useful info", entry.get("extra"));
    }

    @Test
    public void testQuotesInMessage()
    {
        PropertyConfigurator.configure(getValidPatternLayoutProperties());

        MongoDbAppender appender = (MongoDbAppender) Logger.getRootLogger().getAppender("MongoDB");

        collection = mongo.getDB(TEST_DATABASE_NAME).getCollection(TEST_COLLECTION_NAME);
        appender.setCollection(collection);

        assertEquals(0L, countLogEntries());
        log.warn("\"Quotes\" \"embedded\"");
        assertEquals(1L, countLogEntries());
        assertEquals(1L, countLogEntriesAtLevel("WARN"));

        // verify log entry content
        DBObject entry = collection.findOne();
        assertNotNull(entry);
        assertEquals("WARN", entry.get("level"));
        assertEquals("\"Quotes\" \"embedded\"", entry.get("message"));
    }

    @Test
    public void testInvalidStyle()
    {
        PropertyConfigurator.configure(getInvalidStyleProperties());

        // This will cause the appender to log to the default error handler. The
        // default error handler logs to the console. One option for improving
        // this test would be to modify the appender to have a custom error
        // handler that retains the last error that was logged.
        MongoDbAppender appender = (MongoDbAppender) Logger.getRootLogger().getAppender("MongoDBInvalidStyle");

        collection = mongo.getDB(TEST_DATABASE_NAME).getCollection(TEST_COLLECTION_NAME);
        appender.setCollection(collection);

        // If logging style is invalid, attempts to log messages will be ignored
        assertEquals(0L, countLogEntries());
        log.trace("Trace entry");
        assertEquals(0L, countLogEntries());
    }

    @Test
    public void testPerformance()
    {
        PropertyConfigurator.configure(getValidPatternLayoutProperties());

        MongoDbAppender appender = (MongoDbAppender) Logger.getRootLogger().getAppender("MongoDB");

        collection = mongo.getDB(TEST_DATABASE_NAME).getCollection(TEST_COLLECTION_NAME);
        appender.setCollection(collection);

        int NUM_MESSAGES = 1000;
        long now = System.currentTimeMillis();
        for (int i = 0; i < NUM_MESSAGES; i++)
        {
            log.warn("Warn entry");
        }
        long dur = System.currentTimeMillis() - now;
        System.out.println("Millis to log " + NUM_MESSAGES + " messages:" + dur);
        assertEquals(NUM_MESSAGES, countLogEntries());
    }

    private long countLogEntries()
    {
        return (collection.getCount());
    }

    private long countLogEntriesAtLevel(final String level)
    {
        return (countLogEntriesWhere(BasicDBObjectBuilder.start().add("level", level.toUpperCase()).get()));
    }

    private long countLogEntriesWhere(final DBObject whereClause)
    {
        return collection.getCount(whereClause);
    }

    private Properties getValidPatternLayoutProperties()
    {
        Properties props = new Properties();
        props.put("log4j.rootLogger", "DEBUG, MongoDB");
        props.put("log4j.appender.MongoDB", "com.google.code.log4mongo.MongoDbAppender");
        props.put("log4j.appender.MongoDB.databaseName", "log4mongotest");
        props.put("log4j.appender.MongoDB.layout", "com.google.code.log4mongo.CustomPatternLayout");
        props.put("log4j.appender.MongoDB.layout.ConversionPattern",
                "{\"extra\":\"%e\",\"timestamp\":\"%d{yyyy-MM-dd'T'HH:mm:ss'Z'}\",\"level\":\"%p\",\"class\":\"%c{1}\",\"message\":\"%m\"}");
        props.put("log4j.appender.MongoDB.loggingStyle", "PatternLayout");
        return props;
    }

    private Properties getInvalidStyleProperties()
    {
        Properties props = new Properties();
        props.put("log4j.rootLogger", "DEBUG, MongoDBInvalidStyle");
        props.put("log4j.appender.MongoDBInvalidStyle", "com.google.code.log4mongo.MongoDbAppender");
        props.put("log4j.appender.MongoDBInvalidStyle.databaseName", "log4mongotest");
        props.put("log4j.appender.MongoDBInvalidStyle.layout", "com.google.code.log4mongo.CustomPatternLayout");
        props.put("log4j.appender.MongoDBInvalidStyle.layout.ConversionPattern",
                "{\"timestamp\":\"%d{yyyy-MM-dd'T'HH:mm:ss'Z'}\",\"level\":\"%p\"}");
        props.put("log4j.appender.MongoDBInvalidStyle.loggingStyle", "InvalidStyle");
        return props;
    }

}
