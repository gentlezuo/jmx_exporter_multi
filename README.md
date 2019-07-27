JMX Exporter
==

jmx_exporter文档[https://github.com/prometheus/jmx_exporter/blob/master/README.md](/https://github.com/prometheus/jmx_exporter/blob/master/README.md)

原生的jmx_exporter需要为每一个java进程启动一个exporter，并且需要部署到该进程所属的机器上。

这里提供了一种解决方案：将一个jmx_exporter部署在一台机器上，同时抓取多个应用的jmx。现对于原生的jmx_exporter,只需要修改很少的配置文件即可。

比如抓取两个进程的jmx，只需要将原生配置改为列表即可，增加了几个变量`jobs`,`name`,`socket`

## 启动

确保被抓取目标开启了remote jmx，并且配置文件无误

```
java -jar jmx_prometheus_httpserver_multi-0.13.0-SNAPSHOT-jar-with-dependencies.jar 8 example.yaml

```


## Configuration
The configuration is in YAML. An example with all possible options:
```yaml
---

jobs:
  - name: a
    startDelaySeconds: 0
    #hostPort: 127.0.0.1:1234
    username: 
    password: 
    socket: 127.0.0.1:9999
    jmxUrl: service:jmx:rmi:///jndi/rmi://127.0.0.1:1234/jmxrmi
    ssl: false
    lowercaseOutputName: false
    lowercaseOutputLabelNames: false
    whitelistObjectNames: ["org.apache.cassandra.metrics:*"]
    blacklistObjectNames: ["org.apache.cassandra.metrics:type=ColumnFamily,*"]
    rules:
    - pattern: 'org.apache.cassandra.metrics<type=(\w+), name=(\w+)><>Value: (\d+)'
    name: cassandra_$1_$2
    value: $3
    valueFactor: 0.001
    labels: {}
    help: "Cassandra metric $1 $2"
    type: GAUGE
    attrNameSnakeCase: false
	
 - name: a
   startDelaySeconds: 0
   hostPort: 127.0.0.1:1235
   username: 
   password: 
   socket: 127.0.0.1:9999
   #jmxUrl: service:jmx:rmi:///jndi/rmi://127.0.0.1:1235/jmxrmi
   ssl: false
   lowercaseOutputName: false
   lowercaseOutputLabelNames: false
   whitelistObjectNames: ["org.apache.cassandra.metrics:*"]
   blacklistObjectNames: ["org.apache.cassandra.metrics:type=ColumnFamily,*"]
   rules:
   - pattern: 'org.apache.cassandra.metrics<type=(\w+), name=(\w+)><>Value: (\d+)'
   name: cassandra_$4_$5
   value: $5
   valueFactor: 0.001
   labels: {}
   help: "Cassandra metric $4 $5"
   type: GAUGE
   attrNameSnakeCase: false
```
Name     | Description
---------|------------
jobname  | 任务的名字
socket   | 需要暴露给prometheus的地址
startDelaySeconds | start delay before serving requests. Any requests within the delay period will result in an empty metrics set.
hostPort | The host and port to connect to via remote JMX. If neither this nor jmxUrl is specified, will talk to the local JVM.
username | The username to be used in remote JMX password authentication.
password | The password to be used in remote JMX password authentication.
jmxUrl   | A full JMX URL to connect to. Should not be specified if hostPort is.
ssl      | Whether JMX connection should be done over SSL. To configure certificates you have to set following system properties:<br/>`-Djavax.net.ssl.keyStore=/home/user/.keystore`<br/>`-Djavax.net.ssl.keyStorePassword=changeit`<br/>`-Djavax.net.ssl.trustStore=/home/user/.truststore`<br/>`-Djavax.net.ssl.trustStorePassword=changeit`
lowercaseOutputName | Lowercase the output metric name. Applies to default format and `name`. Defaults to false.
lowercaseOutputLabelNames | Lowercase the output metric label names. Applies to default format and `labels`. Defaults to false.
whitelistObjectNames | A list of [ObjectNames](http://docs.oracle.com/javase/6/docs/api/javax/management/ObjectName.html) to query. Defaults to all mBeans.
blacklistObjectNames | A list of [ObjectNames](http://docs.oracle.com/javase/6/docs/api/javax/management/ObjectName.html) to not query. Takes precedence over `whitelistObjectNames`. Defaults to none.
rules    | A list of rules to apply in order, processing stops at the first matching rule. Attributes that aren't matched aren't collected. If not specified, defaults to collecting everything in the default format.
pattern  | Regex pattern to match against each bean attribute. The pattern is not anchored. Capture groups can be used in other options. Defaults to matching everything.
attrNameSnakeCase | Converts the attribute name to snake case. This is seen in the names matched by the pattern and the default format. For example, anAttrName to an\_attr\_name. Defaults to false.
name     | The metric name to set. Capture groups from the `pattern` can be used. If not specified, the default format will be used. If it evaluates to empty, processing of this attribute stops with no output.
value    | Value for the metric. Static values and capture groups from the `pattern` can be used. If not specified the scraped mBean value will be used.
valueFactor | Optional number that `value` (or the scraped mBean value if `value` is not specified) is multiplied by, mainly used to convert mBean values from milliseconds to seconds.
labels   | A map of label name to label value pairs. Capture groups from `pattern` can be used in each. `name` must be set to use this. Empty names and values are ignored. If not specified and the default format is not being used, no labels are set.
help     | Help text for the metric. Capture groups from `pattern` can be used. `name` must be set to use this. Defaults to the mBean attribute description and the full name of the attribute.
type     | The type of the metric, can be `GAUGE`, `COUNTER` or `UNTYPED`. `name` must be set to use this. Defaults to `UNTYPED`.

Metric names and label names are sanitized. All characters other than `[a-zA-Z0-9:_]` are replaced with underscores,
and adjacent underscores are collapsed. There's no limitations on label values or the help text.

A minimal config is `{}`, which will connect to the local JVM and collect everything in the default format.
Note that the scraper always processes all mBeans, even if they're not exported.

Example configurations for javaagents can be found at  https://github.com/prometheus/jmx_exporter/tree/master/example_configs

### Pattern input
The format of the input matches against the pattern is
```
domain<beanpropertyName1=beanPropertyValue1, beanpropertyName2=beanPropertyValue2, ...><key1, key2, ...>attrName: value
```

Part     | Description
---------|------------
domain   | Bean name. This is the part before the colon in the JMX object name.
beanProperyName/Value | Bean properties. These are the key/values after the colon in the JMX object name.
keyN     | If composite or tabular data is encountered, the name of the attribute is added to this list.
attrName | The name of the attribute. For tabular data, this will be the name of the column. If `attrNameSnakeCase` is set, this will be converted to snake case.
value    | The value of the attribute.

No escaping or other changes are made to these values, with the exception of if `attrNameSnakeCase` is set.
The default help includes this string, except for the value.

### Default format
The default format will transform beans in a way that should produce sane metrics in most cases. It is
```
domain_beanPropertyValue1_key1_key2_...keyN_attrName{beanpropertyName2="beanPropertyValue2", ...}: value
```
If a given part isn't set, it'll be excluded.

## Testing

`mvn test` to test.

## Debugging

You can start the jmx's scraper in standalone mode in order to debug what is called 

`java -cp jmx_exporter.jar io.prometheus.jmx.JmxScraper  service:jmx:rmi:your_url`

To get finer logs (including the duration of each jmx call),
create a file called logging.properties with this content:

```
handlers=java.util.logging.ConsoleHandler
java.util.logging.ConsoleHandler.level=ALL
io.prometheus.jmx.level=ALL
io.prometheus.jmx.shaded.io.prometheus.jmx.level=ALL
```

Add the following flag to your Java invocation:

`-Djava.util.logging.config.file=/path/to/logging.properties`


## Installing

A Debian binary package is created as part of the build process and it can
be used to install an executable into `/usr/bin/jmx_exporter` with configuration
in `/etc/jmx_exporter/jmx_exporter.yaml`.
