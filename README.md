
## About The Project

# Reservation System

An underwater volcano formed a new small island in the Pacific Ocean last month. All the conditions on the island seems perfect and it was
decided to open it up for the general public to experience the pristine uncharted territory.
The island is big enough to host a single campsite so everybody is very excited to visit. In order to regulate the number of people on the island, it
was decided to come up with an online web application to manage the reservations. You are responsible for design and development of a REST
API service that will manage the campsite reservations.

**constraints**
- The campsite will be free for all.
- The campsite can be reserved for max 3 days.
- The campsite can be reserved minimum 1 day(s) ahead of arrival and up to 1 month in advance.
- Reservations can be cancelled anytime.
- For sake of simplicity assume the check-in & check-out time is 12:00 AM

**System Requirements**
- The users will need to find out when the campsite is available. So the system should expose an API to provide information of the availability of the campsite for a given date range with the default being 1 month.
- Provide an end point for reserving the campsite. The user will provide his/her email & full name at the time of reserving the campsite along with intended arrival date and departure date. Return a unique booking identifier back to the caller if the reservation is successful.
- The unique booking identifier can be used to modify or cancel the reservation later on. Provide appropriate end point(s) to allow modification/cancellation of an existing reservation
- Due to the popularity of the island, there is a high likelihood of multiple users attempting to reserve the campsite for the same/overlapping date(s). Demonstrate with appropriate test cases that the system can gracefully handle concurrent requests to reserve the campsite.
- Provide appropriate error messages to the caller to indicate the error cases.
- In general, the system should be able to handle large volume of requests for getting the campsite availability.
- There are no restrictions on how reservations are stored as as long as system constraints are not violated.

# Building and Running

For building and running the application you need:

- [JDK 1.8](https://www.oracle.com/java/technologies/downloads/#java8)
- [Maven 3](https://maven.apache.org)

## Running the application locally

- There are several ways to run the application on your local machine. One way is to execute the `main` method in the `com.upgrade.reservation.ReservationApplication` class from your IDE.
- Alternatively you can use the [Spring Boot Maven plugin](https://docs.spring.io/spring-boot/docs/current/reference/html/build-tool-plugins.html#build-tool-plugins.maven) like so:

```shell
mvn spring-boot:run
```
# API Documentation
Service used OpenAPI to generate API Documentation which is accessible at:

```
http://localhost:8080/swagger-ui.html
```

# How to Use

**List campsite available date**

```
curl http://localhost:8080/reservations/available
```

```
'curl http://localhost:8080/reservations/available?startDate=2021-10-01&endDate=2021-10-20'
```


**Get Reservation By Id**

```
curl http://localhost:8080/reservations/{id}
```

**Create Reservation**

```
curl --location --request POST 'http://localhost:8080/reservations' \
--header 'Content-Type: application/json' \
--data-raw '{
    "firstName":"firstName",
    "lastName":"lastName",
    "email":"test@email.com",
    "startDate":"2021-10-01",
    "endDate":"2021-10-02"
}'
```

**Update the existing reservation**

```
curl --location -g --request PUT 'http://localhost:8080/reservations/{id}' \
--header 'Content-Type: application/json' \
--data-raw '{
    "firstName":"firstName",
    "lastName":"lastName",
    "email":"test@email.com",
    "startDate":"2021-10-01",
    "endDate":"2021-10-02"
}'
```

**Delete the existing Reservation**

```
curl --request DELETE 'http://localhost:8080/reservations/{id}'
```

#Testing

**Smoke Testing**

To verifies the entire system from end to end is working you can run the SmokeTests. It will execute:
- All the APIs. It will get the available dates, reserve the campsite, get the reservation by its id, update the reservation and at the end delete the same reservation 
- Running 200 concurrent API calls to reserve the campsite for the same period. To make sure all calls issue at the same time system used CountDownLatch and ExecutorService.

```shell
mvn test -Dtest=SmokeTests
```

#Correlation ID

A unique identifier value will attached to any requests which doesn't have x-correlation-id header that allow reference to a particular request or all request logs.

```
INFO 51734 --- [io-8080-exec-22] c.u.r.filter.CorrelationHeaderFilter     : No correlationId found in Header. Generated : a601cb1c-32a1-41e3-adaf-a7e7560dae8b
```

# Technology Stack

## Overview

|Technology                |Description         |
|--------------------------|--------------------|
|Core Framework            |Spring Boot2        |
|Persistent Layer Framework|Spring Data JPA     |
|Database                  |H2                  |

## Libraries and Plugins

|Technology                |Description         |
|---------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------|
|<a href="https://swagger.io/">Swagger</a>			|Open-Source software framework backed by a large ecosystem of tools that helps developers design, build, document, and consume RESTful Web services. |
|<a href="https://projectlombok.org/">Lombok</a>		|Project Lombok is a java library that automatically plugs into your editor and build tools, spicing up your java.
Never write another getter or equals method again, with one annotation your class has a fully featured builder, Automate your logging variables, and much more. |
|<a href="https://site.mockito.org/">Mockito</a>		|Mockito is a mocking framework for Java. Mockito allows convenient creation of substitutes of real objects for testing purposes.|
