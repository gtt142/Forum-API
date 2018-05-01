FROM ubuntu:16.04

MAINTAINER T. Garifullin

# Обвновление списка пакетов
RUN apt-get -y update

#
# Установка postgresql
#
ENV PGVER 9.5
RUN apt-get install -y postgresql-$PGVER

# Run the rest of the commands as the ``postgres`` user created by the ``postgres-$PGVER`` package when it was ``apt-get installed``
USER postgres

COPY scheme.sql scheme.sql

# Create a PostgreSQL role named ``tp_subd`` with ``subd`` as the password and
# then create a database `forum_api` owned by the ``tp_subd`` role.
RUN /etc/init.d/postgresql start &&\
    psql --command "CREATE USER tp_subd WITH PASSWORD 'subd';" &&\
    createdb --owner=tp_subd forum_api &&\
    psql -a -f scheme.sql &&\
    /etc/init.d/postgresql stop

# Adjust PostgreSQL configuration so that remote connections to the
# database are possible.
RUN echo "host all  all    0.0.0.0/0  md5" >> /etc/postgresql/$PGVER/main/pg_hba.conf

# And add ``listen_addresses`` to ``/etc/postgresql/$PGVER/main/postgresql.conf``
RUN echo "listen_addresses='*'" >> /etc/postgresql/$PGVER/main/postgresql.conf

# Expose the PostgreSQL port
EXPOSE 5432

# Add VOLUMEs to allow backup of config, logs and databases
VOLUME  ["/etc/postgresql", "/var/log/postgresql", "/var/lib/postgresql"]

# Back to the root user
USER root

#
# Сборка проекта
#

# Установка JDK
RUN apt-get install -y openjdk-9-jdk-headless
RUN apt-get install -y maven

# Копируем исходный код в Docker-контейнер

ENV WORK /opt/forum
ADD ./ $WORK/

# Собираем и устанавливаем пакет
WORKDIR $WORK
RUN mvn package

# Объявлем порт сервера
EXPOSE 5000

#
# Запускаем PostgreSQL и сервер
#
CMD service postgresql start && java -Xmx300M -Xmx300M -jar ./target/forum-1.0.0-SNAPSHOT.jar
