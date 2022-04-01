FROM #registry#/ug-registry/jdk8:1.80_231-openresty-1.0.5

COPY ./sources/ /javabin/

COPY ./sh/run.sh /javabin/run.sh
RUN chmod a+x /javabin/*

ENTRYPOINT ["/javabin/run.sh"]