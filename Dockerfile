FROM eclipse-temurin:21-jre
WORKDIR /auth
COPY target/auth-service.jar .
EXPOSE 8080
ENTRYPOINT ["java","-jar","auth-service.jar"]