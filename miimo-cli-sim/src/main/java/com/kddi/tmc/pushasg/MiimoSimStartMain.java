package com.kddi.tmc.pushasg;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.kddi.tmc.pushasg.common.mqttclient.MqttConnection;
import com.kddi.tmc.pushasg.common.mqttclient.MqttConnections;
import com.kddi.tmc.pushasg.common.util.ApplicationProperties;
import com.kddi.tmc.pushasg.common.util.MiiContextSupport;
import com.kddi.tmc.pushasg.common.vo.MiiContext;
import com.kddi.tmc.pushasg.service.CmdSubscriberProcessable;

@SpringBootApplication
public class MiimoSimStartMain implements CommandLineRunner {
    private static final Logger logger = LoggerFactory
            .getLogger(MiimoSimStartMain.class);

    @Autowired
    private ApplicationContext applicationContext;

    private static final ExecutorService threadPool = Executors.newCachedThreadPool();

    private Map<String, String> configs = new HashMap<>();
    
    private int startNum = 0;
    private int currentNum = 0;
    private String devPrefix = null;

    public void startSimTask(String devUniqId) {
    	MiiContext miiContext = new MiiContext();

    	miiContext.setDevUniqId(devUniqId);
    	MiiContext.setDevContext(devUniqId, miiContext);

        DevClientSim sim = applicationContext.getBean(DevClientSim.class);
        sim.setMiiContext(miiContext);
        threadPool.execute(sim);
        logger.debug("start sim task for dev_uniq_id:{} successfully",
                devUniqId);
    }

    private String[] getConfigs(String... args) {
        ArrayList<String> argList = new ArrayList<>();

        for (String arg : args) {
            if (arg.startsWith("--")) {
                String[] configVals = arg.substring(2).split("=");
                configs.put(configVals[0], configVals[1]);
            } else {
                argList.add(arg);
            }
        }

        return argList.toArray(new String[0]);
    }

    @Override
    public void run(String... args) throws Exception {
        args = getConfigs(args);

        if (args.length < 1) {
            logger.info("parameter is invalid.");
            System.exit(1);
        }
        String multiThreadNumStart = configs.get("multi.thread.num.start");
        String tps = configs.get("multi.thread.tps");
        if (multiThreadNumStart == null || tps == null) {
            logger.info("parameter is invalid.");
            System.exit(1);
        }

        try {
            startNum = Integer.parseInt(multiThreadNumStart);
            currentNum = startNum;
        } catch (Exception e) {
            logger.info("multi.thread.num.start:{} is invalid.",
                    multiThreadNumStart);
            System.exit(1);
        }

        int iTps = -1;
        if (tps != null) {
            try {
                iTps = Integer.parseInt(tps);
            } catch (Exception e) {
                logger.info("multi.thread.tps:{} is invalid.", tps);
                System.exit(1);
            }
        }
        
        devPrefix = args[0];
        
        for(int idx = 0 ; idx < iTps ; idx++) {
            startSimTask(devPrefix + (startNum + idx));
        }
    }
    
    @PreDestroy
    public void exit() {
        try {
            threadPool.shutdownNow();
            for (;;) {
                if (threadPool.awaitTermination(1, TimeUnit.MINUTES)) {
                    break;
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        try {
            MqttConnections.releaseAll();
        } catch (MqttException e) {
            logger.error(e.getMessage(), e);
        } finally {
            logger.info("PushMessageStartMain is shutdown.");
        }
    }

    public static void main(String[] args) {
//        SpringApplication.run(new Object[] { MiimoSimStartMain.class },
//                args);
//
		  logger.debug("Program Started!");
		    // サーバーソケットを生成＆待機
		    try (ServerSocket server = new ServerSocket()) {
		      server.bind(new InetSocketAddress("localhost", 8080));
		      try (Socket socket = server.accept();
		          BufferedReader reader = new BufferedReader(
		            new InputStreamReader(socket.getInputStream()));
		          PrintWriter writer = new PrintWriter(
		            socket.getOutputStream(), true)) {
		        // 入力を受け取ったら、大文字に変換の上で応答
		        while (true) {
		          String line = reader.readLine();
		          if (line.equals("\\q")) {
		            break;
		          }
		          writer.println(line.toUpperCase());
		          System.out.println(line);
		        }
		      }
		    } catch (Exception e) {
		      e.printStackTrace();
			  logger.error(e.getMessage());

		    }
		    
		    logger.debug("End!");    
    
    }

    @Service
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class DevClientSim implements Runnable, MiiContextSupport {
        private MiiContext miiContext = null;

        @Autowired
        private ApplicationProperties applicationProperties;

        @Autowired
        private CmdSubscriberProcessable cmdSubscriberProcessable;
        
        public void run() {
            String devUniqId = miiContext.getDevUniqId();
            miiContext.setSslParams(
                    applicationProperties.getSslParamsFromDevUniqId(devUniqId));

            String brokerUrl = applicationProperties
                    .getBrokerUrlFromDevUniqId(devUniqId);
            miiContext.setBrokerUrl(brokerUrl);

            String commandTopic = applicationProperties.getCommandTopicFromDevUniqId(devUniqId);
            miiContext.setRespTopic(applicationProperties.getRespTopicFromDevUniqId(devUniqId));

            if (brokerUrl == null) {
                logger.warn("dev_uniq_id is invalid.");
                return;
            }
            final MqttConnection connection = MqttConnections.getConnectionTo(
            		miiContext.getDevUniqId(), brokerUrl,
                    applicationProperties.getSslParams());

            cmdSubscriberProcessable.setMiiContext(miiContext);
            cmdSubscriberProcessable.addSupportTopic(commandTopic);
            try {
                connection.connect();
                connection.subscribe(commandTopic, cmdSubscriberProcessable);
            } catch (MqttSecurityException e) {
                e.printStackTrace();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void setMiiContext(MiiContext context) {
            this.miiContext = context;
        }

        @Override
        public MiiContext getMiiContext() {
            return miiContext;
        }
    }

}
