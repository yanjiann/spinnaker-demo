FROM openjdk:8
WORKDIR /app/
COPY bin bin
COPY conf conf
COPY target/*adapter-mqtt.jar lib/
RUN ls lib/ > lib.txt
RUN mkdir logs
RUN ln -snf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime
