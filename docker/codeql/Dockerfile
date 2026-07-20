FROM mcr.microsoft.com/cstsectools/codeql-container:latest

USER root
RUN apt-get update && \
    apt-get install -y --no-install-recommends openjdk-21-jdk && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

ENV JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
ENV PATH=$JAVA_HOME/bin:$PATH

ENTRYPOINT []
