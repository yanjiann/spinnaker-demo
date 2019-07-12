package com.hpe.iot.core.nip.adapter.mqtt.subscriber;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.paho.client.mqttv3.MqttException;
import com.hpe.iot.core.nip.adapter.mqtt.connections.MqttConnection;
import com.hpe.iot.core.nip.adapter.mqtt.connections.MqttConnections;
import com.hpe.iot.core.nip.adapter.mqtt.connections.TlsMqttConnection;
import com.hpe.iot.core.nip.adapter.mqtt.connections.PlainMqttConnection;

/**
 * MQTT Subscriber Class
 * 
 * @author Moniruzzaman Md
 *
 */
public class MQTTSubscriber implements MqttConnections {

    private final Map<String, MqttConnection> connectionList = new HashMap<>();

    /**
     * 指定されたMQTTブローカーとの接続を扱うcom.kddi.tmc.pushgw.mqttclient.
     * MqttConnectionの実装クラスのインスタンスを返すスタティックファクトリメソッド。<br>
     * 1ブローカーあたり1接続の制約で生成したインスタンスを返す。<br>
     * 
     * @return 指定されたMQTTブローカーとの接続を扱うcom.kddi.tmc.pushgw.mqttclient.
     *         MqttConnectionのインスタンス
     */
    public MqttConnection getConnectionTo(String mqttBrokerURL) {
        MqttConnection connection = connectionList.get(mqttBrokerURL);
        if (connection == null) {
            synchronized (connectionList) {
                connection = connectionList.get(mqttBrokerURL);
                if (connection == null) {
                    if (mqttBrokerURL.contains("ssl")) {
                        connection = new TlsMqttConnection(mqttBrokerURL);
                    } else {
                        connection = new PlainMqttConnection(mqttBrokerURL);
                    }

                    connectionList.put(mqttBrokerURL, connection);
                }
            }
        }
        return connection;
    }

    public void release() throws MqttException {
        MqttException mqttExp = null;
        synchronized (connectionList) {
            for (MqttConnection conn : connectionList.values()) {
                try {
                    conn.close();
                } catch (MqttException e) {
                    mqttExp = e;
                }
            }

            connectionList.clear();
        }

        if (mqttExp != null) {
            throw mqttExp;
        }
    }

    /* (non-Javadoc)
     * @see com.kddi.tmc.pushasg.common.mqttclient.MqttConnections#init()
     */
    @Override
    public MqttConnections init() throws MqttException {
        return this;
        
    }
}
