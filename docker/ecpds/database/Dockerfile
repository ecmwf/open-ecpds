FROM docker.io/library/mariadb:11.4

LABEL maintainer="Laurent.Gougeon@ecmwf.int"
LABEL org.opencontainers.image.source="https://github.com/ecmwf/open-ecpds"
LABEL org.opencontainers.image.description="OpenECPDS Database"

# Root password to connect to the database
ENV MYSQL_ROOT_PASSWORD=ecpds2020!

# Init script to create the OpenECPDS database
COPY ecpds.sql /docker-entrypoint-initdb.d/

# Make it readable to mysql
RUN chown mysql:mysql /docker-entrypoint-initdb.d/ecpds.sql

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
