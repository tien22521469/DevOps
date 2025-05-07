FROM openjdk:8 AS BUILD_IMAGE
WORKDIR /usr/src/app/
RUN apt update && apt install maven -y
COPY ./ /usr/src/app/
RUN mvn install -DskipTests

FROM openjdk:8

WORKDIR /usr/src/app/
COPY --from=BUILD_IMAGE /usr/src/app/target/emartapp-1.0.0.jar app.jar

EXPOSE 9000
ENTRYPOINT ["java","-jar","app.jar"]
# Test
