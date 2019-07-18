package com.kddi.tmc.pushasg.common.mqttclient;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import com.kddi.tmc.pushasg.common.util.ApplicationProperties;

/**
 * MqttConnectionの実装クラス。 TLS接続用。
 * 接続オプションのみ、スーパークラスと異なる。
 */
class TlsMqttConnection extends PlainMqttConnection {
	
	/**
	 * http://d.hatena.ne.jp/rx7/20131202/p1
	 * https://gist.github.com/sethwebster/b48d7c872fe397c1db11
	 * サーバ証明書
	 * # openssl genrsa 2048 > server.key
	 * # openssl req -new -key server.key > server.csr
	 * # openssl x509 -days 730 -req -signkey server.key < server.csr > server.crt
	 * # cat server.key server.crt > server.pem
	 * # /etc/haproxy/haproxy.cfg
	 * #   bind *:8883 ssl crt /etc/haproxy/certs/server.pem ca-file /etc/haproxy/CA/ca-crt.pem verify required
	 * # Client
	 * #   keytool -importcert -alias asg.server -v -trustcacerts -file server.crt -keystore cacerts
	 * # 
	 * # クライアント認証
	 * # http://qiita.com/akito1986/items/8eb41f5a43bb9421ae79 
	 * # http://hamasyou.com/blog/2007/10/27/weblogic-ssl/
	 * # https://blog.toright.com/posts/4024/haproxy-%E6%95%B4%E5%90%88-ssltls-client-certificate-%E6%95%99%E5%AD%B8.html
	 * #    キーストアを作成する
	 * #    keytool -genkey -alias myserver -keyalg RSA -keystore myKeystore.jks
	 * #    サーバ証明書発行要求を作成する
	 * #    keytool -certreq -alias myserver -keystore myKeystore.jks -file myserver_certreq.csr
	 * #    認証局側手順-秘密キーを作成
	 * #    openssl genrsa -out ca-privatekey.pem 2048
	 * #    認証局側手順-CSR作成
	 * #    openssl req -new -key ca-privatekey.pem -out ca-csr.pem
	 * #    認証局側手順-証明書作成
	 * #    openssl req -x509 -key ca-privatekey.pem -in ca-csr.pem -out ca-crt.pem -days 3560
	 * #    サーバー側手順-認証局の証明書で署名したサーバー証明書を作成。
	 * #    openssl x509 -req -CA ca-crt.pem -CAkey ca-privatekey.pem -CAcreateserial -in server-csr.pem -out server-crt.pem -days 3650
	 * #    CA 局サーバ証明書をキーストアに保存する
	 * #    keytool -import -noprompt -trustcacerts -alias myca -file ca-crt.pem -keystore myKeystore.jks -storepass ippush
	 * #    サーバ証明書をキーストアに保存する
	 * #    keytool -import -noprompt -alias myserver -file server-crt.pem -keystore myKeystore.jks -storepass ippush
	 * #    
	 * @param brokerUrl
	 */
	TlsMqttConnection(String brokerUrl, String... sslParams) {
		super(brokerUrl);

		try {
			char[] keyPass = sslParams[0].toCharArray();
			KeyStore keyStore = KeyStore.getInstance("JKS");
			keyStore.load(this.getClass().getResourceAsStream(sslParams[1]), keyPass);
			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			kmf.init(keyStore, keyPass);
			
			keyPass = sslParams[2].toCharArray();
			KeyStore trustStore = KeyStore.getInstance("JKS");
			trustStore.load(this.getClass().getResourceAsStream(sslParams[3]), keyPass);
			TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
			tmf.init(trustStore);

			SSLContext sslContext = SSLContext.getInstance("TLSv1");
			sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());
			connOpt.setSocketFactory(sslContext.getSocketFactory());
		} catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException | CertificateException
				| IOException | UnrecoverableKeyException e) {
			logger.debug("TLS socket generation failed.");
		}
	}

}
