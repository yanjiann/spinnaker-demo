package com.hpe.iot.core.nip.adapter.mqtt.connections;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import lombok.Getter;

/**
 * MQTT接続等に使う設定値を取り扱うクラス。<br>
 * スタティックファクトリメソッドを使って、シングルトンで運用する。<br>
 * 可視性をデフォルトにし、MQTT接続の詳細をパッケージ外の利用者が意識しなくて済むにようにする。<br>
 * 
 */
@Getter
public final class MqttConnectionProperties {

	private static final MqttConnectionProperties INSTANCE = new MqttConnectionProperties();
	
	// 各プロパティの内容はmqtt-connection.conf参照
	private int keepAliveInterval;
	private int qos;
	private int maxRetryCount;
	private int retryInterval;
	private int reconnectInterval;
	
//	private int limitQuantity;
//	private long limitLockTimeout;
//	private int limitRetryCount;
//	private long limitRetryInterval;
	private int maxInflight;
	private int publisherConnsPerRoute;

	private MqttConnectionProperties() {
	    
		final Config config = ConfigFactory.load("iot-mqtt");

		keepAliveInterval = config.getInt("mqtt.keep_alive_interval");
		qos = config.getInt("mqtt.publisher.qos");
		maxRetryCount = config.getInt("mqtt.publisher.max_retry_count");
		retryInterval = config.getInt("mqtt.publisher.retry_interval");
		reconnectInterval = config.getInt("mqtt.broker.reconnectInterval");
		
//		limitQuantity = config.getInt("mqtt.publisher.limit.quantity");
//		limitLockTimeout = config.getLong("mqtt.publisher.limit.lock.timeout");
//		limitRetryCount = config.getInt("mqtt.publisher.limit.retry.count");
//		limitRetryInterval=config.getLong("mqtt.publisher.limit.retry.interval");
//		
		maxInflight = config.getInt("mqtt.publisher.max.inflight");
		
		publisherConnsPerRoute = config.getInt("mqtt.publisher.conns.per.route");
	}

	
	/**
	 * @return	本クラスのインスタンス（シングルトン）を返す
	 */
	public static MqttConnectionProperties getInstance() {
		return INSTANCE;
	}
}
