package com.hpe.iot.core.nip.adapter.mqtt.publisher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;

import com.hpe.iot.core.nip.adapter.mqtt.connections.MqttConnection;
import com.hpe.iot.core.nip.adapter.mqtt.connections.MqttConnections;
import com.hpe.iot.core.nip.adapter.mqtt.connections.MqttConnectionProperties;
import com.hpe.iot.core.nip.adapter.mqtt.connections.TlsMqttConnection;
import com.hpe.iot.core.nip.adapter.mqtt.connections.PlainMqttConnection;

/**
 * MQTT Publisher class
 * 
 * @author Moniruzzaman Md
 * @version 1.0.0
 *
 */
public class MQTTPublisher implements MqttConnections {

    private static final int CONNS_PER_ROUTE = MqttConnectionProperties
            .getInstance().getPublisherConnsPerRoute();

    private final Map<String, RoundRobinList<MqttConnection>> connectionsList = new HashMap<>();

    /**
     * 指定されたMQTTブローカーとの接続を扱うcom.kddi.tmc.pushgw.mqttclient.
     * MqttConnectionの実装クラスのインスタンスを返すスタティックファクトリメソッド。<br>
     * 1ブローカーあたり1接続の制約で生成したインスタンスを返す。<br>
     * 
     * @return 指定されたMQTTブローカーとの接続を扱うcom.kddi.tmc.pushgw.mqttclient.
     *         MqttConnectionのインスタンス
     */
    public MqttConnection getConnectionTo(String mqttBrokerURL) {
        RoundRobinList<MqttConnection> connections = connectionsList
                .get(mqttBrokerURL);
        if (connections == null) {
            synchronized (connectionsList) {
                connections = connectionsList.get(mqttBrokerURL);
                if (connections == null) {

                    connections = initPool(mqttBrokerURL);
                    connectionsList.put(mqttBrokerURL, connections);
                }
            }
        }

        return available(connections);
    }

    private MqttConnection available(RoundRobinList<MqttConnection> conns) {
        synchronized (conns) {
            MqttConnection conn = conns.next();
            if (!conn.isConnected()) {
                try {
                    conn.connect();
                } catch (MqttSecurityException e) {
                    // do nothing
                } catch (MqttException e) {
                    // do nothing
                }
            }
            return conn;
        }
    }

    private RoundRobinList<MqttConnection> initPool(String mqttBrokerURL) {
        RoundRobinList<MqttConnection> conns = new RoundRobinList<>(
                CONNS_PER_ROUTE);

        MqttConnection conn;
        for (int idx = 0; idx < CONNS_PER_ROUTE; idx++) {
            if (mqttBrokerURL.contains("ssl")) {
                conn = new TlsMqttConnection(mqttBrokerURL);
            } else {
                conn = new PlainMqttConnection(mqttBrokerURL);
            }
            conns.add(conn);
        }

        return conns;
    }

    private static class RoundRobinList<T> extends ArrayList<T> {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;
        
        private int current = 0;

        public RoundRobinList() {
            super();
        }

        public RoundRobinList(int initialCapacity) {
            super(initialCapacity);

        }

        public T next() {
            if (super.isEmpty()) {
                return null;
            }

            if (current >= super.size()) {
                current = 0;
            }

            T value = get(current);
            current++;

            return value;
        }
    }

    public void release() throws MqttException {
        MqttException mqttExp = null;
        synchronized (connectionsList) {
            for (List<MqttConnection> conns : connectionsList.values()) {
                synchronized (conns) {
                    for (MqttConnection conn : conns) {
                        try {
                            conn.close();
                        } catch (MqttException e) {
                            mqttExp = e;
                        }
                    }
                    conns.clear();
                }
            }

            connectionsList.clear();

        }

        if (mqttExp != null) {
            throw mqttExp;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.kddi.tmc.pushasg.common.mqttclient.MqttConnections#init()
     */
    @Override
    public MqttConnections init() throws MqttException {

        return this;
    }
}
