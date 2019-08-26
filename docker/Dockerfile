FROM tomcat:9-jre8-alpine

# Tomcat Server
EXPOSE 8080

ENV TZ="America/New_York"
ENV JAVA_OPTS -Xmx1024m -Dcds.endpoint.test=http://localhost:8080/opencds-decision-support-service-test/evaluate -Dcds.endpoint.production=http://localhost:8080/opencds-decision-support-service/evaluate

RUN adduser -u 1000 -D appuser && \
    apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/$TZ /etc/timezone && \
    rm -rf webapps/docs && \
    rm -rf webapps/examples && \
    rm -rf webapps/host-manager && \
    rm -rf webapps/manager && \
    rm -rf webapps/ROOT && \
    chown -R appuser: ./

USER appuser

# Copy OpenCDS supporting config files
COPY docker/opencds-rckms-supporting-configs.tar /usr/local/tomcat/
COPY docker/opencds-rckms-supporting-configs-test.tar /usr/local/tomcat/

RUN tar -xf ./opencds-rckms-supporting-configs.tar -C /home/appuser
RUN tar -xf ./opencds-rckms-supporting-configs-test.tar -C /home/appuser

# Copy OpenCDS Decision Support Service to Tomcat Webapps directory
COPY docker/opencds-decision-support-service.war webapps/
COPY docker/opencds-decision-support-service-test.war webapps/

# Copy OpenCDS REST warfile
COPY target/opencds-rest-service.war webapps/

# Run Tomcat server
CMD ["catalina.sh", "run"]
