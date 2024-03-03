package jkml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class CommandProcessorTests {

	@Test
	void testNoArg() {
		var cp = new CommandProcessor();
		assertNotEquals(0, cp.run(new String[0]));
	}

	@Test
	void testString() {
		var cp = new CommandProcessor();
		assertEquals(0, cp.run(new String[] { "-p", "$", "-s", "{}", "--pretty" }));
	}

	@Test
	void testFile() {
		var cp = new CommandProcessor();
		assertEquals(0, cp.run(new String[] { "-p", "$", "-f", "src/test/resources/example.json" }));
	}

	@Test
	void testException() {
		var cp = new CommandProcessor();
		assertNotEquals(0, cp.run(new String[] { "-p", "$", "-f", "src/test/resources/noSuchFile.json" }));
	}

}
