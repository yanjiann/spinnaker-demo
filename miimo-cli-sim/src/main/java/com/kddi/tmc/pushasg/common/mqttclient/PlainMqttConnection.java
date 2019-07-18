package com.kddi.tmc.pushasg.common.mqttclient;

import static com.kddi.tmc.pushasg.common.util.MessageUtil.MSGID_MQTT_CONN_CLOSED;
import static com.kddi.tmc.pushasg.common.util.MessageUtil.MSGID_MQTT_CONN_SUCCESS;
import static com.kddi.tmc.pushasg.common.util.MessageUtil.MSGID_MQTT_DISCONN;
import static com.kddi.tmc.pushasg.common.util.MessageUtil.MSGID_MQTT_RECONN;
import static com.kddi.tmc.pushasg.common.util.MessageUtil.MSGID_MQTT_RECONN_SUCCESS;
import static com.kddi.tmc.pushasg.common.util.MessageUtil.MSGID_MQTT_SUBSCRIBE_SUCCESS;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kddi.tmc.pushasg.common.util.StringUtil;

import lombok.Getter;

/**
 * MqttConnectionの実装クラス。 
 * パッケージ外から本クラスへの依存性を持たせないようにするため可視性はデフォルトとする。
 * 
 */
class PlainMqttConnection implements MqttConnection {

    protected static final Logger logger = LoggerFactory.getLogger(MqttConnection.class);
    protected static final com.kddi.tmc.pushasg.common.log.Logger log = new com.kddi.tmc.pushasg.common.log.Logger(PlainMqttConnection.class);
    private static final MqttConnectionProperties mqttConnProp = MqttConnectionProperties.getInstance();
    protected final MqttConnectOptions connOpt = new MqttConnectOptions();

//  private MqttMessageProcessable recevier = null;
    private Set<MqttMessageProcessable> receviers = new HashSet<MqttMessageProcessable>();
    private Set<String> topics = new HashSet<String>(); 
    private MqttClient mqttClient = null;

    @Getter
    private final String brokerUrl;
    private boolean isClosed = false;

    // 1ブローカーあたり1接続の制約を守るため、
    // パッケージ外からは、com.kddi.telematics.mqttclient.MqttConnectionsのスタティックファクトリメソッドからのインスタンス取得させるものとし、
    // 可視性はデフォルトとして、パッケージ外からの直接のインスタンス化を制限する。
    PlainMqttConnection(String brokerUrl) {
        this.brokerUrl = brokerUrl;

        // ・A client shall set the "Clean Session" flag in the CONNECT packet to
        // false.
        // ・A client shall not set the "Will Flag".
        // ・so Will Messages are not enabled A client may choose to provide a
        // non-zero MQTT KeepAlive value or to provide a KeepAlive of 0.
        // ・The MQTT server may require that a client provides a User Name and a
        // password (or other credential).
        // ※PolicyNetはuser/passでの認証は行わない
        connOpt.setCleanSession(false);
        connOpt.setKeepAliveInterval(mqttConnProp.getKeepAliveInterval());
    }
    
    private synchronized void addRecevier(MqttMessageProcessable recevier) {
        receviers.add(recevier);
    }
    
    private synchronized void removeRecevier(MqttMessageProcessable recevier) {
        receviers.remove(recevier);
    }
    
    private synchronized void addTopic(String... topic) {       
        topics.addAll(Arrays.asList(topic));
    }
    
    private synchronized void removeTopic(String... topic) {       
        topics.removeAll(Arrays.asList(topic));
    }
    


