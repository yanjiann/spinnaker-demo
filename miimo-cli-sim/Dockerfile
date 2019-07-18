FROM openkbs/jdk-mvn-py3
ENV src_dir=$HOME/mqtt-src
ENV app_dir=$HOME/app
ENV lib_dir=$HOME/app/lib

RUN mkdir ${src_dir}
RUN mkdir ${app_dir}
RUN mkdir -p ${lib_dir}
RUN mkdir ${app_dir}/logs
COPY --chown=developer:developer src ${src_dir}/src
COPY --chown=developer:developer pom.xml ${src_dir}
RUN cd ${src_dir};mvn install
#RUN cd ${src_dir};mvn -Dhttp.proxyHost=web-proxy.sgp.hpecorp.net -Dhttp.proxyPort=8080 -Dhttps.proxyHost=web-proxy.sgp.hpecorp.net -Dhttps.proxyPort=8080 install

COPY bin ${app_dir}/bin
COPY conf ${app_dir}/conf
RUN cp ${src_dir}/target/miimo-cli-sim.jar ${lib_dir}/.

RUN rm -R ${source_project}
