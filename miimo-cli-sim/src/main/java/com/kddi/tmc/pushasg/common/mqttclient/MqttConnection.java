package com.kddi.tmc.pushasg.common.mqttclient;

import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;

/**
 * MQTTブローカーとの接続を操作するオブジェクトのインターフェース。<br>
 * 
 * org.eclipse.paho.client.mqttv3.MqttClientのAPIを要件にあわせて扱いやすくしたラッパー。<br>
 * 
 * 
 * 実装クラスは以下の制約を保障する<br>
 * 
 * ・用途上、publishはスレッドセーフを保障する。（他メソッドについては保障しない。）<br>
 * ・ブローカーとの接続確立後に、何らかの理由で接続が切断された場合は、指定のインターバル間隔にて自動で再接続を試みる。<br>
 *  再接続の処理は、接続が成功するか、closeが呼び出されるまで継続する。
 * 
 */
public interface MqttConnection extends MqttCallback {
	
	/**
	 * MQTTブローカーと接続する。<br>
	 * 他メソッドを利用前に呼び出しておく必要がある。
	 * 接続済の場合は何もしない。
	 * 
	 * @throws MqttException
	 * @throws MqttSecurityException
	 */
	public void connect() throws MqttException, MqttSecurityException;

	/**
	 * 指定されたtopicをSubscribeし、メッセージを受信したらreceiverにメッセージ内容を渡す。<br>
	 * MQTTブローカーと未接続の場合は何もしない。
	 * 
	 * @param topic	subscribeするトピック名。
	 * @param recevier	受信したメッセージを処理するインスタンス。
	 * @throws MqttException
	 */
	public void subscribe(String topic, MqttMessageProcessable recevier) throws MqttException;
	
	/**
	 * 指定された複数のtopicをSubscribeし、メッセージを受信したらreceiverにメッセージ内容を渡す。<br>
	 * トピック名がワイルドカードで集約できない場合のみこちらを使う。
	 * MQTTブローカーと未接続の場合は何もしない。
	 * 
	 * @param topics	subscribeする複数のトピック名
	 * @param recevier	受信したメッセージを処理するクラス
	 * @throws MqttException
	 */
	public void subscribe(String[] topics, MqttMessageProcessable recevier) throws MqttException;
	
	/**
     * 指定されたtopicをunsubscribe。<br>
     * MQTTブローカーと未接続の場合は何もしない。
     * 
     * @param topic subscribeするトピック名。
     * @throws MqttException
     */
    public void unsubscribe(String topic) throws MqttException;
    
    /**
     * 指定された複数のtopicをunsubscribe。<br>
     * トピック名がワイルドカードで集約できない場合のみこちらを使う。
     * MQTTブローカーと未接続の場合は何もしない。
     * 
     * @param topics    subscribeする複数のトピック名
     * @throws MqttException
     */
    public void unsubscribe(String[] topics) throws MqttException;
	
	/**
	 * 指定されたtopicに対して、指定されたpayloadをpublishする。<br>
	 * MQTTブローカーと未接続の場合は何もしない。<br>
	 * 
	 * oneM2Mの以下の仕様したMQTT publishを行う。<br>
	 * ・MQTT's "QoS 1" message reliability level.<br>
	 * ・It does not use the following features: Retained Messages.<br>
	 * 
	 * QoS 1に準拠するため、publishに失敗したら、所定の間隔でリトライする。<br>
	 * ただし、無限リトライは輻輳につながる可能性があるため、有限のリトライ回数上限を超えた場合は失敗と判断、MqttExceptionをthrowする。<br>
	 * 
	 * リトライ中にclose()が呼びだされた場合はリトライを停止する。
	 * 
	 * @param topic		メッセージを送信する宛先トピック名
	 * @param payload	メッセージ内容
	 * @throws MqttException
	 */
	public void publish(String topic, byte[] payload) throws MqttException;
	
	/**
	 * MQTTブローカーと接続を切断する。<br>
	 * 各種リトライ処理（切断時の再接続、publishの再送信）を実施している場合はそれも停止する。<br>
	 * 
	 * @throws MqttException
	 */
	public void close() throws MqttException;
	
	/**
	 * MQTTブローカーとの接続状態を返す。
	 * 
	 * @return	MQTTブローカーと接続(true)/未接続（false）
	 */
	public boolean isConnected();
	
	
	/**
	 * @return　MQTTブローカーのURL
	 */
	public String getBrokerUrl();
}
