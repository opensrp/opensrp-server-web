# Opensrp Server Web

OpenSRP Server Generic Web Application. Github
Repository: [Opensrp Server Web](https://github.com/OpenSRP/opensrp-server-web).

## What is Opensrp?

OpenSRP is an open-source, mobile-first platform, built to enable data-driven decision making at all levels of the
health system.

## How to use the image?

### Prerequisites

For opensrp to work it needs the following applications to support it in brackets are their respective spring profiles
that activate them (Default spring profiles are `jedis,postgres and oauth2`):

	1. Redis (`jedis` profile)
	2. Postgres (`postgres` profile)
	3. Keycloak or Openmrs for (v2.1.* tags for openmrs sync, `openmrs-sync` profile) authentication is by default oauth2 but basic auth is still available for openmrs authentication with (`basic_auth` profile)

To update the active profiles make changes on
this [line](https://github.com/opensrp/opensrp-server-web/blob/f16ba2704fcb74ca9e46da77ddfae6ac21d5fd9c/src/main/webapp/WEB-INF/web.xml#L126) (
comma separated) and include the file on the volumes section of docker compose.

Opensrp server is deployed as a .war file. Its main configurations are stored in the following locations:

1. [context.xml](https://github.com/opensrp/opensrp-server-web/blob/master/src/main/webapp/META-INF/context.xml)
    - Holds the postgres DB configurations e.g credentials, database name and other additional configs.
    - Copy this file and update the postgres credentials.
2. [web.xml](https://github.com/opensrp/opensrp-server-web/blob/master/src/main/webapp/WEB-INF/web.xml)
    - Holds mappings between URL paths and the servlets that handle requests with those paths, also the active spring
      profiles.
    - Unless one need additional spring maven profiles other than defaults there is no need to copy and update
      configuration.
        - Additional profiles can be added on
          this [line](https://github.com/opensrp/opensrp-server-web/blob/460ac16ac2315693e7f335f45a5555a4930dbc5d/src/main/webapp/WEB-INF/web.xml#L126)
          .
3. [log4j2.xml](https://github.com/opensrp/opensrp-server-configs/blob/master/assets/config/log4j2.xml)
    - Holds the logging configurations.
    - Unless one needs to customize log configurations leave as is.
4. [opensrp.properties](https://github.com/opensrp/opensrp-server-configs/blob/master/assets/config/opensrp.properties)
    - Holds all the application properties config.
    - Copy the file and update applications configurations for redis, rabbitmq, openmrs, dhis2, threading, metrics,
      sentry just to name a few.
    - Change only what one needs and leave the rest as defaults.
    - Additionally update
      cors [here](https://github.com/opensrp/opensrp-server-configs/blob/master/assets/config/opensrp.properties#L46) to
        * for developments purposes or comma separated links of trusted origins.
5. keycloak.json
    - Holds all the keycloak configs used on spring.
   ```json
     {
       "auth-server-url": "https://<keycloak-url>/auth/",
       "confidential-port": 443,
       "credentials": {
         "secret": "<sample-secret>"
         },
         "realm": "<realm name>",
         "resource": "<resource name>",
         "ssl-required": "external"
     }
   ```

### Mybatis In-App Migration

Mybatis runtime configuration has been added from v2.10.x, v3.2.x, v2.1.7x meaning migrations will run when during
application start up.

Now using the image.

```yaml
version: "3.9"
services:
  redis:
    restart: unless-stopped
    image: redis:6.0
    ports:
      - "6379:6379"
    command: redis-server --requirepass redisPassword
    volumes:
      - redisdata:/data
  postgres:
    restart: unless-stopped
    image: postgres:14
    ports:
      - "5457:5432"
    environment:
      - "POSTGRES_PASSWORD=mysecretpassword"
      - "POSTGRES_USER=postgres"
      - "POSTGRES_DB=postgres"
    volumes:
      - pgdata:/var/lib/postgresql/data
# Remove keycloak service if openmrs is used for authentication
  keycloak:
    restart: unless-stopped
    image: jboss/keycloak:16.1.1
    environment:
      - "KEYCLOAK_USER=admin"
      - "KEYCLOAK_PASSWORD=admin"
      - "DB_VENDOR=postgres"
      - "DB_PASSWORD=secretpassword"
      - "DB_USER=keycloak"
      - "DB_ADDR=postgres:5432"
      - "PROXY_ADDRESS_FORWARDING=true"
    ports:
      - "8081:8080"
      - "8443:8443"
    depends_on:
      - postgres
  opensrp-server-web:
    restart: unless-stopped
    image: opensrp/opensrp-server-web:v2.10.1-SNAPSHOT # pick the latest tag
    ports:
      - "8080:8080"
    volumes:
      - ./context.xml:/usr/local/tomcat/webapps/opensrp/META-INF/context.xml
      - ./opensrp.properties:/usr/local/tomcat/webapps/opensrp/WEB-INF/classes/opensrp.properties
      - ./keycloak.json:/usr/local/tomcat/webapps/opensrp/WEB-INF/keycloak.json
      - multimediaData:/opt/multimedia
    depends_on:
      - mybatis
      - keycloak
      - redis
# refer to https://hub.docker.com/r/opensrp/web 
# opensrp-web: 
#    depends_on:
#      - opensrp-server-web

volumes:
  redisdata:
    external: true
  pgdata:
    external: true
  multimediaData:
    external: true

```

And to run execute:

 ```bash
 docker-compose up
 ```

For production setups consider backing up the postgresql databases.

## Licence Information

Open Smart Register Platform (OpenSRP), formerly Dristhi software

Copyright 2012-2021

Foundation for Research in Health Systems; Sustainable Engineering Lab; Columbia University; and The Special Programme
of Research,  
Development and Research Training in Human Reproduction (HRP) of the World Health Organization; Ona; mPower Social
Enterprise Bangladesh;  
Interactive Health Solutions; Summit Institute of Development; Interactive Research and Development; Johns Hopkins
University Global  
mHealth Institute; Harvard University School of Public Health

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
License. You may obtain a copy of the License at:

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "
AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific
language governing permissions and limitations under the License.
