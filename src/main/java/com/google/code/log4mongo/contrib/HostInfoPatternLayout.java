package com.google.code.log4mongo.contrib;

import org.apache.log4j.helpers.PatternParser;

import com.google.code.log4mongo.MongoDbPatternLayout;

public class HostInfoPatternLayout extends MongoDbPatternLayout
{

	public HostInfoPatternLayout()
	{
	}

	public HostInfoPatternLayout(String pattern)
	{
		super(pattern);
	}

	public PatternParser createPatternParser(String pattern)
	{
		PatternParser parser;
		if (pattern == null)
			parser = new HostInfoPatternParser(DEFAULT_CONVERSION_PATTERN);
		else
			parser = new HostInfoPatternParser(pattern);

		return parser;
	}
}