    @Override
    public void connect() throws MqttException, MqttSecurityException {
        if (mqttClient != null && mqttClient.isConnected()) {
            // 接続済の場合は何もしない
            return;
        }

        synchronized (this) {
            if (mqttClient != null && mqttClient.isConnected()) {
                // 接続済の場合は何もしない
                return;
            }
            // ClientIDのフォーマット仕様（A::<IN-AE-ID>で固定）が規定されており、同じIN-AEのClientIDは同じになる。
            // Brokerは同じClientIDからの複数の接続は許可しないため、、
            // oneM2MのClientID仕様に準拠すると、IN-AEを分散アプリケーションにするとBrokerとの接続に問題が生じるケースが出てきてしまう。
            // そのため、CSE側との接続に支障がないかぎりは、ClientIDは仕様に準拠しないものとする。
            mqttClient = new MqttClient(brokerUrl,
                    MqttClient.generateClientId() + "_" + hashCode(), new MemoryPersistence());
            mqttClient.setCallback(this);

            logger.debug("Connecting to {} ....", mqttClient.getServerURI());
            mqttClient.connect(connOpt);
            // logger.info("Connected to {}", mqttClient.getServerURI());
            log.writeAppLog(MSGID_MQTT_CONN_SUCCESS, mqttClient.getServerURI(),
                    mqttClient.getClientId());
            isClosed = false;
        }

    }

    @Override
    public void subscribe(String topic, MqttMessageProcessable recevier) throws MqttException {
        if (mqttClient == null || !mqttClient.isConnected()) {
            // 未接続の場合は何もしない
            return;
        }
        //this.recevier = recevier;
        addRecevier(recevier);
        addTopic(topic);
        mqttClient.subscribe(topic, mqttConnProp.getQos());
//        logger.info("Subscribing topic. " + topic);
        log.writeAppLog(MSGID_MQTT_SUBSCRIBE_SUCCESS, topic, mqttClient.getClientId());
    }
    
    @Override
    public void unsubscribe(String topic) throws MqttException {
        if (mqttClient == null || !mqttClient.isConnected()) {
            // 未接続の場合は何もしない
            return;
        }
        //this.recevier = recevier;
//        removeRecevier(recevier);
        removeTopic(topic);
        mqttClient.unsubscribe(topic);
//        mqttClient.subscribe(topic, mqttConnProp.getQos());
//        logger.info("Subscribing topic. " + topic);
        logger.debug("unsubscribe topic:{} client={}", topic, mqttClient.getClientId());
    }
    
    @Override
    public void unsubscribe(String[] topic) throws MqttException {
        if (mqttClient == null || !mqttClient.isConnected()) {
            // 未接続の場合は何もしない
            return;
        }
        //this.recevier = recevier;
//        removeRecevier(recevier);
        removeTopic(topic);
        mqttClient.unsubscribe(topic);
//        mqttClient.subscribe(topic, mqttConnProp.getQos());
//        logger.info("Subscribing topic. " + topic);
        
        Arrays.asList(topics).stream().forEach(s -> logger.debug("unsubscribe topic:{} client={}", s, mqttClient.getClientId()));
    }
    
    private void reSubscribe() throws MqttException {
        if (!this.topics.isEmpty()) {
            String[] cachedTopics = this.topics.toArray(new String[0]);

            int[] qos = new int[cachedTopics.length];
            Arrays.fill(qos, mqttConnProp.getQos());

            mqttClient.subscribe(cachedTopics, qos);
            Arrays.asList(topics).stream().forEach(s -> logger.debug("ReSubscribing topics. " + s));
            log.writeAppLog(MSGID_MQTT_SUBSCRIBE_SUCCESS, String.join(",", cachedTopics), mqttClient.getClientId());
        }
    }

    @Override
    public void subscribe(String[] topics, MqttMessageProcessable recevier) throws MqttException {
        if (mqttClient == null || !mqttClient.isConnected()) {
            // 未接続の場合は何もしない
            return;
        }
        addRecevier(recevier);
        addTopic(topics);
        int[] qos = new int[topics.length];
        Arrays.fill(qos, mqttConnProp.getQos());
        mqttClient.subscribe(topics, qos);
        Arrays.asList(topics).stream().forEach(s -> logger.debug("Subscribing topics. " + s));
        log.writeAppLog(MSGID_MQTT_SUBSCRIBE_SUCCESS, String.join(",", topics), mqttClient.getClientId());
    }

