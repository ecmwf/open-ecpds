ARG BUILDPLATFORM=linux/amd64
FROM --platform=${BUILDPLATFORM} docker.io/library/mariadb:10.11.4

LABEL maintainer="Laurent.Gougeon@ecmwf.int"

# Root password to connect to the database
ENV MYSQL_ROOT_PASSWORD=ecpds2020!

# Init script to create the OpenECPDS database
COPY ecpds.sql.gz /docker-entrypoint-initdb.d/

# Make it readable to mysql
RUN chown mysql:mysql /docker-entrypoint-initdb.d/ecpds.sql.gz

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
