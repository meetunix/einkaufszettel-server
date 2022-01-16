FROM openjdk:17-alpine
LABEL maintainer "martin@nachtsieb.de"
WORKDIR /
ARG JAR_FILE
ARG CONF_FILE
RUN mkdir /log /ezdb
COPY /target/${JAR_FILE} ezserver.jar
COPY example_configs/${CONF_FILE} config.properties
VOLUME ["/ezdb", "/logs"]
EXPOSE 8080
CMD ["java", "-jar", "/ezserver.jar", "-c", "/config.properties"]
