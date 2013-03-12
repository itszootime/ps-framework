package org.uncertweb.ps.handler.json.gson;

import java.io.FileReader;
import java.io.IOException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.uncertweb.ps.data.Input;
import org.uncertweb.ps.data.Request;
import org.uncertweb.ps.data.SingleInput;
import org.uncertweb.ps.handler.json.gson.RequestDeserializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Ignore
public class RequestDeserializerTest {
	
	private static Request request;
	
	@BeforeClass
	public static void setUp() throws IOException {
		// create gson
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(Request.class, new RequestDeserializer());
		Gson gson = builder.create();
		
		// read from file
		FileReader reader = new FileReader("src/test/resources/simple-request.json");
		request = gson.fromJson(reader, Request.class);
		reader.close();
	}
	
	@Test
	public void inputA() {
		Input inputA = request.getInputs().get("InputA");
		Assert.assertTrue(inputA instanceof SingleInput);
		Object object = inputA.getAsSingleInput().getObject();
		Assert.assertTrue(object instanceof String);
		Assert.assertEquals("hello", object);
	}

}
