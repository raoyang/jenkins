FROM #registry#/ug-registry/jdk8:1.8.0_231

COPY ./sources/ /javabin/

COPY ./sh/run.sh /javabin/run.sh
RUN chmod a+x /javabin/*

ENTRYPOINT ["/javabin/run.sh"]