package jkml;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.help.HelpFormatter;
import org.apache.commons.cli.help.TextHelpAppendable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandProcessor {

	private static final int SUCCESS = 0;

	private static final int FAILURE = 1;

	private final Logger log = LoggerFactory.getLogger(CommandProcessor.class);

	public static void main(String[] args) {
		System.exit(new CommandProcessor().run(args));
	}

	public int run(String[] args) {
		try {
			return parseAndProcess(args);
		} catch (Exception e) {
			System.out.println("Error: " + JsonPathHelper.getRootCauseMessage(e));
			log.debug(e.getMessage(), e);
			return FAILURE;
		}
	}

	private int parseAndProcess(String[] args) {
		var jsonPathOption = Option.builder("p").hasArg().argName("query").desc("JSONPath query expression").required().get();
		var jsonFileOption = Option.builder("f").hasArg().argName("file").desc("JSON file").get();
		var jsonStringOption = Option.builder("s").hasArg().argName("string").desc("JSON string").get();
		var prettyPrintOption = Option.builder(null).longOpt("pretty").desc("Pretty-print output").get();

		var options = new Options();
		options.addOption(jsonPathOption);
		options.addOptionGroup(new OptionGroup().addOption(jsonFileOption).addOption(jsonStringOption));
		options.addOption(prettyPrintOption);

		CommandLine cmd;
		try {
			cmd = new DefaultParser().parse(options, args);
		} catch (ParseException e) {
			printHelp(options);
			return FAILURE;
		}

		var jsonPath = cmd.getOptionValue(jsonPathOption);
		var jsonFile = cmd.getOptionValue(jsonFileOption);
		var json = cmd.getOptionValue(jsonStringOption);

		log.debug("JSONPath: {}", jsonPath);
		log.debug("JSON file: {}", jsonFile);
		log.debug("JSON string: {}", json);

		var jsonPathHelper = new JsonPathHelper();
		if (cmd.hasOption(prettyPrintOption)) {
			log.debug("Pretty printing enabled");
			jsonPathHelper.setPrettyPrint();
		}

		if (jsonFile == null && json == null) {
			System.out.print(jsonPathHelper.execute(jsonPath, System.in));
		} else if (jsonFile == null) {
			System.out.print(jsonPathHelper.execute(jsonPath, json));
		} else {
			System.out.print(jsonPathHelper.execute(jsonPath, Path.of(jsonFile)));
		}

		return SUCCESS;
	}

	private static void printHelp(Options options) {
		var appendable = new TextHelpAppendable(System.out);
		appendable.setLeftPad(0);
		var formatter = HelpFormatter.builder()
				.setComparator((o1, o2) -> 0)
				.setHelpAppendable(appendable)
				.setShowSince(false)
				.get();
		formatter.setSyntaxPrefix("Usage:");
		var syntax = "java -jar jsonpath-cli.jar";
		var header = "Options -s and -f are mutually exclusive. JSON is expected from standard input if neither is provided.";
		try {
			formatter.printHelp(syntax, header, options, "", true);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

}
