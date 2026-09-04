FROM debian:12-slim

ARG ASTERISK_VERSION=22.11.0

RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        ca-certificates \
        wget \
        build-essential \
        pkg-config \
        libedit-dev \
        libjansson-dev \
        libxml2-dev \
        libsqlite3-dev \
        uuid-dev \
        libssl-dev \
        libcurl4-openssl-dev \
        libnewt-dev \
        libncurses5-dev \
        unixodbc \
        unixodbc-dev \
        odbc-postgresql \
        libpq-dev \
        python3 \
        python3-pip \
        python3-venv \
        nano && \
    rm -rf /var/lib/apt/lists/*

RUN python3 -m venv /opt/asterisk-venv && \
    /opt/asterisk-venv/bin/pip install --no-cache-dir \
        alembic \
        sqlalchemy \
        psycopg2-binary

ENV PATH="/opt/asterisk-venv/bin:$PATH"

WORKDIR /usr/src

RUN wget https://downloads.asterisk.org/pub/telephony/asterisk/asterisk-${ASTERISK_VERSION}.tar.gz && \
    tar -xzf asterisk-${ASTERISK_VERSION}.tar.gz && \
    rm asterisk-${ASTERISK_VERSION}.tar.gz

WORKDIR /usr/src/asterisk-${ASTERISK_VERSION}

RUN ./configure --with-jansson-bundled && \
    make -j2 && \
    make install && \
    make samples

COPY odbc.ini /etc/odbc.ini
COPY odbcinst.ini /etc/odbcinst.ini
COPY config/res_odbc.conf.template /etc/asterisk/res_odbc.conf.template
COPY entrypoint.sh /usr/local/bin/asterisk-entrypoint.sh
ENV ASTERISK_VERSION=${ASTERISK_VERSION}
RUN chmod +x /usr/local/bin/asterisk-entrypoint.sh

ENTRYPOINT ["/usr/local/bin/asterisk-entrypoint.sh"]
CMD ["asterisk", "-f", "-vvv"]
