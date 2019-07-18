package com.kddi.tmc.pushasg.service;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.kddi.tmc.pushasg.common.log.TranType;
import com.kddi.tmc.pushasg.common.mqttclient.MqttConnection;
import com.kddi.tmc.pushasg.common.mqttclient.MqttConnections;
import com.kddi.tmc.pushasg.common.util.DateUtil;
import com.kddi.tmc.pushasg.common.util.MessageUtil;
import com.kddi.tmc.pushasg.common.util.MiiContextSupport;
import com.kddi.tmc.pushasg.common.util.SessionManagement;
import com.kddi.tmc.pushasg.common.util.StringUtil;
import com.kddi.tmc.pushasg.common.vo.MiiContext;
import com.kddi.tmc.pushasg.vo.CommandRequestVo;
import com.kddi.tmc.pushasg.vo.CommandResponseVo;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class CmdSubscriberProcessable extends BaseSubscriberProcessable implements MiiContextSupport {
    private static final Logger logger = LoggerFactory
            .getLogger(CmdSubscriberProcessable.class);

    private static final String DEFAULT_RES_MSG_TYPE = "command";
    private static final String RES_RESULT_OK = "accept";
    private static final String RES_RESULT_NG = "reject";
    private MiiContext miiContext = null;
    
    private List<SuccessAction<CommandRequestVo>> commandRcvActions = new ArrayList<SuccessAction<CommandRequestVo>>();
    
    public void addCommandRcvAction(SuccessAction<CommandRequestVo> action) {
    	commandRcvActions.add(action);
    }

    protected com.kddi.tmc.pushasg.common.subscriber.SubscriberAction getAction() {
        return (String topic, byte[] payload) -> {
            return doProcess(topic, payload);
        };
    }

    private boolean doProcess(String topic, byte[] payload) {
        com.kddi.tmc.pushasg.common.log.Logger log = new com.kddi.tmc.pushasg.common.log.Logger(getClass());

        // 受信したMQTT電文(byte[])をStringに変換してoneM2MのRequestPrimitiveにparse
        String strPayload = this.encodeToString(payload);
        CommandRequestVo res = CommandRequestVo.valueOf(strPayload);
        if (res == null) {
            // parseに失敗した場合は不正入力として処理中断。レスポンスが生成できないのでIN-CSEへの応答もしない
            logger.debug("There was something wrong with the received response.");
            log.writeTranLog(TranType.RECV, "-", "-", strPayload);
            log.writeAppLog(MessageUtil.MSGID_UNKOWN_REQ);
            return false;
        }
        
        log.writeTranLog(TranType.RECV, "[Command]", res.getClientId() == null ? "-" : res.getMsgType(), strPayload);
        
        // validate
        if (!DEFAULT_RES_MSG_TYPE.equals(res.getMsgType())) {
            logger.debug("msg type is invalid.");
            return false;
        }
       
        // response
        try {
			pushCommandResp();
		} catch (MqttSecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        return true;
    }

    public void pushCommandResp()
            throws MqttSecurityException, MqttException {
        com.kddi.tmc.pushasg.common.log.Logger log = new com.kddi.tmc.pushasg.common.log.Logger(getClass());
        
        String sessionId = SessionManagement.createSessionId();
        String respTopic = miiContext.getRespTopic();
        String brokerUrl = miiContext.getBrokerUrl();
        String devUniqId = miiContext.getDevUniqId();
        
        if (respTopic == null || brokerUrl == null) {
            throw new IllegalArgumentException("clientid is invalid.");
        }

        MqttConnection conn = MqttConnections.getConnectionTo(miiContext.getDevUniqId(), brokerUrl, miiContext.getSslParams());
        if (!conn.isConnected()) {
            conn.connect();
        }
        CommandResponseVo req = createRequest(devUniqId, sessionId);

        byte[] payload = null;
        try {
            payload = req.toJson().getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        conn.publish(respTopic, payload);
        log.writeTranLog(TranType.SEND, "PUN_S400", req.getMsgId(), StringUtil.encodeToString(payload));
        return;
    }

    private CommandResponseVo createRequest(String devUniqId, String sessionId) {
    	CommandResponseVo reqVo = new CommandResponseVo("command_resq", sessionId,
                new CommandResponseVo.EventTimeVo(DateUtil.getNow(),
                        DateUtil.getNow()), RES_RESULT_OK);
        return reqVo;
    }
    
    @Override
    public void setMiiContext(MiiContext context) {
        this.miiContext = context;
        for (SuccessAction<CommandRequestVo> action : commandRcvActions) {
        	MiiContextSupport.setMiiContext(action, context);
        }
    }

    @Override
    public MiiContext getMiiContext() {
        return miiContext;
    }
}
