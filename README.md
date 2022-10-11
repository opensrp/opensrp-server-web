# opensrp-server-web

[![Build Status](https://travis-ci.org/OpenSRP/opensrp-server-web.svg?branch=master)](https://travis-ci.org/OpenSRP/opensrp-server-web)
[![Coverage Status](https://coveralls.io/repos/github/opensrp/opensrp-server-web/badge.svg)](https://coveralls.io/github/opensrp/opensrp-server-web)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/5544ce1a89924b919197c902819c83eb)](https://www.codacy.com/app/OpenSRP/opensrp-server-web?utm\_source=github.com\&utm\_medium=referral\&utm\_content=OpenSRP/opensrp-server-web\&utm\_campaign=Badge\_Grade)

## Overview

Generic web application

### Relevant Wiki Pages

*   OpenSRP Server Refactor and Cleanup
    *   [Refactor and Cleanup](https://smartregister.atlassian.net/wiki/spaces/Documentation/pages/562659330/OpenSRP+Server+Refactor+and+Clean+up)
    *   [How to upload and use maven jar artifacts](https://smartregister.atlassian.net/wiki/spaces/Documentation/pages/564428801/How+to+upload+and+use+maven+jar+artifacts)
    *   [Managing Server Wide Properties](https://smartregister.atlassian.net/wiki/spaces/Documentation/pages/602570753/Managing+Server+Wide+Properties)
    *   [Server Web Build](https://smartregister.atlassian.net/wiki/spaces/Documentation/pages/616595457/Server+Web+Build)
*   [OpenSRP Server Build](https://smartregister.atlassian.net/wiki/display/Documentation/OpenSRP+Server+Build)
*   Deployment
    *   [Docker Setup](https://smartregister.atlassian.net/wiki/display/Documentation/Docker+Setup)
    *   [Docker Compose Setup](https://smartregister.atlassian.net/wiki/spaces/Documentation/pages/52690976/Docker+Compose+Setup)
    *   [Ansible Playbooks](https://smartregister.atlassian.net/wiki/spaces/Documentation/pages/540901377/Ansible+Playbooks)
*   [Postgres Database Support](https://smartregister.atlassian.net/wiki/spaces/Documentation/pages/251068417/Postgres+Database+Support+as+Main+Datastore)
*   [OpenSRP Load Testing](https://smartregister.atlassian.net/wiki/spaces/Documentation/pages/268075009/OpenSRP+Load+Testing)

**Date/Time Filters**
Endpoints supporting date/time filters have the following optional parameters  `fromDate` and `toDate` support [DateTimeFormat.ISO](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/format/annotation/DateTimeFormat.ISO.html "enum in org.springframework.format.annotation") i.e `yyyy-MM-dd'T'HH:mm:ss.SSSXXX` and unix timestamp with millisecond precision

*   yyyy - year
*   MM - month
*   dd - date
*   'T' string literal
*   HH - hour
*   mm - minute
*   ss - seconds
*   SSS - milliseconds
*   XXX - ISO 8601 time zone

e.g` 2020-10-10T19:32:13.856Z`, `2020-10-10T19:32`, `2020-10-10T19:32:56.235+07:00`
Sample Request

`/opensrp/rest/event/findIdsByEventType?fromDate=2000-10-31T01:30&serverVersion=0`

`/opensrp/rest/event/findIdsByEventType?fromDate=1602068945000&serverVersion=0`

`/opensrp/rest/event/findIdsByEventType?fromDate=2000-10-31T01:30&serverVersion=0`

`/opensrp/rest/event/findIdsByEventType?fromDate=2000-10-31T01:30:00.000%2B05:00&serverVersion=0`

**NOTE:**
Remember to add your timezone to the DateTimeFormat.ISO

### Health Endpoint

The health endpoint of the opensrp server is `/opensrp/health`. It always returns information in JSON format. The status code of the response can either be `200` or `503` depending on status of the services. Response status code is `200` if all the services are running ok but `503` if any service is down/inaccessible.

Sample responses from the health endpoint are as follows:

Request Endpoint: `/opensrp/health`\
Request Method: GET\
Status Code: 200

```json
{
  "problems": {},
  "services": {
    "postgres": true,
    "redis": true,
    "keycloak": true,
    "rabbitmq": true
  },
  "serverTime": "2021-11-01T09:44:43.584+03:00",
  "buildVersion": "3.2"
}
```

***

Request Endpoint: `/opensrp/health`\
Request Method: GET\
Status Code: 503

```json
{
  "problems": {
    "redis": "Cannot get Jedis connection; nested exception is redis.clients.jedis.exceptions.JedisConnectionException: Could not get a resource from the pool",
    "rabbitmq": "java.io.IOException"
  },
  "services": {
    "postgres": true,
    "redis": false,
    "keycloak": true,
    "rabbitmq": false
  },
  "serverTime": "2021-11-02T09:44:43.584+03:00",
  "buildVersion": "3.2"
}
```

#### Configurations for the health endpoint

| Configuration                               | Description                                    | Type    | Default |
|---------------------------------------------|------------------------------------------------|---------|---------|
| health.endpoint.keycloak.connectionTimeout  | http client connection timeout for the request | Integer | 5000ms  |
| health.endpoint.keycloak.readTimeout        | http client read timeout for the request       | Integer | 5000ms  |
| health.endpoint.postgres.queryTimeout       | postgres query timeout for indicator DB query  | Integer | 2000ms  |

The above configs can be updated on `opensrp.properties` file.

**NOTE: Some services will only be checked if they are enabled by the spring maven profiles e.g rabbitmq**

### Metrics Endpoint

The metrics endpoint of the opensrp server is `/opensrp/metrics`. It returns information in `text/plain; version=0.0.4; charset=utf-8` format. The status code of the response should always be `200`.

The endpoint is only accessible through the following ips when unauthenticated but requires authentication for the any other ips:

*   `127.0.0.1`,
*   One additional configurable ip, kindly check below `metrics.additional_ip_allowed`

Sample responses from the metrics endpoint are as follows:

Request Endpoint: `/opensrp/health`\
Request Method: GET\
Status Code: 200

```text
# HELP health_check_rabbitmq  
# TYPE health_check_rabbitmq gauge
health_check_rabbitmq 0.0
# HELP postgres_size The database size
# TYPE postgres_size gauge
postgres_size{database="opensrp",} 1.2512801439E10
# HELP health_check_postgres  
# TYPE health_check_postgres gauge
health_check_postgres 1.0
jvm_threads_states_threads{state="blocked",} 0.0
```

#### Configurations for the metrics endpoint

| Configuration                               | Description                                    | Type    | Default |
|---------------------------------------------|------------------------------------------------|---------|---------|
| metrics.tags  | Refers to the common tags to be added on all metrics | Map | {}  |
| metrics.health\_check\_updater.cron        | Cron schedule for updating health indicator (custom metrics)      | Integer | 1minute  |
| metrics.additional\_ip\_allowed       | ip or pattern for access metrics endpoint without authentication e.g  192.168.100.0/8 or 192.168.100.3 (only one ip) | String | ""  |
| metrics.include       | metrics to be include  | Set (Comma separated string) | "all"  |
| metrics.exclude       | metrics to be excluded  | Set (Comma separated string) | ""  |
| metrics.permitAll       | permits access to metrics endpoint without authentication (Set true if restriction on reverse proxy configs, otherwise whitelist ip if needed) | Boolean | false  |

> Available metrics:
> `all,log4j2,jvm_thread,jvm_thread,jvm_gc,jvm_mem,cpu,uptime,db,disk_space`

> Health indicators [above](#health-endpoint) are added as gauge meters with name in the following pattern; health\_check\_%s, `%s` is a placeholder for service name e.g health\_check\_postgres

The above configs can be updated on `opensrp.properties` file.

### Redis for High Availability

Redis Sentinel support is added on openSRP server web. To enable redis sentinel the following configurations were added:

#### Configurations for the redis sentinel

| Configuration        | Description                                                                                                    | Type                         | Default      |
|----------------------|----------------------------------------------------------------------------------------------------------------|------------------------------|--------------|
| redis.sentinels      | Comma separated string of redis sentinel e.g. "localhost:26379,localhost:26380".                               | String                       | ""           |
| redis.master         | Name of the set of redis instances to monitor. Its a name used to identify a redis master and its replicas.    | String                       | "mymaster"   |
| redis.architecture   | Refers to the deployment topology used i.e standalone or sentinel.                                             | String                       | "standalone" |

#### Supported Redis Architecture

*   Standalone - Deploy single redis instance (master).
*   Sentinel - Deploy more than one redis instance made up of sentinel and a master, which would handle automatic fail-over in case a master is not available.
