FROM centos:centos8
RUN dnf install -y curl git java-1.8.0-openjdk-devel.x86_64

ENV JAVA_HOME /etc/alternatives/java_sdk_1.8.0_openjdk
ENV PATH $PATH:$JAVA_HOME/bin

WORKDIR /root

# Maven
ARG MAVEN_ARCHIVE=https://mirrors.bfsu.edu.cn/apache/maven/maven-3/3.8.1/binaries/apache-maven-3.8.1-bin.tar.gz
RUN curl -sL $MAVEN_ARCHIVE | tar -xz -C /opt && ln -s /opt/apache-maven-3.8.1 /opt/maven
ENV M2_HOME /opt/maven
ENV PATH $PATH:$M2_HOME/bin

# sbt
ARG SBT_ARCHIVE=https://github.com/sbt/sbt/releases/download/v1.4.8/sbt-1.4.8.tgz
RUN curl -sL $SBT_ARCHIVE | tar -xz -C /opt && ln -s /opt/sbt-1.4.8 /opt/sbt
ENV SBT_HOME /opt/sbt
ENV PATH $PATH:$SBT_HOME/bin

# build nebula-java client
ARG NEBULA_JAVA_REPO=https://github.com/vesoft-inc/nebula-java.git
RUN git clone $NEBULA_JAVA_REPO && cd nebula-java && mvn clean install -Dcheckstyle.skip=true -DskipTests -Dgpg.skip -X

# build binlog-replayer spark job
COPY . /root/epik-gateway-binlog-replayer
RUN cd /root/epik-gateway-binlog-replayer && sbt assembly
