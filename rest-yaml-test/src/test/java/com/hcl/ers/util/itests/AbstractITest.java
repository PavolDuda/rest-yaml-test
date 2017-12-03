package com.hcl.ers.util.itests;

import static com.jayway.restassured.config.EncoderConfig.encoderConfig;
import static com.jayway.restassured.config.RedirectConfig.redirectConfig;

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.hcl.ers.util.itests.beans.YamlInitGroup;
import com.hcl.ers.util.itests.beans.YamlTestGroup;
import com.hcl.ers.util.itests.data.TestData;
import com.hcl.ers.util.itests.util.JsonMapper;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.config.RestAssuredConfig;
import com.jayway.restassured.specification.RequestSpecification;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;


@RunWith(value = Parameterized.class)
public abstract class AbstractITest {
	
	public static final String ENV = "env";

	public Config conf;
	public int port;
	public String baseURL;

	public RequestSpecification rspec;
	
	public static TestData testData = new TestData(getEnv());
	
	
	@Before
	public void setUp() {
		this.conf = ConfigFactory.load("application-" + getEnv());
		this.baseURL = conf.getString("server.baseURI");
		this.port = conf.getInt("server.port");

		final RequestSpecBuilder build = new RequestSpecBuilder().setBaseUri(baseURL).setPort(port);

		rspec = build.build();
		RestAssured.config = new RestAssuredConfig()
								.encoderConfig(encoderConfig().defaultContentCharset("UTF-8"))
								.redirect(redirectConfig().followRedirects(true).and().maxRedirects(1));
		RestAssured.useRelaxedHTTPSValidation();
	}
	
	public static List<YamlTestGroup> getTestGroupData() {
		long startTime = System.currentTimeMillis();
		List<YamlTestGroup> groups = JsonMapper.toObject(testData.getTestData().getObject("testGroup", List.class), YamlTestGroup.class);
		long endTime = System.currentTimeMillis();
		
		System.out.println("total testGroup count="+groups.size()+" yaml parsing time in millis="+(endTime-startTime));
		return groups;
	}
	
	public static YamlInitGroup getInitGroupData() {
		YamlInitGroup initGroup = JsonMapper.toObject(testData.getTestData().getObject("initGroup", Map.class), YamlInitGroup.class);
		return initGroup;
	}
	
	private static String getEnv() {
		final String env = System.getProperty(AbstractITest.ENV);
		if (env != null) {
			return env;
		} else {
			return "dev";
		}
	}
}
