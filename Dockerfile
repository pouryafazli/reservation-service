FROM  openjdk:8
EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=prod                                                                                                       
ADD target/reservation-service.jar reservation-service.jar
ENTRYPOINT ["java","-jar", "reservation-service.jar"]