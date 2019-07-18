package com.kddi.tmc.pushasg.service;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.kddi.tmc.pushasg.common.mqttclient.MqttMessageProcessable;
import com.kddi.tmc.pushasg.common.subscriber.SubscriberAction;
import com.kddi.tmc.pushasg.common.subscriber.SubscriberCommand;
import com.kddi.tmc.pushasg.common.vo.MiiContext;

public abstract class BaseSubscriberProcessable implements MqttMessageProcessable {
    private static final Logger logger = LoggerFactory
            .getLogger(BaseSubscriberProcessable.class);
    
	private ExecutorService threadPool = Executors.newCachedThreadPool();
	
	private Set<String> topics = new HashSet<String>();
	
	@Autowired
	private ApplicationContext context;
	
	@Override
	public boolean process(String topic, int id, int qos, byte[] payload) {
		SubscriberCommand command = context.getBean(SubscriberCommand.class);
		command.setParams(topic, payload);
		command.appendAction(getAction());
		threadPool.execute(command);
		return true;
	}
	
	protected abstract SubscriberAction getAction();
	
	@PreDestroy
    private void shutdown() {
        threadPool.shutdown();
    }
	
	/**
     * byte配列をStringに変換し、空白や改行を除去したものを返す
     * 
     * @param byte_array
     *            バイト配列
     * @return 変換後の文字列
     */
    protected String encodeToString(byte[] byte_array) {
        try {
            String rawString = new String(byte_array, "UTF-8");
            String compactString = rawString.replaceAll("\r\n", "").replaceAll("\n", "");
            return compactString;
        } catch (UnsupportedEncodingException e) {
            logger.error("Failed to encode byte array into character string.");
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    public void addSupportTopic(String... topic) {
        topics.addAll(Arrays.asList(topic));
    }
    
    public void removeSupportTopic(String... topic) {
        topics.removeAll(Arrays.asList(topic));
    }
    
    @Override
    public boolean supports(String topic) {
        return topics.contains(topic);
    }

}
