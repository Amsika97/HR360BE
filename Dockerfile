FROM openjdk:17
ADD hr360-0.0.1-snapshot.jar hr360-0.0.1-snapshot.jar
ENTRYPOINT ["java","-Dspring.profiles.active=test","-jar","hr360-0.0.1-snapshot.jar"]
EXPOSE 4040
