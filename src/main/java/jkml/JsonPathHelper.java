package jkml;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.function.Function;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.JsonPathException;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.ParseContext;

public class JsonPathHelper {

	private static final Configuration CONFIGURATION = Configuration.builder().options(Option.SUPPRESS_EXCEPTIONS).build();

	private final Logger log = LoggerFactory.getLogger(JsonPathHelper.class);

	private boolean prettyPrint = false;

	public JsonPathHelper setPrettyPrint() {
		prettyPrint = true;
		return this;
	}

	public String execute(String jsonPath, String json) {
		return execute(jsonPath, parseContext -> parseContext.parse(json));
	}

	public String execute(String jsonPath, Path json) {
		return execute(jsonPath, parseContext -> {
			try {
				return parseContext.parse(json.toFile());
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});
	}

	public String execute(String jsonPath, InputStream json) {
		return execute(jsonPath, parseContext -> parseContext.parse(json));
	}

	private String execute(String jsonPath, Function<ParseContext, DocumentContext> parser) {
		JsonPath path;
		try {
			path = JsonPath.compile(jsonPath);
		} catch (JsonPathException e) {
			return "Error: JSONPath compilation: " + getRootCauseMessage(e);
		}

		DocumentContext context;
		try {
			context = parser.apply(JsonPath.using(CONFIGURATION));
		} catch (JsonPathException e) {
			return "Error: JSON parsing: " + getRootCauseMessage(e);
		}

		Object object;
		try {
			object = context.read(path);
		} catch (JsonPathException e) {
			log.debug("JSONPath evaluation error", e);
			object = null;
		}
		return resultToString(object);
	}

	public static String getRootCauseMessage(Throwable throwable) {
		var rootCause = getRootCause(throwable);
		return (rootCause == null ? throwable : rootCause).getMessage();
	}

	private static Throwable getRootCause(Throwable throwable) {
		var causes = new ArrayList<Throwable>();
		while (throwable != null && !causes.contains(throwable)) {
			causes.add(throwable);
			throwable = throwable.getCause();
		}
		return causes.isEmpty() ? null : causes.get(causes.size() - 1);
	}

	private String resultToString(Object object) {
		if (object == null) {
			return "null";
		}
		var wrappedObject = JSONObject.wrap(object);
		var indentFactor = prettyPrint ? 2 : 0;
		if (wrappedObject instanceof JSONObject jsonObject) {
			return jsonObject.toString(indentFactor);
		}
		if (wrappedObject instanceof JSONArray jsonArray) {
			return jsonArray.toString(indentFactor);
		}
		return String.valueOf(wrappedObject);
	}

}
