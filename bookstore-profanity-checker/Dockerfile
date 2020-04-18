FROM maven:3.5-jdk-8

COPY pom.xml .
COPY src ./src

RUN mvn clean package

EXPOSE 8003
ENTRYPOINT ["mvn","jetty:run"] 
