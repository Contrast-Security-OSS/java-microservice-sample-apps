FROM maven:3.5-jdk-8

WORKDIR /app
COPY pom.xml .
COPY src ./src

EXPOSE 8001
ENTRYPOINT ["mvn","spring-boot:run"] 
