FROM docker.io/library/mariadb:11.4

LABEL maintainer="Laurent.Gougeon@ecmwf.int"
LABEL org.opencontainers.image.source="https://github.com/ecmwf/open-ecpds"
LABEL org.opencontainers.image.description="OpenECPDS Database"

# Supply the database root password at runtime, never in the image.

# Init script to create the OpenECPDS database
COPY ecpds.sql /docker-entrypoint-initdb.d/
COPY zz-development-password.sh /docker-entrypoint-initdb.d/
COPY development-entrypoint.sh /usr/local/bin/ecpds-development-entrypoint.sh

# Make it readable to mysql
RUN chown mysql:mysql /docker-entrypoint-initdb.d/ecpds.sql
RUN chmod 0644 /docker-entrypoint-initdb.d/zz-development-password.sh && \
    chmod 0755 /usr/local/bin/ecpds-development-entrypoint.sh

# Make directories readable to mysql
RUN chown mysql:mysql /etc/mysql/conf.d
RUN chown mysql:mysql /var/lib/mysql

# Redirect logs to data directory
RUN echo "log_error = /var/lib/mysql/mariadb_error.log" >> /etc/mysql/mariadb.conf.d/50-server.cnf

# Mountable volumes
VOLUME /etc/mysql/conf.d
VOLUME /var/lib/mysql

# Expose the port
EXPOSE 3306

ENTRYPOINT ["/usr/local/bin/ecpds-development-entrypoint.sh"]
CMD ["mariadbd"]
