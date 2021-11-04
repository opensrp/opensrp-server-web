# opensrp-server-web
[![Build Status](https://travis-ci.org/OpenSRP/opensrp-server-web.svg?branch=master)](https://travis-ci.org/OpenSRP/opensrp-server-web) [![Coverage Status](https://coveralls.io/repos/github/OpenSRP/opensrp-server-web/badge.svg?branch=master)](https://coveralls.io/github/OpenSRP/opensrp-server-web?branch=master) [![Codacy Badge](https://api.codacy.com/project/badge/Grade/5544ce1a89924b919197c902819c83eb)](https://www.codacy.com/app/OpenSRP/opensrp-server-web?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=OpenSRP/opensrp-server-web&amp;utm_campaign=Badge_Grade)

## Overview 
Generic web application

### Relevant Wiki Pages
* OpenSRP Server Refactor and Cleanup
  * [Refactor and Cleanup](https://smartregister.atlassian.net/wiki/spaces/Documentation/pages/562659330/OpenSRP+Server+Refactor+and+Clean+up)
  * [How to upload and use maven jar artifacts](https://smartregister.atlassian.net/wiki/spaces/Documentation/pages/564428801/How+to+upload+and+use+maven+jar+artifacts)
  * [Managing Server Wide Properties](https://smartregister.atlassian.net/wiki/spaces/Documentation/pages/602570753/Managing+Server+Wide+Properties)
  * [Server Web Build](https://smartregister.atlassian.net/wiki/spaces/Documentation/pages/616595457/Server+Web+Build)
* [OpenSRP Server Build](https://smartregister.atlassian.net/wiki/display/Documentation/OpenSRP+Server+Build)
* Deployment
  * [Docker Setup](https://smartregister.atlassian.net/wiki/display/Documentation/Docker+Setup)
  * [Docker Compose Setup](https://smartregister.atlassian.net/wiki/spaces/Documentation/pages/52690976/Docker+Compose+Setup)
  * [Ansible Playbooks](https://smartregister.atlassian.net/wiki/spaces/Documentation/pages/540901377/Ansible+Playbooks)
* [Postgres Database Support](https://smartregister.atlassian.net/wiki/spaces/Documentation/pages/251068417/Postgres+Database+Support+as+Main+Datastore)
* [OpenSRP Load Testing](https://smartregister.atlassian.net/wiki/spaces/Documentation/pages/268075009/OpenSRP+Load+Testing)

**Date/Time Filters** 
Endpoints supporting date/time filters have the following optional parameters  `fromDate` and `toDate` support [DateTimeFormat.ISO](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/format/annotation/DateTimeFormat.ISO.html "enum in org.springframework.format.annotation") i.e `yyyy-MM-dd'T'HH:mm:ss.SSSXXX` and unix timestamp with millisecond precision
 * yyyy - year
 * MM - month
 * dd - date
 * 'T' string literal
 * HH - hour
 * mm - minute
 * ss - seconds
 * SSS - milliseconds
 * XXX - ISO 8601 time zone

e.g`` 2020-10-10T19:32:13.856Z``, ``2020-10-10T19:32``, ``2020-10-10T19:32:56.235+07:00``
Sample Request

``/opensrp/rest/event/findIdsByEventType?fromDate=2000-10-31T01:30&serverVersion=0``

``/opensrp/rest/event/findIdsByEventType?fromDate=1602068945000&serverVersion=0``

``/opensrp/rest/event/findIdsByEventType?fromDate=2000-10-31T01:30&serverVersion=0``

``/opensrp/rest/event/findIdsByEventType?fromDate=2000-10-31T01:30:00.000%2B05:00&serverVersion=0``

**NOTE:** 
Remember to add your timezone to the DateTimeFormat.ISO  

### Health Endpoint
The health endpoint of the opensrp server is `/opensrp/health`. It always returns information in JSON. The status code of the response can either be `200` or `503` depending on status of the services. Response status code is `200` if all the services are running ok but `503` if any service is down/inaccessible.

Sample responses from the health endpoint are as follows:

Request Endpoint: `/opensrp/health`  
Request Method: GET  
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

---

Request Endpoint: `/opensrp/health`  
Request Method: GET  
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
    "openmrs": true,
    "rabbitmq": false
  },
  "serverTime": "2021-11-02T09:44:43.584+03:00",
  "buildVersion": "3.2"
}
```

**NOTE: Some services will only be checked if they are enabled by the spring maven profiles e.g rabbitmq**
