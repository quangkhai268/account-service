# Build jar trước:  mvn -DskipTests package
# Image chạy được với UID ngẫu nhiên của OpenShift (không ghi file vào thư mục cố định).
FROM registry.access.redhat.com/ubi9/openjdk-17-runtime:latest

COPY target/account-server-1.0.0.jar /deployments/app.jar

EXPOSE 8081
ENV JAVA_APP_JAR=/deployments/app.jar
