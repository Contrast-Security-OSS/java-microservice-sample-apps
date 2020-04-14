FROM maven:3.5-jdk-8

WORKDIR /app
COPY pom.xml .
COPY src ./src

RUN mvn clean package 

EXPOSE 8000
ENTRYPOINT ["mvn","tomcat7:run-war"] 
