FROM maven:3.5-jdk-8

WORKDIR /app
COPY pom.xml .
COPY devservice.yml .
COPY src ./src

RUN mvn clean package 
RUN mv target/bookstore-devservice*-SNAPSHOT.jar target/bookstore-devservice.jar
RUN ls -al
RUN ls -al src
EXPOSE 8002

ENTRYPOINT ["java", "-jar", "target/bookstore-devservice.jar", "server", "devservice.yml"]
