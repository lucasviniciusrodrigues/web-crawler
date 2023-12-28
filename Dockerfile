FROM maven:3.6.3-jdk-14

ADD . /usr/src/axr
WORKDIR /usr/src/axr
EXPOSE 4567
ENTRYPOINT ["mvn", "clean", "verify", "exec:java"]