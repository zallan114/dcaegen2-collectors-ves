/*
 * ============LICENSE_START=======================================================
 * PROJECT
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2018 Nokia. All rights reserved.s
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.dcae;

import com.att.nsa.drumlin.till.nv.impl.nvReadableStack;
import com.att.nsa.drumlin.till.nv.impl.nvReadableTable;
import com.att.nsa.drumlin.till.nv.rrNvReadable;
import com.google.common.annotations.VisibleForTesting;
import io.vavr.Function1;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import org.apache.commons.configuration.ConfigurationConverter;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Paths;

/**
 * Abstraction over application configuration.
 * Its job is to provide easily discoverable (by method names lookup) and type safe access to configuration properties.
 */
public class ApplicationSettings {

    private static final Logger inlog = LoggerFactory.getLogger(ApplicationSettings.class);
    private static final String COLLECTOR_PROPERTIES = "etc/collector.properties";
    private final PropertiesConfiguration properties = new PropertiesConfiguration();

    public ApplicationSettings(String[] args, Function1<String[], Map<String, String>> argsParser) {
        properties.setDelimiterParsingDisabled(true);
        Map<String, String> parsedArgs = argsParser.apply(args);
        loadProperties(Paths.get(new File(COLLECTOR_PROPERTIES).getAbsolutePath()).toString());
        loadCommandLineProperties(parsedArgs);
        parsedArgs.filterKeys(k -> !k.equals("c")).forEach(this::updateProperty);
    }

    private void loadCommandLineProperties(Map<String, String> parsedArgs) {
        parsedArgs.get("c").forEach(e -> {
            properties.clear();
            loadProperties(e);
        });
    }

    private void loadProperties(String property){
        try {
            properties.load(property);
        } catch (ConfigurationException ex) {
            inlog.error("Cannot load properties cause:", ex);
            throw new RuntimeException(ex);
        }
    }

    public String validAuthorizationCredentials() {
        return properties.getString("header.authlist", null);
    }

    public int maximumAllowedQueuedEvents() {
        return properties.getInt("collector.inputQueue.maxPending", 1024 * 4);
    }

    public boolean jsonSchemaValidationEnabled() {
        return properties.getInt("collector.schema.checkflag", -1) > 0;
    }

    public boolean authorizationEnabled() {
        return properties.getInt("header.authflag", 0) > 0;
    }

    public JSONObject jsonSchema() {
        return new JSONObject(
                properties.getString("collector.schema.file", "{\"v5\":\"./etc/CommonEventFormat_28.3.json\"}"));
    }

    public int httpPort() {
        return properties.getInt("collector.service.port", 8080);
    }

    public int httpsPort() {
        return properties.getInt("collector.service.secure.port", 8443);
    }

    public boolean httpsEnabled() {
        return httpsPort() > 0;
    }

    public boolean eventTransformingEnabled() {
        return properties.getInt("event.transform.flag", 1) > 0;
    }

    public String keystorePasswordFileLocation() {
        return properties.getString("collector.keystore.passwordfile", "./etc/passwordfile");
    }

    public String keystoreFileLocation() {
        return properties.getString("collector.keystore.file.location", "../etc/keystore");
    }

    public String keystoreAlias() {
        return properties.getString("collector.keystore.alias", "tomcat");
    }

    public String exceptionConfigFileLocation() {
        return properties.getString("exceptionConfig", null);
    }

    public String cambriaConfigurationFileLocation() {
        return properties.getString("collector.dmaapfile", "./etc/DmaapConfig.json");
    }

    public Map<String, String[]> dMaaPStreamsMapping() {
        String streamIdsProperty = properties.getString("collector.dmaap.streamid", null);
        if (streamIdsProperty == null) {
            return HashMap.empty();
        } else {
            return convertDMaaPStreamsPropertyToMap(streamIdsProperty);
        }
    }

    /*
     * Kept back here for backward compatibility.
     * RestfulCollectorServlet upon its initialization requires options to be represented
     * as object represented by rrNvReadable interface, so we define a a handy transformation function here.
     */
    public rrNvReadable torrNvReadable() {
        final nvReadableStack settings = new nvReadableStack();
        settings.push(new nvReadableTable(ConfigurationConverter.getProperties(properties)));
        return settings;
    }

    private Map<String, String[]> convertDMaaPStreamsPropertyToMap(String streamIdsProperty) {
        java.util.HashMap<String, String[]> domainToStreamIdsMapping = new java.util.HashMap<>();
        String[] topics = streamIdsProperty.split("\\|");
        for (String t : topics) {
            String domain = t.split("=")[0];
            String[] streamIds = t.split("=")[1].split(",");
            domainToStreamIdsMapping.put(domain, streamIds);
        }
        return HashMap.ofAll(domainToStreamIdsMapping);
    }

    private void updateProperty(String key, String value) {
        if (properties.containsKey(key)) {
            properties.setProperty(key, value);
        } else {
            properties.addProperty(key, value);
        }
    }

    @VisibleForTesting
    String getStringDirectly(String key) {
        return properties.getString(key);
    }
}
