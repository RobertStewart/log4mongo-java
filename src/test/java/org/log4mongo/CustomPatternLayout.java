/* Copyright (C) 2010 Robert Stewart (robert@wombatnation.com)
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
 */

package org.log4mongo;

import org.apache.log4j.helpers.PatternParser;

/**
 * Example PatternLayout that specifies a custom PatternParser.
 */
public class CustomPatternLayout extends MongoDbPatternLayout {
    public CustomPatternLayout() {
    }

    public CustomPatternLayout(String pattern) {
	super(pattern);
    }

    public PatternParser createPatternParser(String pattern) {
	PatternParser parser;
	if (pattern == null)
	    parser = new CustomPatternParser(DEFAULT_CONVERSION_PATTERN);
	else
	    parser = new CustomPatternParser(pattern);

	return parser;
    }
}
