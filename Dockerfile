FROM ubuntu
MAINTAINER Justin Walrath <walrathjaw@gmail.com>
RUN apt-get -y update
RUN apt-get install -y openjdk-7-jdk git
RUN git clone http://github.com/maximx1/lecarton /tmp/lecarton
WORKDIR /tmp/lecarton
RUN ./activator stage
EXPOSE 9000
CMD ["cd", "/tmp/lecarton/", "&&", "target/universal/stage/bin/lecarton", -Ddb.default.url="jdbc:h2:/tmp/.data", "-DapplyEvolutions.default=true", "-Dhttp.port=9000", "-J-Xms32M", "-J-Xmx64M" ]