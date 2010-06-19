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

import org.apache.log4j.helpers.FormattingInfo;
import org.apache.log4j.helpers.PatternConverter;
import org.apache.log4j.helpers.PatternParser;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Simple PatternParser that adds a single PatternConverter for logging some
 * extra info. The extra info returned from convert(LoggingEvent) will replace
 * %e in the pattern when an event is logged if the logging style is set to
 * PatternLayout.
 */
public class CustomPatternParser extends PatternParser
{
    static final char EXTRA_CHAR = 'e';

    public CustomPatternParser(String pattern)
    {
        super(pattern);
    }

    /**
     * This method is called on each pattern converter character while the
     * PatternParser superclass is parsing the pattern. If the character is for
     * a custom converter handled by this PatternParser subclass, this class
     * adds the appropriate converter to a LinkedList of converters. If not, it
     * allows the superclass to handle the converter character.
     * 
     * @see org.apache.log4j.helpers.PatternParser#finalizeConverter(char)
     */
    public void finalizeConverter(char formatChar)
    {
        PatternConverter pc = null;
        switch (formatChar)
        {
        case EXTRA_CHAR:
            pc = new ExtraInfoPatternConverter(formattingInfo);
            currentLiteral.setLength(0);
            addConverter(pc);
            break;

        default:
            super.finalizeConverter(formatChar);
        }
    }

    /**
     * Custom PatternConverter for replacing a converter character (in this
     * case, the character happens to be 'e') with some additional info. For a
     * real application, this might be a session ID or some other piece of
     * meaningful info.
     */
    private class ExtraInfoPatternConverter extends PatternConverter
    {
        ExtraInfoPatternConverter(FormattingInfo formatInfo)
        {
            super(formatInfo);
        }

        /**
         * Returns the string that will replace %e in the pattern string.
         */
        public String convert(LoggingEvent event)
        {
            return "useful info";
        }
    }
}
