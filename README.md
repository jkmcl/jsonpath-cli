A simple command-line tool that evaluates JSONPath using `com.jayway.jsonpath:json-path`.

It can be used as a replacement of `jq`.

Usage examples:

```
# String input
java -jar jsonpath-cli.jar -s '{"name":"value"}' -p '$.name'

# File input
java -jar jsonpath-cli.jar -f example.json -p '$.store.book[0].author'

# Standard input
cat example.json | java -jar jsonpath-cli.jar -p '$.store.book[0].author'

```
