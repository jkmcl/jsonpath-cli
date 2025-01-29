package jkml;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

class JsonPathHelperTests {

	private final Logger log = LoggerFactory.getLogger(JsonPathHelperTests.class);

	private final JsonPathHelper helper = new JsonPathHelper();

	@Test
	void invalidJsonPath() {
		var error = helper.execute("$.", "{}");
		log.info(error);
		assertTrue(error.contains("JSONPath compilation"));
	}

	@Test
	void invalidJson() {
		var error = helper.execute("$", "}");
		log.info(error);
		assertTrue(error.contains("JSON parsing"));
	}

	@Test
	void general() throws Exception {
		var json = Files.readString(Path.of("src/test/resources/general.json"));
		assertEquals("string", helper.execute("$.string1", json));
		assertEquals("", helper.execute("$.string2", json));
		assertEquals("1", helper.execute("$.integer", json));
		assertEquals("1.2", helper.execute("$.double1", json));
		assertEquals(Double.parseDouble("1.2e+3"), Double.parseDouble(helper.execute("$.double2", json)));
		assertEquals("true", helper.execute("$.boolean1", json));
		assertEquals("false", helper.execute("$.boolean2", json));
		assertEquals("null", helper.execute("$.null", json));
		assertEquals("{\"name\":\"adam\"}", helper.execute("$.object", json));
		assertEquals("[\"value\"]", helper.execute("$.array", json));
	}

	@Test
	void successfulResponse() throws Exception {
		var jsonFile = Path.of("src/test/resources/successful_response.json");
		var jsonObject = (JSONObject) JSONValue.parse(Files.readString(jsonFile));
		assertEquals(jsonObject.get("access_token"), helper.execute("$.access_token", jsonFile));
		assertEquals("null", helper.execute("$.error", jsonFile));
	}

	@Test
	void errorResponse() throws Exception {
		var jsonFile = Path.of("src/test/resources/error_response.json");
		var jsonObject = (JSONObject) JSONValue.parse(Files.readString(jsonFile));
		assertEquals("null", helper.execute("$.access_token", jsonFile));
		assertEquals(jsonObject.get("error"), helper.execute("$.error", jsonFile));
	}

	@Test
	void inputStream() throws Exception {
		try (var json = Files.newInputStream(Path.of("src/test/resources/example.json"))) {
			var result = helper.execute("$.store.book[0].author", json);
			assertEquals("Nigel Rees", result);
		}
	}

	@Test
	void prettyPrint() {
		var input = "{\"name1\":\"value1\",\"name2\":\"value2\"}";
		var output = helper.setPrettyPrint().execute("$", "{\"name1\":\"value1\",\"name2\":\"value2\"}");
		assertNotEquals(input, output);
	}

	private void executeAndLog(String jsonPath, String json) {
		assertDoesNotThrow(() -> {
			var result = helper.execute(jsonPath, json);
			log.info("JSONPath: {}", jsonPath);
			log.info("Result: {}", result);
		});
	}

	/**
	 * Examples at https://github.com/json-path/JsonPath
	 */
	@Test
	void examples() throws Exception {
		var json = Files.readString(Path.of("src/test/resources/example.json"));
		executeAndLog("$.store.book[*].author", json);
		executeAndLog("$..author", json);
		executeAndLog("$.store.*", json);
		executeAndLog("$.store..price", json);
		executeAndLog("$..book[2]", json);
		executeAndLog("$..book[-2]", json);
		executeAndLog("$..book[0,1]", json);
		executeAndLog("$..book[:2]", json);
		executeAndLog("$..book[1:2]", json);
		executeAndLog("$..book[-2:]", json);
		executeAndLog("$..book[2:]", json);
		executeAndLog("$..book[?(@.isbn)]", json);
		executeAndLog("$.store.book[?(@.price < 10)]", json);
		executeAndLog("$..book[?(@.price <= $['expensive'])]", json);
		executeAndLog("$..book[?(@.author =~ /.*REES/i)]", json);
		executeAndLog("$..*", json);
		executeAndLog("$..book.length()", json);
	}

}
