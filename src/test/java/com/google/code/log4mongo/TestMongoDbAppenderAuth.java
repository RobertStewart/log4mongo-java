package com.google.code.log4mongo;

import org.apache.log4j.PropertyConfigurator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.mongodb.DBCollection;
import com.mongodb.Mongo;

/**
 * Authentication-related JUnit unit tests for MongoDbAppender.
 * 
 * Note: these tests require that a MongoDB server is running, and (by default)
 * assumes that server is listening on the default port (27017) on localhost.
 * 
 * @author Robert Stewart (robert@wombatnation.com)
 * @version $Id$
 */
public class TestMongoDbAppenderAuth
{

    private final static String TEST_MONGO_SERVER_HOSTNAME = "localhost";
    private final static int TEST_MONGO_SERVER_PORT = 27017;
    private final static String TEST_DATABASE_NAME = "log4mongotestauth";
    private final static String TEST_COLLECTION_NAME = "logevents";

    private final static String LOG4J_AUTH_PROPS = "src/test/resources/log4j_auth.properties";

    private final static String username = "open";
    private final static String password = "sesame";

    private final Mongo mongo;
    private DBCollection collection;

    public TestMongoDbAppenderAuth() throws Exception
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
        // Ensure both the appender and the JUnit test use the same
        // collection object - provides consistency across reads (JUnit) &
        // writes (Log4J)
        collection = mongo.getDB(TEST_DATABASE_NAME).getCollection(
                TEST_COLLECTION_NAME);
        collection.drop();

        mongo.getDB(TEST_DATABASE_NAME).requestStart();
    }

    @After
    public void tearDown() throws Exception
    {
        mongo.getDB(TEST_DATABASE_NAME).requestDone();
    }

    /**
     * Catching the RuntimeException thrown when Log4J calls the MongoDbAppender
     * activeOptions() method isn't easy, since it is thrown in another thread.
     */
    @Test(expected = RuntimeException.class)
    @Ignore
    public void testAppenderActivateNoAuth()
    {
        PropertyConfigurator.configure(LOG4J_AUTH_PROPS);
    }

    /**
     * Adds the user to the test database before activating the appender.
     */
    @Test
    public void testAppenderActivateWithAuth()
    {
        mongo.getDB(TEST_DATABASE_NAME).addUser(username,
                password.toCharArray());
        PropertyConfigurator.configure(LOG4J_AUTH_PROPS);
    }

}
