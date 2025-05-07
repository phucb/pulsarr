/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ai.platon.pulsar.common.config;

import ai.platon.pulsar.common.measure.ByteUnit;

import java.awt.*;
import java.time.Duration;
import java.time.Instant;

@SuppressWarnings("unused")
public interface AppConstants {

    String PULSAR_CONTEXT_CONFIG_LOCATION = "classpath*:/pulsar-beans/app-context.xml";

    String YES_STRING = "y";

    String ALL_BATCHES = "all";

    /**
     * The first non-null Unicode character is U+0001, which is the Start of Heading (SOH) character.
     * @link <a href="https://en.wikipedia.org/wiki/UTF-8">UTF-8</a>
     * */
    Character UNICODE_FIRST_CODE_POINT = '\u0001';
    /**
     * The last Unicode character in the Unicode standard is U+10FFFF, which is the highest code point in Unicode.
     * @link <a href="https://en.wikipedia.org/wiki/UTF-8">UTF-8</a>
     * */
    Character UNICODE_LAST_CODE_POINT = '\uFFFF';

    /**
     * An example of the shortest url
     * */
    String SHORTEST_VALID_URL = "http://t.tt";
    /**
     * The length of the shortest example url
     * */
    int SHORTEST_VALID_URL_LENGTH = SHORTEST_VALID_URL.length();
    /**
     * The example url
     * */
    String EXAMPLE_URL = "http://example.com";
    /**
     * The prefix of all pulsar specified urls
     * */
    String INTERNAL_URL_PREFIX = "http://internal.pulsar.platon.ai";
    /**
     * The url of the nil page
     * */
    String NIL_PAGE_URL = INTERNAL_URL_PREFIX + "/nil";
    /**
     * The url of the top page
     * */
    String URL_TRACKER_HOME_URL = INTERNAL_URL_PREFIX + "/url/tracker";

    /**
     * Storage
     * */
    String MEM_STORE_CLASS = "org.apache.gora.memory.store.MemStore";
    /** A minimal file backend store */
    String FILE_BACKEND_STORE_CLASS = "ai.platon.pulsar.persist.gora.FileBackendPageStore";
    String MONGO_STORE_CLASS = "org.apache.gora.mongodb.store.MongoStore";
    String HBASE_STORE_CLASS = "org.apache.gora.hbase.store.HBaseStore";
    // schema version 1.10.x
//    String WEBPAGE_SCHEMA = "webpage110";
    /**
     * Schema version 1.12.x, has to be compatible with webpage110
     * */
    String WEBPAGE_SCHEMA = "webpage120";

    /**
     * Fetch
     * */
    int DISTANCE_INFINITE = 10000;
    Duration FETCH_TASK_TIMEOUT_DEFAULT = Duration.ofMinutes(10);

    /**
     * Parse
     * */
    Instant TCP_IP_STANDARDIZED_TIME = Instant.parse("1982-01-01T00:00:00Z");
    Instant MIN_ARTICLE_PUBLISH_TIME = Instant.parse("1995-01-01T00:00:00Z");
    Duration DEFAULT_MAX_PARSE_TIME = Duration.ofSeconds(60);

    int MAX_LINK_PER_PAGE = 4000;

    /**
     * Local file commands
     * */
    String CMD_PROXY_POOL_DUMP = "dump-proxy-pool";

    String CMD_PROXY_FORCE_IDLE = "IPS-force-idle";
    String CMD_PROXY_RECONNECT = "IPS-reconnect";
    String CMD_PROXY_DISCONNECT = "IPS-disconnect";

    /**
     * Browser
     * */
    int DEFAULT_BROWSER_MAX_OPEN_TABS = 50;

    double BROWSER_TAB_REQUIRED_MEMORY = ByteUnit.GIB.toBytes(1.5); // at least 1.5 GiB to open a new tab

    double DEFAULT_BROWSER_RESERVED_MEMORY = ByteUnit.GIB.toBytes(2.0); // 3 GiB

    double DEFAULT_BROWSER_RESERVED_MEMORY_MIB = ByteUnit.BYTE.toMiB(DEFAULT_BROWSER_RESERVED_MEMORY); // 5 GiB

    Duration POLLING_DRIVER_TIMEOUT_DEFAULT = Duration.ofSeconds(60);

    Dimension DEFAULT_VIEW_PORT = new Dimension(1920, 1080);
    String PULSAR_META_INFORMATION_ID = "PulsarMetaInformation";
    String PULSAR_META_INFORMATION_SELECTOR = "#" + PULSAR_META_INFORMATION_ID;
    String PULSAR_SCRIPT_SECTION_ID = "PulsarScriptSection";
    String PULSAR_SCRIPT_SECTION_SELECTOR = "#" + PULSAR_SCRIPT_SECTION_ID;
    String PULSAR_DOCUMENT_NORMALIZED_URI = "normalizedURI";
    String PULSAR_ATTR_HIDDEN = "_h";
    String PULSAR_ATTR_OVERFLOW_HIDDEN = "_oh";
    String PULSAR_ATTR_OVERFLOW_VISIBLE = "_visible";
    String PULSAR_ATTR_ELEMENT_NODE_VI = "vi";
    String PULSAR_ATTR_TEXT_NODE_VI = "tv";

    String PULSAR_ATTR_COMPUTED_STYLE = "cs";
    String PULSAR_ATTR_ELEMENT_NODE_DATA = "nd";

    // Browser use tool
    String BROWSER_INTERACTIVE_ELEMENTS_SELECTOR = "a, button, input, select, textarea, " +
                "[role='button'], [role='link'], [onclick], [onmousedown], [onmouseup]";

    /**
     * Other notable properties:
     * overflow
     * text-overflow
     * */
    String CLIENT_JS_PROPERTY_NAMES = "font-size, color, background-color";

    /**
     * Metrics
     * */
    String DEFAULT_METRICS_NAME = "pulsar";

    /**
     * SQL engine
     * */
    String H2_SESSION_FACTORY = "ai.platon.pulsar.ql.h2.H2SessionFactory";

    /**
     * Local file base url, the host is a fake host.
     * Consider just use http://localhost.
     * */
    String LOCAL_FILE_BASE_URL = "http://localfile.org";

    String BROWSER_SPECIFIC_URL_PREFIX = "http://browser-specific.org";
}
