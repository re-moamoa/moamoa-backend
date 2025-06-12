# 1단계: 커스텀 JRE 생성
FROM eclipse-temurin:17-jdk AS jlinker
WORKDIR /opt
RUN selected_modules="java.base,java.logging,java.management,java.naming,java.net.http,java.rmi,java.scripting,java.security.jgss,java.security.sasl,java.sql,java.xml,java.instrument,jdk.crypto.ec,jdk.unsupported,java.desktop" && \
    jlink --add-modules "${selected_modules}" --strip-debug --no-header-files --no-man-pages --compress=2 --output /opt/custom-java-runtime

# 2단계: Distroless 이미지에 커스텀 JRE 탑재
FROM gcr.io/distroless/base-debian11 AS base-jre
COPY --from=jlinker /opt/custom-java-runtime /opt/java-runtime
ENV JAVA_HOME=/opt/java-runtime
ENV PATH="${JAVA_HOME}/bin:${PATH}"

# 3단계: 애플리케이션 실행용 이미지 구성
FROM base-jre AS runtime
WORKDIR /app
COPY build/libs/*.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
