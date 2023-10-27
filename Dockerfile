FROM navikt/java:18
LABEL maintainer="Team Bidrag" \
      email="bidrag@nav.no"

COPY ./target/bidrag-samhandler-*.jar app.jar
ENV SPRING_PROFILES_ACTIVE=nais
EXPOSE 8080
