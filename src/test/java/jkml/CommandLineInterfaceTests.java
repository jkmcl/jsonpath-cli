package jkml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class CommandLineInterfaceTests {

	@Test
	void testNoArg() {
		var intf = new CommandLineInterface();
		assertNotEquals(0, intf.run(new String[0]));
	}

	@Test
	void testString() {
		var intf = new CommandLineInterface();
		assertEquals(0, intf.run(new String[] { "-p", "$", "-s", "{}", "--pretty" }));
	}

	@Test
	void testFile() {
		var intf = new CommandLineInterface();
		assertEquals(0, intf.run(new String[] { "-p", "$", "-f", "src/test/resources/example.json" }));
	}

	@Test
	void testException() {
		var intf = new CommandLineInterface();
		assertNotEquals(0, intf.run(new String[] { "-p", "$", "-f", "src/test/resources/noSuchFile.json" }));
	}

}
