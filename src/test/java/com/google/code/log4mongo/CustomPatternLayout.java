package com.google.code.log4mongo;

import org.apache.log4j.helpers.PatternParser;

/**
 * Example PatternLayout that specifies a custom PatternParser.
 */
public class CustomPatternLayout extends MongoDbPatternLayout
{
    public CustomPatternLayout()
    {
    }

    public CustomPatternLayout(String pattern)
    {
        super(pattern);
    }

    public PatternParser createPatternParser(String pattern)
    {
        PatternParser parser;
        if (pattern == null)
            parser = new CustomPatternParser(DEFAULT_CONVERSION_PATTERN);
        else
            parser = new CustomPatternParser(pattern);

        return parser;
    }
}
