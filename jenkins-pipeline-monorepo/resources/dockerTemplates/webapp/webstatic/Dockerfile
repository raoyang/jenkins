FROM #registry#/ug-registry/nginx:1.15.6

RUN mkdir -p /www
COPY ./sources /www

COPY ./sh/run.sh /webapp/run.sh
RUN chmod a+x /webapp/run.sh
COPY nginx.conf /webapp/nginx.conf

ENTRYPOINT ["/webapp/run.sh"]