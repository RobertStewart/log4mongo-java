package org.log4mongo;

import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.util.JSON;

import java.util.Date;

import org.apache.log4j.Layout;
import org.apache.log4j.spi.ErrorCode;
import org.apache.log4j.spi.LoggingEvent;
import org.bson.Document;

/**
 * A Log4J Appender that uses a PatternLayout to write log events into a MongoDB database.
 * This appender is same as the MongoDbPatternLayoutAppender, only difference is that the
 * MongoDbPatternLayoutDateAppender will save pattern layout 'timestamp'(%d) in the mongodb as a
 * Date type instead of String.
 * <p>
 * The conversion pattern specifies the format of a JSON document. The document can contain
 * sub-documents and the elements can be strings or arrays.
 * <p>
 * For some Log4J appenders (especially file appenders) blank space padding is often used to get
 * fields in adjacent rows to line up. For example, %-5p is often used to make all log levels the
 * same width in characters. Since each value is stored in a separate property in the document, it
 * usually doesn't make sense to use blank space padding with MongoDbPatternLayoutDateAppender.
 * <p>
 * The appender does <u>not</u> create any indexes on the data that's stored. If query performance
 * is required, indexes must be created externally (e.g., in the mongo shell or an external
 * reporting application).
 *
 */
public class MongoDbPatternLayoutDateAppender extends MongoDbAppender {

    @Override
    public boolean requiresLayout() {
        return (true);
    }

    /**
     * Inserts a BSON representation of a LoggingEvent into a MongoDB collection. A PatternLayout is
     * used to format a JSON document containing data available in the LoggingEvent and, optionally,
     * additional data returned by custom PatternConverters. Here timestamp is stored as a Date in mongodb.
     * <p>
     * The format of the JSON document is specified in the .layout.ConversionPattern property.
     *
     * @param loggingEvent
     *            The LoggingEvent that will be formatted and stored in MongoDB
     */
    @Override
    protected void append(final LoggingEvent loggingEvent) {
        if (isInitialized()) {
            DBObject bson = null;
            String json = layout.format(loggingEvent);

            if (json.length() > 0) {
                Object obj = JSON.parse(json);
                if (obj instanceof DBObject) {
                    bson = (DBObject) obj;
                    String dateKey = getDateKeyFromPatternLayout(layout);
                    if (dateKey != null) {
                        //saving time stamp as a date instead of string
                        bson.put(dateKey, new Date(loggingEvent.getTimeStamp()));
                    }
                }
            }

            if (bson != null) {
                try {
                    getCollection().insertOne(new Document(bson.toMap()));
                } catch (MongoException e) {
                    errorHandler.error("Failed to insert document to MongoDB", e,
                            ErrorCode.WRITE_FAILURE);
                }
            }
        }
    }

    /**
     * this method returns the key of the date which is mentioned in layout pattern
     * @param layout
     * @return key of Date (%d)
     */
    private String getDateKeyFromPatternLayout(Layout layout) {
        String dateKey = null;
        String conversionPattern = ((MongoDbPatternLayout)layout).getConversionPattern();
        String[] splitPattern = conversionPattern.split(",");
        for (String pattern : splitPattern) {
            if (pattern.contains("%d")) {
                dateKey = pattern.split(":")[0];
            }
        }
        // here we are removing double quotes '"' and opening curly braces '{' from the Key
        if (dateKey != null) {
            dateKey = dateKey.replaceAll("\"|\"$", "");
            dateKey = dateKey.replaceAll("\\{", "");
        }
        return dateKey;
    }

}
