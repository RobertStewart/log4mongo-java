package org.log4mongo;

import static org.junit.Assert.assertNotNull;

import java.util.Date;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

/**
 * JUnit unit tests for PatternLayout style logging with Date as a BSON object.
 * <p>
 * Since tests may depend on different Log4J property settings, each test reconfigures an appender
 * using a Properties object.
 * <p>
 * Note: these tests require that a MongoDB server is running, and (by default) assumes that server
 * is listening on the default port (27017) on localhost.
 */
public class TestMongoDbPatternLayoutDate {

    public static final String TEST_MONGO_SERVER_HOSTNAME = "localhost";

    public static final int TEST_MONGO_SERVER_PORT = 27017;

    private static final String TEST_DATABASE_NAME = "log4mongotest";

    private static final String TEST_COLLECTION_NAME = "logeventslayout";

    private static final String APPENDER_NAME = "MongoDBPatternLayout";

    private final MongoClient mongo;

    private MongoCollection collection;

    public TestMongoDbPatternLayoutDate() throws Exception {
        mongo = new MongoClient(TEST_MONGO_SERVER_HOSTNAME, TEST_MONGO_SERVER_PORT);
    }

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        MongoClient mongo = new MongoClient(TEST_MONGO_SERVER_HOSTNAME, TEST_MONGO_SERVER_PORT);
        mongo.dropDatabase(TEST_DATABASE_NAME);
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        MongoClient mongo = new MongoClient(TEST_MONGO_SERVER_HOSTNAME, TEST_MONGO_SERVER_PORT);
        mongo.dropDatabase(TEST_DATABASE_NAME);
    }

    @Before
    public void setUp() throws Exception {
        // Ensure both the appender and the JUnit test use the same collection
        // object - provides consistency across reads (JUnit) & writes (Log4J)
        collection = mongo.getDatabase(TEST_DATABASE_NAME).getCollection(TEST_COLLECTION_NAME);
        collection.drop();
    }

    /**
     * Here the timestamp we get from DB is Date type. Hence it is valid here to type cast timestamp into java.util.Date
     * and it does not throw any exception. If timestamp is stored as string in the db then it will not cast into the
     * java.util.Date and it will throw typecast exception.
     */
    @Test(expected = Test.None.class)
    public void testDateStoredInMongodbIsISOObject() {
        PropertyConfigurator.configure(getValidPatternLayoutProperties());

        MongoDbAppender appender = (MongoDbAppender) Logger.getRootLogger().getAppender(
                APPENDER_NAME);

        collection = mongo.getDatabase(TEST_DATABASE_NAME).getCollection(TEST_COLLECTION_NAME);
        appender.setCollection(collection);

        FindIterable<DBObject> entries = collection.find(DBObject.class);
        for (DBObject entry : entries) {
            assertNotNull(entry);
            //here date is type cast into Date. It will not throw the exception as the date saved in DB is ISODate
            Date date =  (Date)entry.get("timestamp");
        }
    }

    private Properties getValidPatternLayoutProperties() {
        Properties props = new Properties();
        props.put("log4j.rootLogger", "DEBUG, MongoDBPatternLayout");
        props.put("log4j.appender.MongoDBPatternLayout",
                "org.log4mongo.MongoDbPatternLayoutDateAppender");
        props.put("log4j.appender.MongoDBPatternLayout.databaseName", "log4mongotest");
        props.put("log4j.appender.MongoDBPatternLayout.layout", "org.log4mongo.CustomPatternLayout");
        props.put(
                "log4j.appender.MongoDBPatternLayout.layout.ConversionPattern",
                "{\"extra\":\"%e\",\"timestamp\":\"%d{DATE}\",\"level\":\"%p\",\"class\":\"%c{1}\",\"message\":\"%m\"}");
        return props;
    }
}
