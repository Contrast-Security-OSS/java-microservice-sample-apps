FROM maven:3.5-jdk-8

WORKDIR /app
COPY pom.xml .
COPY src ./src

EXPOSE 8000
ENTRYPOINT ["mvn","spring-boot:run"] 
