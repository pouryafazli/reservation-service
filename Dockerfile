FROM  openjdk:8
EXPOSE 8080
ADD target/reservation-service.jar reservation-service.jar
ENTRYPOINT ["java","-jar", "reservation-service.jar"]