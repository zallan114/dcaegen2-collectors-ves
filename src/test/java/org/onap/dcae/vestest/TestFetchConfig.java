/*-
 * ============LICENSE_START=======================================================
 * PROJECT
 * ================================================================================
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
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
package org.onap.dcae.vestest;

import static org.junit.Assert.*;

import static org.junit.Assert.assertEquals;

import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.json.JSONObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonObject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.dcae.commonFunction.CommonStartup;
import org.onap.dcae.commonFunction.DmaapPropertyReader;
import org.onap.dcae.commonFunction.EventProcessor;
import org.onap.dcae.commonFunction.EventPublisherHash;
import org.onap.dcae.controller.FetchDynamicConfig;


public class TestFetchConfig {

	JSONObject jsonObject;
	
	@Before
	public void setUp() throws Exception {

		
		System.setProperty("CONSUL_HOST", "localhost");
		System.setProperty("CONFIG_BINDING_SERVICE", "localhost");
		System.setProperty("HOSTNAME", "localhost");
	}

	@After
	public void tearDown() throws Exception {
	}
	

	
	@Test
	public void testwritefile() {
		Boolean flag = false;
		
		FetchDynamicConfig fc = new FetchDynamicConfig();
		//Used for writing the configuration output
		FetchDynamicConfig.configFile = "src/test/resources/controller-config_formatted_op.json";

		
		
		try{
			JsonParser parser = new JsonParser();
			FileReader fr = new FileReader ("src/test/resources/controller-config_singleline_ip.json"  );
			final JsonObject jo =  (JsonObject) parser.parse (fr);
			final String jsonText = jo.toString ();
			jsonObject = new JSONObject ( jsonText );
			
			fc.writefile(jsonObject.toString());
		}
		catch(Exception e){
			System.out.println("Exception while opening the file");
			e.printStackTrace();
		}
		if(jsonObject.has("streams_publishes"))
		{
			flag = true;
		}
		
		assertEquals(true, flag);
		
	}
	
	@Test
	public void testgetConsul() {
		Boolean flag = false;

		
		try{

			FetchDynamicConfig.getconsul();
			flag = true;
		}
		catch(Exception e){
			System.out.println("Exception while getting to consul");
			e.printStackTrace();
		}
		assertEquals(true, flag);
		
	}

	@Test
	public void testgetCBS() {
		Boolean flag = false;
		
		try{
			
			
			JsonParser parser = new JsonParser();
			FileReader fr = new FileReader ("src/test/resources/controller-config_singleline_ip.json"  );
			final JsonObject jo =  (JsonObject) parser.parse (fr);
			final String jsonText = jo.toString ();
			jsonObject = new JSONObject ( jsonText );
			
			//retstring from Consul is parsed
			FetchDynamicConfig.retString = "[{\"ID\":\"81bc2a17-8cfa-3f6f-30a9-a545a9b6ac2f\",\"Node\":\"zldcrdm5bdcc2dokr00\",\"Address\":\"135.25.108.161\",\"Datacenter\":\"zldcrdm5bdcc2\",\"TaggedAddresses\":{\"lan\":\"135.25.108.161\",\"wan\":\"135.25.108.161\"},\"NodeMeta\":{\"fqdn\":\"zldcrdm5bdcc2dokr00.2f3fb3.rdm5b.tci.att.com\"},\"ServiceID\":\"20299a144716:config_binding_service:10000\",\"ServiceName\":\"config_binding_service\",\"ServiceTags\":[],\"ServiceAddress\":\"135.25.108.161\",\"ServicePort\":10000,\"ServiceEnableTagOverride\":false,\"CreateIndex\":9153156,\"ModifyIndex\":9153156}]";
			
			//Mock the return CBS output
			FetchDynamicConfig.retCBSString = jsonObject.toString();
			FetchDynamicConfig.getCBS();
			flag = true;
		}
		catch(Exception e){
			System.out.println("Exception while fetching cbs configuration");
			e.printStackTrace();
		}
		assertEquals(true, flag);
		
	}
	

	@Test
	public void testverifyConfigChange() {
	
		
		Boolean ret= false;
		
		try{
			//File date to be compared
			FetchDynamicConfig.configFile = "src/test/resources/controller-config_singleline_ip.json";
			//Mock the return CBS output
			FetchDynamicConfig.retCBSString = "{\"header.authflag\": \"1\", \"collector.schema.file\": \"{\\\"v1\\\": \\\"./etc/CommonEventFormat_27.2.json\\\", \\\"v2\\\": \\\"./etc/CommonEventFormat_27.2.json\\\", \\\"v3\\\": \\\"./etc/CommonEventFormat_27.2.json\\\", \\\"v4\\\": \\\"./etc/CommonEventFormat_27.2.json\\\", \\\"v5\\\": \\\"./etc/CommonEventFormat_28.4.json\\\"}\", \"collector.keystore.passwordfile\": \"/opt/app/dcae-certificate/.password\", \"tomcat.maxthreads\": \"200\", \"collector.dmaap.streamid\": \"fault=ves-fault|syslog=ves-syslog|heartbeat=ves-heartbeat|measurementsForVfScaling=ves-measurement|mobileFlow=ves-mobileflow|other=ves-other|stateChange=ves-statechange|thresholdCrossingAlert=ves-thresholdCrossingAlert|voiceQuality=ves-voicequality|sipSignaling=ves-sipsignaling\", \"streams_subscribes\": {}, \"collector.inputQueue.maxPending\": \"8096\", \"collector.keystore.alias\": \"dynamically generated\", \"streams_publishes\": {\"ves-mobileflow\": {\"type\": \"message_router\", \"dmaap_info\": {\"client_id\": \"1517590629043\", \"client_role\": \"com.att.secCollector.member\", \"location\": \"rdm5bdcc2\", \"topic_url\": \"https://DMAAPHOST:3905/events/com.att.dcae.dmaap.FTL.24256-SEC-MOBILEFLOW-OUTPUT-v1\"}, \"aaf_username\": \"userid@namespace\", \"aaf_password\": \"authpwd\"}, \"ves-measurement\": {\"type\": \"message_router\", \"dmaap_info\": {\"client_id\": \"1517590433916\", \"client_role\": \"com.att.secCollector.member\", \"location\": \"rdm5bdcc2\", \"topic_url\": \"https://DMAAPHOST:3905/events/com.att.dcae.dmaap.FTL.24256-ENC-MEASUREMENT-OUTPUT-v1\"}, \"aaf_username\": \"userid@namespace\", \"aaf_password\": \"authpwd\"}, \"ves-voicequality\": {\"type\": \"message_router\", \"dmaap_info\": {\"client_id\": \"1517590778397\", \"client_role\": \"com.att.secCollector.member\", \"location\": \"rdm5bdcc2\", \"topic_url\": \"https://DMAAPHOST:3905/events/com.att.dcae.dmaap.FTL.24256-VES-VOICEQUALITY-OUTPUT-v1\"}, \"aaf_username\": \"userid@namespace\", \"aaf_password\": \"authpwd\"}, \"ves-thresholdCrossingAlert\": {\"type\": \"message_router\", \"dmaap_info\": {\"client_id\": \"1517590728150\", \"client_role\": \"com.att.secCollector.member\", \"location\": \"rdm5bdcc2\", \"topic_url\": \"https://DMAAPHOST:3905/events/com.att.dcae.dmaap.FTL.24256-SEC-TCA-OUTPUT-v1\"}, \"aaf_username\": \"userid@namespace\", \"aaf_password\": \"authpwd\"}, \"ves-fault\": {\"type\": \"message_router\", \"dmaap_info\": {\"client_id\": \"1517590384670\", \"client_role\": \"com.att.secCollector.member\", \"location\": \"rdm5bdcc2\", \"topic_url\": \"https://DMAAPHOST:3905/events/com.att.dcae.dmaap.FTL.24256-SEC-FAULT-OUTPUT-v1\"}, \"aaf_username\": \"userid@namespace\", \"aaf_password\": \"authpwd\"}, \"ves-heartbeat\": {\"type\": \"message_router\", \"dmaap_info\": {\"client_id\": \"1517590530041\", \"client_role\": \"com.att.secCollector.member\", \"location\": \"rdm5bdcc2\", \"topic_url\": \"https://DMAAPHOST:3905/events/com.att.dcae.dmaap.FTL.24256-SEC-HEARTBEAT-OUTPUT-v1\"}, \"aaf_username\": \"userid@namespace\", \"aaf_password\": \"authpwd\"}, \"ves-sipsignaling\": {\"type\": \"message_router\", \"dmaap_info\": {\"client_id\": \"1517590828736\", \"client_role\": \"com.att.secCollector.member\", \"location\": \"rdm5bdcc2\", \"topic_url\": \"https://DMAAPHOST:3905/events/com.att.dcae.dmaap.FTL.24256-VES-SIPSIGNALING-OUTPUT-v1\"}, \"aaf_username\": \"userid@namespace\", \"aaf_password\": \"authpwd\"}, \"ves-syslog\": {\"type\": \"message_router\", \"dmaap_info\": {\"client_id\": \"1517590482019\", \"client_role\": \"com.att.secCollector.member\", \"location\": \"rdm5bdcc2\", \"topic_url\": \"https://DMAAPHOST:3905/events/com.att.dcae.dmaap.FTL.24256-SEC-SYSLOG-OUTPUT-v1\"}, \"aaf_username\": \"userid@namespace\", \"aaf_password\": \"authpwd\"}, \"ves-other\": {\"type\": \"message_router\", \"dmaap_info\": {\"client_id\": \"1517590581045\", \"client_role\": \"com.att.secCollector.member\", \"location\": \"rdm5bdcc2\", \"topic_url\": \"https://DMAAPHOST:3905/events/com.att.dcae.dmaap.FTL.24256-SEC-OTHER-OUTPUT-v1\"}, \"aaf_username\": \"userid@namespace\", \"aaf_password\": \"authpwd\"}, \"ves-statechange\": {\"type\": \"message_router\", \"dmaap_info\": {\"client_id\": \"1517590677649\", \"client_role\": \"com.att.secCollector.member\", \"location\": \"rdm5bdcc2\", \"topic_url\": \"https://DMAAPHOST:3905/events/com.att.dcae.dmaap.FTL.24256-SEC-STATECHANGE-OUTPUT-v1\"}, \"aaf_username\": \"userid@namespace\", \"aaf_password\": \"authpwd\"}}, \"collector.schema.checkflag\": \"1\", \"services_calls\": {}, \"event.transform.flag\": \"1\", \"collector.keystore.file.location\": \"/opt/app/dcae-certificate/keystore.jks\", \"header.authlist\": \"sample1,c2FtcGxlMQ==|userid1,base64encodepwd1|userid2,base64encodepwd2\", \"collector.service.secure.port\": \"8443\", \"collector.service.port\": \"-1\"}";
			ret=FetchDynamicConfig.verifyConfigChange();
			
		}
		catch(Exception e){
			System.out.println("Exception on verifyConfigChange");
			//e.printStackTrace();
		}
		assertEquals(true, ret);
		
	}
	
}

