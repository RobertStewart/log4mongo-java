/* Copyright (C) 2010 Robert Stewart (robert@wombatnation.com)
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
