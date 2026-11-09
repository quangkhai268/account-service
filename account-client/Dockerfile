# Build jar trước:  mvn -DskipTests package
FROM registry.access.redhat.com/ubi9/openjdk-17-runtime:latest

COPY target/account-client-1.0.0.jar /deployments/app.jar

EXPOSE 8080
ENV JAVA_APP_JAR=/deployments/app.jar
