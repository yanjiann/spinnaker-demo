package com.hpe.iot.core.nip.adapter.mqtt.connections;

/**
 * MQTT subscribeで受信したメッセージを処理するオブジェクトのインターフェース。<br>
 * com.kddi.tmc.pushgw.mqtt.client.MqttConnection.subscribeを利用するには、本インターフェースを実装したクラスを引数に指定する必要がある。
 * 
 */
public interface MqttMessageProcessable {
	
	/**
	 * MQTT subscribeで受信したメッセージを処理するメソッド。<br>
	 * メッセージ受信時にコールバック処理にて以下の引数が渡される。
	 * 
	 * @param topic	受信したメッセージの宛先トピック名
	 * @param id	受信したメッセージのID
	 * @param qos	受信したメッセージのQoS
	 * @param payload	受信したメッセージの内容
	 * 
	 * 実装クラスは以下の戻り値を保障しなければならない。
	 * @return	受信メッセージの処理結果をtrue/falseを返す
	 */
	public boolean process(String topic, int id, int qos, byte[] payload);
	
	/**
	 * subscribeが該当topicを支持するかを判定する
	 * 
	 * @param topic
	 * @return
	 */
	public boolean supports(String topic);
}
