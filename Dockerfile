FROM eclipse-temurin:17-alpine AS build
RUN adduser -D -s /bin/sh appuser
WORKDIR /app
RUN chown appuser:appuser /app
USER appuser
COPY --chown=appuser:appuser .mvn .mvn
COPY --chown=appuser:appuser mvnw pom.xml ./
RUN ./mvnw dependency:go-offline
COPY --chown=appuser:appuser src src
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:17-alpine AS extract
WORKDIR /app
COPY --from=build /app/target/cloud-storage-0.0.1-SNAPSHOT.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

FROM eclipse-temurin:17-alpine
WORKDIR /app
COPY --from=extract app/dependencies ./
COPY --from=extract app/spring-boot-loader/ ./
COPY --from=extract app/snapshot-dependencies/ ./
COPY --from=extract app/application/ ./
RUN adduser -D -s /bin/sh appuser
USER appuser
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]


