FROM #registry#/ug-registry/dotnet-core:lateinos-3.1.3

COPY ./sources/ /tmp/src/

COPY ./sh/run.sh /dotnetbin/run.sh
RUN chmod a+x /dotnetbin/*

ENTRYPOINT ["/dotnetbin/run.sh"]