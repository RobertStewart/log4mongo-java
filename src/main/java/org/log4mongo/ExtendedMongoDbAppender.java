package org.log4mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.bson.BSONObject;

import java.util.LinkedHashMap;
import java.util.Map;


/**
 * This appender is designed so you can add top level elements to each logging
 * entry. Users can also extend MongoDbAppender themselves in order to add the
 * top level elements.
 * <p>
 * Use case: A desire to use a common appender for unified logs across different
 * code bases, such that commonly logged elements be consistent, such as
 * application, eventType, etc. This is enabled by adding a property called
 * rootLevelProperties with a key=value list of elements to be added to the root
 * level log. See log4j.properties.sample for an example.
 *
 * @author Mick Knutson (http://www.baselogic.com)
 */
public class ExtendedMongoDbAppender extends MongoDbAppender {

	private DBObject constants;

	private Map <String, String> rootProperties = new LinkedHashMap <String, String>();

	/**
	 * @see org.apache.log4j.AppenderSkeleton#activateOptions()
	 */
	@Override
	public void activateOptions() {
		super.activateOptions();
		initTopLevelProperties();
	}

	/**
	 * Initialize custom top level elements to appear in a log event
	 * <p>
	 * Allows users to create custom properties to be added to the top level
	 * log event.
	 */
	public void initTopLevelProperties() {
		constants = new BasicDBObject();
		if ( !rootProperties.isEmpty() ) {
			constants.putAll( rootProperties );
		}
	}

	/**
	 * This will handle spaces and empty values
	 * A = minus- & C=equals= & E==F
	 * For XML, must escape (&amp;)
	 *
	 * @param rootLevelProperties
	 */
	public void setRootLevelProperties( String rootLevelProperties ) {
		for ( String keyValue : rootLevelProperties.split( " *& *" ) ) {
			String[] pairs = keyValue.split( " *= *", 2 );
			rootProperties.put( pairs[ 0 ], pairs.length == 1 ? "" : pairs[ 1 ] );
		}
	}

	/**
	 * @param bson The BSON object to insert into a MongoDB database collection.
	 */
	@Override
	public void append( BSONObject bson ) {
		if ( this.isInitialized() && bson != null ) {
			if ( constants != null ) {
				bson.putAll( constants );
			}
			super.append( bson );
		}
	}
}
