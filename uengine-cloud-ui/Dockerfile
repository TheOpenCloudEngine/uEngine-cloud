FROM webratio/nodejs-http-server
VOLUME /tmp
ADD dist /opt/www
EXPOSE 8080
ENTRYPOINT ["http-server","/opt/www","-p","8080"]
