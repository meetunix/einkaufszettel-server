FROM openjdk:17-alpine
LABEL maintainer "martin@nachtsieb.de"
WORKDIR /
ARG JAR_FILE
ARG CONF_FILE
RUN mkdir /log /ezdb
COPY ${JAR_FILE} ezserver.jar
ADD example_configs /configs
VOLUME ["/ezdb", "/log"]
EXPOSE 8080
CMD  java -jar ezserver.jar -c configs/$CONF_FILE
