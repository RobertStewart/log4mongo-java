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
