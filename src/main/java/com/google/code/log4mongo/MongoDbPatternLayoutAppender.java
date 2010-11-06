/*
 * Copyright (C) 2010 Robert Stewart (robert@wombatnation.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.google.code.log4mongo;

import org.apache.log4j.spi.ErrorCode;
import org.apache.log4j.spi.LoggingEvent;

import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.util.JSON;

/**
 * A Log4J Appender that uses a PatternLayout to write log events into a MongoDB database.
 * <p>
 * The conversion pattern specifies the format of a JSON document. The document can contain
 * sub-documents and the elements can be strings or arrays. 
 * <p>
 * For some Log4J appenders (especially file appenders) blank space padding is often used to
 * get fields in adjacent rows to line up. For example, %-5p is often used to make all log levels
 * the same width in characters. Since each value is stored in a separate property in the document,
 * it usually doesn't make sense to use blank space padding with MongoDbPatternLayoutAppender.
 * <p>
 * The appender does <u>not</u> create any indexes on the data that's stored. If query performance
 * is required, indexes must be created externally (e.g., in the mongodb shell or an external reporting
 * application).
 * 
 * @author Robert Stewart (robert@wombatnation.com)
 * @see http://logging.apache.org/log4j/1.2/apidocs/org/apache/log4j/Appender.html
 * @see http://www.mongodb.org/
 * @version $Id$
 */
public class MongoDbPatternLayoutAppender extends MongoDbAppender
{
    @Override
    public boolean requiresLayout()
    {
        return (true);
    }

    /**
     * Inserts a BSON representation of a LoggingEvent into a MongoDB collection.
     * A PatternLayout is used to format a JSON document containing data available
     * in the LoggingEvent and, optionally, additional data returned by custom PatternConverters.
     * <p>
     * The format of the JSON document is specified in the .layout.ConversionPattern property.
     *  
     * @param loggingEvent The LoggingEvent that will be formatted and stored in MongoDB
     */
    @Override
    protected void append(final LoggingEvent loggingEvent)
    {
        if (isInitialized())
        {
            DBObject bson = null;
            String json = layout.format(loggingEvent);

            if (json.length() > 0)
            {
                Object obj = JSON.parse(json);
                if (obj instanceof DBObject)
                {
                    bson = (DBObject) obj;
                }
            }

            if (bson != null)
            {
                try {
                    getCollection().insert(bson);
                } catch (MongoException e) {
                    errorHandler.error("Failed to insert document to MongoDB", e,
                            ErrorCode.WRITE_FAILURE);
                }
            }
        }
    }

}

