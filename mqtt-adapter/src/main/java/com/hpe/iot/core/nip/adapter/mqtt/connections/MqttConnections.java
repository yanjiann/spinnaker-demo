package com.hpe.iot.core.nip.adapter.mqtt.connections;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.paho.client.mqttv3.MqttException;

import com.hpe.iot.core.nip.adapter.mqtt.publisher.MQTTPublisher;
import com.hpe.iot.core.nip.adapter.mqtt.subscriber.MQTTSubscriber;

/**
 * com.kddi.tmc.pushgw.mqttclient.MqttConnectionの実装クラス用のファクトリ兼インスタンス管理クラス。<br>
 * 
 * MQTT接続は1つのブローカーに対して1つあればいいため、<br>
 * 本クラスのスタティックファクトリメソッドによってcom.kddi.tmc.pushgw.mqtt.client.
 * MqttConnectionの利用クラスに1ブローカーあたり1インスタンスの制約を強制する。<br>
 * 
 */
public interface MqttConnections {

    static Map<POOL_TYPE, MqttConnections> conns = new HashMap<>();

    public static void initialize() throws MqttException {
        conns.put(POOL_TYPE.PUB, new MQTTPublisher().init());
        conns.put(POOL_TYPE.SUB, new MQTTSubscriber().init());
    }

    MqttConnections init() throws MqttException;

    /**
     * 指定されたMQTTブローカーとの接続を扱うcom.kddi.tmc.pushgw.mqttclient.
     * MqttConnectionの実装クラスのインスタンスを返すスタティックファクトリメソッド。<br>
     * 1ブローカーあたり1接続の制約で生成したインスタンスを返す。<br>
     * 
     * @return 指定されたMQTTブローカーとの接続を扱うcom.kddi.tmc.pushgw.mqttclient.
     *         MqttConnectionのインスタンス
     */
    public MqttConnection getConnectionTo(String mqttBrokerURL);

    public void release() throws MqttException;

    public static void releaseAll() throws MqttException {
        MqttException exp = null;
        for (MqttConnections conn : conns.values()) {
            try {
                conn.release();
            } catch (MqttException e) {
                exp = e;
            }
        }

        if (exp != null) {
            throw exp;
        }
    }

    public static MqttConnections getMqttConnections(POOL_TYPE type) {
        return conns.get(type);
    }

    public static enum POOL_TYPE {
        PUB, SUB
    }
}
