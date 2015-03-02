FROM ubuntu
MAINTAINER Justin Walrath <walrathjaw@gmail.com>
RUN apt-get -y install software-properties-common
RUN echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections
RUN add-apt-repository -y ppa:webupd8team/java
RUN apt-get -y update
RUN apt-get install -y git oracle-java8-installer
RUN git clone http://github.com/maximx1/lecarton /tmp/lecarton
WORKDIR /tmp/lecarton
RUN ./activator stage
EXPOSE 9000
CMD ["/tmp/lecarton/target/universal/stage/bin/lecarton", "-Ddb.default.url=jdbc:h2:/tmp/.data", "-DapplyEvolutions.default=true", "-Dhttp.port=9000", "-J-Xms32M", "-J-Xmx64M"]