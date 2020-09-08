FROM maven:3-openjdk-8

##ARG JAR_FILE=target/*.jar

##COPY ${JAR_FILE} app.jar

##ENTRYPOINT ["java","-jar","/app.jar"]

EXPOSE 8080 5005

ENTRYPOINT [ "mvn", "spring-boot:run" ]
