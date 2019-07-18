package com.kddi.tmc.pushasg.common.mqttclient;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.paho.client.mqttv3.MqttException;

import com.kddi.tmc.pushasg.common.util.ApplicationProperties;

/**
 * com.kddi.tmc.pushgw.mqttclient.MqttConnectionの実装クラス用のファクトリ兼インスタンス管理クラス。<br>
 * 
 * MQTT接続は1つのブローカーに対して1つあればいいため、<br>
 * 本クラスのスタティックファクトリメソッドによってcom.kddi.tmc.pushgw.mqtt.client.
 * MqttConnectionの利用クラスに1ブローカーあたり1インスタンスの制約を強制する。<br>
 * 
 */
public final class MqttConnections {

	private static final Map<String, Map<String, MqttConnection>> connectionPools = new HashMap<>();

	private MqttConnections() {
	}

	/**
	 * 指定されたMQTTブローカーとの接続を扱うcom.kddi.tmc.pushgw.mqttclient.
	 * MqttConnectionの実装クラスのインスタンスを返すスタティックファクトリメソッド。<br>
	 * 1ブローカーあたり1接続の制約で生成したインスタンスを返す。<br>
	 * 
	 * @return 指定されたMQTTブローカーとの接続を扱うcom.kddi.tmc.pushgw.mqttclient.
	 *         MqttConnectionのインスタンス
	 */
	public static MqttConnection getConnectionTo(String namespace,
			String mqttBrokerURL, String... sslParams) {
	    Map<String, MqttConnection> connectionList = getPool(namespace);
	    
		MqttConnection connection = connectionList.get(mqttBrokerURL);
		if (connection == null) {
			synchronized (connectionList) {
				connection = connectionList.get(mqttBrokerURL);
				if (connection == null) {
					if (mqttBrokerURL.contains("ssl")) {
						connection = new TlsMqttConnection(mqttBrokerURL, sslParams);
					} else {
						connection = new PlainMqttConnection(mqttBrokerURL);
					}

					connectionList.put(mqttBrokerURL, connection);
				}
			}
		}
		return connection;
	}
	
	private static Map<String, MqttConnection> getPool(String namespace) {
	    Map<String, MqttConnection> pool = connectionPools.get(namespace);
	    
	    if (pool == null) {
	        synchronized (connectionPools) {
	            pool = connectionPools.get(namespace);
	            if (pool == null) {
	                pool = new HashMap<>();
	                connectionPools.put(namespace, pool);
	            }
	        }
	    }
	    
	    return pool;
	}
	
    public static void release(String namespace) throws MqttException {
        MqttException mqttExp = null;
        synchronized (connectionPools) {

            Map<String, MqttConnection> connectionList = connectionPools
                    .get(namespace);
            for (MqttConnection conn : connectionList.values()) {
                try {
                    conn.close();
                } catch (MqttException e) {
                    mqttExp = e;
                }
            }
            connectionList.clear();

            connectionPools.remove(namespace);
        }

        if (mqttExp != null) {
            throw mqttExp;
        }
    }
	
    public static void releaseAll() throws MqttException {
        MqttException mqttExp = null;
        synchronized (connectionPools) {
            for (Map<String, MqttConnection> connectionList : connectionPools
                    .values()) {
                for (MqttConnection conn : connectionList.values()) {
                    try {
                        conn.close();
                    } catch (MqttException e) {
                        mqttExp = e;
                    }
                }
                connectionList.clear();
            }

            connectionPools.clear();
        }

        if (mqttExp != null) {
            throw mqttExp;
        }
    }
}
