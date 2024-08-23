FROM amazoncorretto:21.0.4
EXPOSE 8080:8080
RUN mkdir /app
COPY ./build/libs/*-all.jar /app/human-resource-information-service.jar
ENTRYPOINT ["java","-jar","/app/human-resource-information-service.jar"]
