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

import org.apache.log4j.Logger;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

import junit.framework.TestCase;


/**
 * JUnit unit tests for MongoDbAppender.
 * 
 * Note: these tests require that a MongoDB server is running, and (by default)
 * assumes that server is listening on the default port (27017) on localhost.
 *
 * @author Peter Monks (peter.monks@alfresco.com)
 * @version $Id$
 */
public class TestMongoDbAppender
    extends TestCase
{
    private final static Logger log = Logger.getLogger(TestMongoDbAppender.class);
    
    private final static String TEST_MONGO_SERVER_HOSTNAME = "localhost";
    private final static int    TEST_MONGO_SERVER_PORT     = 27017;
    private final static String TEST_DATABASE_NAME         = "log4mongotest";
    private final static String TEST_COLLECTION_NAME       = "logevents";
    
    private final Mongo mongo;

    
    
    public TestMongoDbAppender()
        throws Exception
    {
        mongo = new Mongo(TEST_MONGO_SERVER_HOSTNAME, TEST_MONGO_SERVER_PORT);
    }
    

    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();
            
        mongo.dropDatabase(TEST_DATABASE_NAME);
    }
    
    
    public void testSingleLogEntry()
    {
        try
        {
            mongo.getDB(TEST_DATABASE_NAME).requestStart();
            
            log.trace("Trace entry");
            assertEquals(1L, countLogEntries());
            assertEquals(1L, countLogEntriesAtLevel("trace"));
            assertEquals(0L, countLogEntriesAtLevel("debug"));
            assertEquals(0L, countLogEntriesAtLevel("info"));
            assertEquals(0L, countLogEntriesAtLevel("warn"));
            assertEquals(0L, countLogEntriesAtLevel("error"));
            assertEquals(0L, countLogEntriesAtLevel("fatal"));
        }
        finally
        {
            mongo.getDB(TEST_DATABASE_NAME).requestDone();
        }
    }


    public void testAllLevels()
    {
        try
        {
            mongo.getDB(TEST_DATABASE_NAME).requestStart();
            
            log.trace("Trace entry");
            log.debug("Debug entry");
            log.info("Info entry");
            log.warn("Warn entry");
            log.error("Error entry");
            log.fatal("Fatal entry");
            
            assertEquals(6L, countLogEntries());
            assertEquals(1L, countLogEntriesAtLevel("trace"));
            assertEquals(1L, countLogEntriesAtLevel("debug"));
            assertEquals(1L, countLogEntriesAtLevel("info"));
            assertEquals(1L, countLogEntriesAtLevel("warn"));
            assertEquals(1L, countLogEntriesAtLevel("error"));
            assertEquals(1L, countLogEntriesAtLevel("fatal"));
        }
        finally
        {
            mongo.getDB(TEST_DATABASE_NAME).requestDone();
        }
    }
    
    
    public void testLogWithException()
    {
        try
        {
            mongo.getDB(TEST_DATABASE_NAME).requestStart();
            
            log.error("Error entry", new RuntimeException("Here is an exception!"));
            
            assertEquals(1L, countLogEntries());
        }
        finally
        {
            mongo.getDB(TEST_DATABASE_NAME).requestDone();
        }
    }
    
    
    public void testLogWithChainedExceptions()
    {
        try
        {
            mongo.getDB(TEST_DATABASE_NAME).requestStart();
            
            Exception rootCause = new RuntimeException("I'm the real culprit!");
            
            log.error("Error entry", new RuntimeException("I'm an innocent bystander.", rootCause));
            
            assertEquals(1L, countLogEntries());
        }
        finally
        {
            mongo.getDB(TEST_DATABASE_NAME).requestDone();
        }
    }
    
    
    public void testAuthentication()
    {
        //####TODO!!!!
    }
    
    
    private long countLogEntries()
    {
        return(mongo.getDB(TEST_DATABASE_NAME).getCollection(TEST_COLLECTION_NAME).getCount());
    }
    
    
    private long countLogEntriesAtLevel(final String level)
    {
        return(countLogEntriesWhere(BasicDBObjectBuilder.start().add("level", level.toUpperCase()).get()));
    }
    
    
    private long countLogEntriesWhere(final DBObject whereClause)
    {
        DBCursor cursor = mongo.getDB(TEST_DATABASE_NAME).getCollection(TEST_COLLECTION_NAME).find(whereClause);
        
        return(cursor.count());
    }

}
