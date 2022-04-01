FROM #registry#/ug-registry/openjdk:openjdk-11-jdk

COPY ./sources/ /javabin/

COPY ./sh/run.sh /javabin/run.sh
RUN chmod a+x /javabin/*

ENTRYPOINT ["/javabin/run.sh"]