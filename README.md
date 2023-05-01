A simple command-line tool that evaluates JSONPath using `com.jayway.jsonpath:json-path`.

It can be used as a replacement of `jq`.

Usage:

```
java -jar jsonpath-cli.jar -p <JSONPath>

java -jar jsonpath-cli.jar -p <JSONPath> -s <JSON string>

java -jar jsonpath-cli.jar -p <JSONPath> -f <JSON file>

```

Arguments `-s` and `-f` are mutually exclusive. JSON is expected from standard input if neither is provided.

Other options:

```
--pretty  Pretty-print output
```

Examples:

```
echo '{"name":"value"}' | java -jar jsonpath-cli.jar -p '$.name'

java -jar jsonpath-cli.jar -p '$.name' -s '{"name":"value"}'

java -jar jsonpath-cli.jar -p '$.name' -f example.json

```
