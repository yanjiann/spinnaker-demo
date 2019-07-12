package com.hpe.iot.core.nip.adapter.mqtt.connections;

import java.util.ArrayList;
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

import com.hpe.iot.core.nip.adapter.mqtt.connections.MqttConnections.POOL_TYPE;
import com.hpe.iot.core.nip.adapter.mqtt.service.MqttClientService;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * MqttConnectionの実装クラス。 
 * パッケージ外から本クラスへの依存性を持たせないようにするため可視性はデフォルトとする。
 * 
 */
@Slf4j
public class PlainMqttConnection implements MqttConnection {

    private static final MqttConnectionProperties mqttConnProp = MqttConnectionProperties.getInstance();
    protected final MqttConnectOptions connOpt = new MqttConnectOptions();

//  private MqttMessageProcessable recevier = null;
    private ArrayList<MqttMessageProcessable> receviers = new ArrayList<MqttMessageProcessable>();
    private Set<String> topics = new HashSet<String>(); 
    private MqttClient mqttClient = null;

    @Getter
    private final String brokerUrl;
    private boolean isClosed = false;

    // 1ブローカーあたり1接続の制約を守るため、
    // パッケージ外からは、com.kddi.telematics.mqttclient.MqttConnectionsのスタティックファクトリメソッドからのインスタンス取得させるものとし、
    // 可視性はデフォルトとして、パッケージ外からの直接のインスタンス化を制限する。
    public PlainMqttConnection(String brokerUrl) {
        this.brokerUrl = brokerUrl;

        
        connOpt.setMaxInflight(mqttConnProp.getMaxInflight());

        // ・A client shall set the "Clean Session" flag in the CONNECT packet to
        // false.
        // ・A client shall not set the "Will Flag".
        // ・so Will Messages are not enabled A client may choose to provide a
        // non-zero MQTT KeepAlive value or to provide a KeepAlive of 0.
        // ・The MQTT server may require that a client provides a User Name and a
        // password (or other credential).
        // ※PolicyNetはuser/passでの認証は行わない
        connOpt.setCleanSession(true);
        connOpt.setKeepAliveInterval(mqttConnProp.getKeepAliveInterval());
    }
    
    private synchronized void addRecevier(MqttMessageProcessable recevier) {
        receviers.add(recevier);
    }
    
    private synchronized void addTopic(String... topic) {       
        topics.addAll(Arrays.asList(topic));
    }
    


    @Override
    public void connect() throws MqttException, MqttSecurityException {
//        com.kddi.tmc.pushasg.common.log.Logger log = new com.kddi.tmc.pushasg.common.log.Logger(getClass());
        
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
                    MqttClient.generateClientId() + "_" + this.hashCode(), new MemoryPersistence());
            mqttClient.setCallback(this);

            mqttClient.connect(connOpt);
            isClosed = false;
        }

    }

    @Override
    public void subscribe(String topic, MqttMessageProcessable recevier) throws MqttException {
        if (mqttClient == null || !mqttClient.isConnected()) {
            // 未接続の場合は何もしない
            return;
        }
        addRecevier(recevier);
        addTopic(topic);
        mqttClient.subscribe(topic, mqttConnProp.getQos());
    }
    
    private void reSubscribe() throws MqttException {
        if (!this.topics.isEmpty()) {
            String[] cachedTopics = this.topics.toArray(new String[0]);

            int[] qos = new int[cachedTopics.length];
            Arrays.fill(qos, mqttConnProp.getQos());

            mqttClient.subscribe(cachedTopics, qos);
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
                return;
            } catch (MqttException e) {
            	log.error("MqttException:" + e.getMessage());
                try {
                    Thread.sleep(retryInterval * 1000);
                } catch (InterruptedException e1) {
                    return;
                }
            }
        }
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
                }
            }
        }
    }
    
    private MqttMessageProcessable getRecevier(String topic) {
        MqttMessageProcessable recevier = null;
        for (int idx = 0; idx < receviers.size(); idx++) {
            recevier = receviers.get(idx);
            if (recevier.supports(topic)) {
                return recevier;
            }
        }

        return null;
    }

    @Override
    public void messageArrived(String topic, MqttMessage msg) {
        MqttConnection.setCurrentConn(MqttConnections.getMqttConnections(POOL_TYPE.PUB).getConnectionTo(this.getBrokerUrl()));

        MqttMessageProcessable recevier = getRecevier(topic);
        if (recevier != null) {
        	recevier.process(topic, msg.getId(), msg.getQos(), msg.getPayload());
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        // 接続がロストした場合は、接続できるまで再接続を試みる
    	log.warn("connectionLost!! cause:" + cause.getMessage());

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
                            mqttClient.connect(connOpt);                            
                        	log.warn("connection connected!");
                        }                      
                    }
                }
                reSubscribe();
                return;
            } catch (MqttException e) {
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
