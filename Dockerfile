FROM maven:3-openjdk-11-slim AS build

# Copy OpenSRP Source
COPY . /tmp/opensrp-server-web

# Build WAR file
ARG opensrp_maven_package_profiles="postgres,jedis,oauth2"

RUN mvn clean package -Dmaven.test.skip=true -P $opensrp_maven_package_profiles -f /tmp/opensrp-server-web/pom.xml

# Explode WAR file
WORKDIR /tmp/opensrp-server-web-exploded
RUN jar -xvf /tmp/opensrp-server-web/target/opensrp.war

FROM tomcat:9-jdk11-corretto
# Copy the exploded directory
COPY --from=build /tmp/opensrp-server-web-exploded /usr/local/tomcat/webapps/opensrp

RUN yum update -y

EXPOSE 8080
# Start app
CMD /usr/local/tomcat/bin/catalina.sh run
