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

package org.log4mongo.contrib;

import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.helpers.PatternConverter;
import org.apache.log4j.helpers.PatternParser;
import org.apache.log4j.spi.LoggingEvent;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * PatternParser that adds pattern converters for logging useful host-related info, specifically:
 * <ul>
 * <li>hostname</li>
 * <li>VM name (which often includes the pid) of the JVM on this host</li>
 * <li>IP address</li>
 * </ul>
 */
public class HostInfoPatternParser extends PatternParser {

    static final char HOST_NAME = 'H';

    static final char VM_NAME = 'V';

    static final char IP_ADDRESS = 'I';

    static final Map<String, PatternConverter> converters;

    static {
        Map<String, PatternConverter> tmp = new HashMap<String, PatternConverter>();
        tmp.put(String.valueOf(HOST_NAME), new HostPatternConverter());
        tmp.put(String.valueOf(VM_NAME), new VMNamePatternConverter());
        tmp.put(String.valueOf(IP_ADDRESS), new IPAddressPatternConverter());

        converters = Collections.unmodifiableMap(tmp);
    }

    public HostInfoPatternParser(String pattern) {
        super(pattern);
    }

    /**
     * This method is called on each pattern converter character while the PatternParser superclass
     * is parsing the pattern. If the character is for a custom converter handled by this
     * PatternParser subclass, this class adds the appropriate converter to a LinkedList of
     * converters. If not, it allows the superclass to handle the converter character.
     *
     * @see org.apache.log4j.helpers.PatternParser#finalizeConverter(char)
     */
    public void finalizeConverter(char formatChar) {
        PatternConverter pc = null;
        switch (formatChar) {
        case HOST_NAME:
            pc = HostInfoPatternParser.converters.get(String.valueOf(HOST_NAME));
            currentLiteral.setLength(0);
            addConverter(pc);
            break;
        case VM_NAME:
            pc = HostInfoPatternParser.converters.get(String.valueOf(VM_NAME));
            currentLiteral.setLength(0);
            addConverter(pc);
            break;
        case IP_ADDRESS:
            pc = HostInfoPatternParser.converters.get(String.valueOf(IP_ADDRESS));
            currentLiteral.setLength(0);
            addConverter(pc);
            break;

        default:
            super.finalizeConverter(formatChar);
        }
    }

    /**
     * Custom PatternConverter for replacing converter character 'H' with the host name.
     */
    private static class HostPatternConverter extends PatternConverter {

        private String hostname = "";

        HostPatternConverter() {
            super();

            try {
                hostname = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                LogLog.warn(e.getMessage());
            }
        }

        public String convert(LoggingEvent event) {
            return hostname;
        }
    }

    /**
     * Custom PatternConverter for replacing converter character 'V' with the VM name of the JVM,
     * usually formatted as pid@host.
     */
    private static class VMNamePatternConverter extends PatternConverter {

        private String process = "";

        VMNamePatternConverter() {
            super();

            process = ManagementFactory.getRuntimeMXBean().getName();
        }

        public String convert(LoggingEvent event) {
            return process;
        }
    }

    /**
     * Custom PatternConverter for replacing converter character 'I' with the IP Address.
     */
    private static class IPAddressPatternConverter extends PatternConverter {

        private String ipaddress = "";

        IPAddressPatternConverter() {
            super();

            try {
                ipaddress = InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException e) {
                LogLog.warn(e.getMessage());
            }
        }

        public String convert(LoggingEvent event) {
            return ipaddress;
        }
    }
}
