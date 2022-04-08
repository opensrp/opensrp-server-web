FROM maven:3-openjdk-11-slim AS build

# Copy OpenSRP Source
COPY . /tmp/opensrp-server-web

# Build WAR file
ARG opensrp_maven_package_profiles="postgres,jedis,oauth2"

RUN mvn clean package -Dmaven.test.skip=true -P $opensrp_maven_package_profiles -f /tmp/opensrp-server-web/pom.xml

# Explode WAR file
WORKDIR /tmp/opensrp-server-web-exploded
RUN jar -xvf /tmp/opensrp-server-web/target/opensrp.war

FROM tomcat:9-jre11-openjdk-slim
# Copy the exploded directory
COPY --from=build /tmp/opensrp-server-web-exploded /usr/local/tomcat/webapps/opensrp

# copy the migration files
COPY --from=build /tmp/opensrp-server-web/configs/assets/migrations /migrations

# Download mybatis
RUN apt update && apt install -y unzip wget

# setup mybatis
RUN mkdir -p /opt/mybatis \
    && wget --quiet --no-cookies https://github.com/mybatis/migrations/releases/download/mybatis-migrations-3.3.4/mybatis-migrations-3.3.4-bundle.zip -O /opt/mybatis/mybatis-migrations-3.3.4.zip \
    && unzip /opt/mybatis/mybatis-migrations-3.3.4.zip -d /opt/mybatis/ \
    && rm -f /opt/mybatis/mybatis-migrations-3.3.4.zip \
    && chmod +x /opt/mybatis/mybatis-migrations-3.3.4/bin/migrate

# Run migrations (mybatis)
# RUN /opt/mybatis/mybatis-migrations-3.3.4/bin/migrate up --path=/migrations --env=deployment

EXPOSE 8080 8081
# Start app
CMD /usr/local/tomcat/bin/catalina.sh run
