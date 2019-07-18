package com.kddi.tmc.pushasg.common.mqttclient;

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
final class MqttConnectionProperties {

	private static final MqttConnectionProperties INSTANCE = new MqttConnectionProperties();
	
	// 各プロパティの内容はmqtt-connection.conf参照
	private int keepAliveInterval;
	private int qos;
	private int maxRetryCount;
	private int retryInterval;
	private int reconnectInterval;

	private MqttConnectionProperties() {
		final Config config = ConfigFactory.load("mqtt-connection");

		keepAliveInterval = config.getInt("keep_alive_interval");
		qos = config.getInt("qos");
		maxRetryCount = config.getInt("publish_max_retry_count");
		retryInterval = config.getInt("publish_retry_interval");
		reconnectInterval = config.getInt("reconnect_interval");
	}

	
	/**
	 * @return	本クラスのインスタンス（シングルトン）を返す
	 */
	public static MqttConnectionProperties getInstance() {
		return INSTANCE;
	}
}
