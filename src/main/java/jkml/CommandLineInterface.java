package jkml;

import java.nio.file.Path;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandLineInterface {

	private static final int SUCCESS = 0;

	private static final int FAILURE = 1;

	private final Logger log = LoggerFactory.getLogger(CommandLineInterface.class);

	public static void main(String[] args) {
		System.exit(new CommandLineInterface().run(args));
	}

	public int run(String[] args) {
		try {
			return runOrThrow(args);
		} catch (Exception e) {
			System.out.println("Error: " + JsonPathHelper.getRootCauseMessage(e));
			log.debug(e.getMessage(), e);
			return FAILURE;
		}
	}

	private int runOrThrow(String[] args) {
		var jsonPathOption = Option.builder("p").hasArg().desc("JSONPath query expression").required().build();
		var jsonFileOption = Option.builder("f").hasArg().desc("JSON file").build();
		var jsonStringOption = Option.builder("s").hasArg().desc("JSON string").build();
		var prettyPrintOption = Option.builder(null).longOpt("pretty").desc("Pretty printing").optionalArg(true).build();

		var options = new Options();
		options.addOption(jsonPathOption);
		options.addOptionGroup(new OptionGroup().addOption(jsonFileOption).addOption(jsonStringOption));
		options.addOption(prettyPrintOption);

		CommandLine cmd;
		try {
			cmd = new DefaultParser().parse(options, args);
		} catch (ParseException e) {
			printHelp();
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

	private static void printHelp() {
		var ls = System.lineSeparator();
		var sb = new StringBuilder();

		sb.append("Usage:").append(ls).append(ls);
		sb.append("  java -jar jsonpath-cli.jar -p <JSONPath>").append(ls).append(ls);
		sb.append("  java -jar jsonpath-cli.jar -p <JSONPath> -s <JSON string>").append(ls).append(ls);
		sb.append("  java -jar jsonpath-cli.jar -p <JSONPath> -f <JSON file>").append(ls).append(ls);

		sb.append("Arguments -s and -f are mutually exclusive. JSON is expected from standard").append(ls);
		sb.append("input if neither is provided.").append(ls).append(ls);

		sb.append("Other options:").append(ls).append(ls);
		sb.append("  --pretty  Pretty-print output").append(ls);

		System.out.print(sb.toString());
	}

}