    @Override
    public void publish(String topic, byte[] payload) throws MqttException {
        // 未接続の場合は何もしない
        if (mqttClient == null || !mqttClient.isConnected()) {
            return;
        }
        MqttTopic mqttTopic = mqttClient.getTopic(topic);
        MqttMessage message = new MqttMessage(payload);

        // ・MQTT's "QoS 1" message reliability level.
        // ・It does not use the following features: Retained Messages.<br>
        message.setQos(mqttConnProp.getQos());
        message.setRetained(false);

        // Publishは、失敗しても指定回数リトライする。
        int maxRetryCount = mqttConnProp.getMaxRetryCount();
        int retryInterval = mqttConnProp.getRetryInterval();
        MqttDeliveryToken token = null;
        for (int i = 0; i <= maxRetryCount; i++) {
            // closeが呼ばれた場合は処理を中断する
            if (isClosed) {
                return;
            }

            try {
                token = mqttTopic.publish(message);
                token.waitForCompletion();
                logger.debug("The message was published successfully.[{}]", StringUtil.toString(payload));
                return;
            } catch (MqttException e) {
                logger.debug("MQTT PUBLISH failed. " + e.getMessage());
                try {
                    Thread.sleep(retryInterval * 1000);
                } catch (InterruptedException e1) {
                    return;
                }
            }
        }
        logger.debug("Failed to retry PUBLISH.");
        throw new MqttException(new Throwable());
    }

    @Override
    public void close() throws MqttException {
        if (mqttClient != null && mqttClient.isConnected()) {
            synchronized (this) {
                isClosed = true;
                if (mqttClient != null && mqttClient.isConnected()) {
                    mqttClient.disconnect();
                    mqttClient.close();
                    // logger.info("MQTT connection close.");
                    log.writeAppLog(MSGID_MQTT_CONN_CLOSED,
                            mqttClient.getServerURI(),
                            mqttClient.getClientId());
                }
            }
        }
    }
    
    private MqttMessageProcessable getRecevier(String topic) {
        for (MqttMessageProcessable recevier : receviers) {
            if (recevier.supports(topic)) {
                return recevier;
            }
        }

        return null;
    }

    @Override
    public void messageArrived(String topic, MqttMessage msg) {
        MqttMessageProcessable recevier = getRecevier(topic);
        if (recevier != null) {
            recevier.process(topic, msg.getId(), msg.getQos(), msg.getPayload());
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
//        logger.error("Connection to {} was lost. Reconnecting..........", mqttClient.getServerURI());
        log.writeAppLog(MSGID_MQTT_DISCONN, mqttClient.getServerURI(), mqttClient.getClientId());

        // 接続がロストした場合は、接続できるまで再接続を試みる
        int reconnectInterval = mqttConnProp.getReconnectInterval();
        while (true) {
            // closeが呼ばれた場合はループを抜ける
            if (isClosed) {
                return;
            }

            try {
                if (!mqttClient.isConnected()) {
                    synchronized (this) {
                        if (!mqttClient.isConnected()) {
                            log.writeAppLog(MSGID_MQTT_RECONN,
                                    mqttClient.getServerURI(),
                                    mqttClient.getClientId());
                            mqttClient.connect(connOpt);
                            // reSubscribe();
                            // logger.info("Reconnected to " +
                            // mqttClient.getServerURI());
                            log.writeAppLog(MSGID_MQTT_RECONN_SUCCESS,
                                    mqttClient.getServerURI(),
                                    mqttClient.getClientId());
                        }
                    }
                }
                reSubscribe();
                return;
            } catch (MqttException e) {
                logger.debug("Failed to reconnect to the broker. " + e.getMessage());
                try {
                    Thread.sleep(reconnectInterval * 1000);
                } catch (InterruptedException e1) {
                    return;
                }
            }
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken arg0) {
        logger.debug("Client delivery completed.");
    }

    @Override
    public boolean isConnected() {
        if (mqttClient != null) {
            return mqttClient.isConnected();
        } else {
            return false;
        }
    }

}
