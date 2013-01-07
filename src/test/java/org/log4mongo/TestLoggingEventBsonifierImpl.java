package org.log4mongo;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.InvocationTargetException;

import org.junit.Test;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class TestLoggingEventBsonifierImpl {

    @Test
    public void testStringBuffer() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        LoggingEventBsonifierImplSubclass bsonifier = new LoggingEventBsonifierImplSubclass();
        DBObject bson = new BasicDBObject();
        String key = "thekey";
        StringBuffer sb = new StringBuffer("thevalue");
        bsonifier.publicNullSafePut(bson, key, sb);
        String retrievedValue = (String) bson.get(key);
        assertEquals(sb.toString(), retrievedValue);
    }
    
    // Create a subclass so I can test a protected method
    // Replace this after extending Privateer to support superclasses in method signature
    public class LoggingEventBsonifierImplSubclass extends LoggingEventBsonifierImpl {
        public void publicNullSafePut(DBObject bson, final String key, final Object value) {
            nullSafePut(bson, key, value);
        }
        
    }
}
