package com.kddi.tmc.pushasg.common.subscriber;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
public class SubscriberCommand implements Runnable {
	private HashMap<String, List<SubscriberAction>> actionsMap = new HashMap<String, List<SubscriberAction>>();
	private static final String DEFAULT_ACTION_KEY = "default_a_key"; 

	private String topic;
	private byte[] payload;

	public void setParams(String topic, byte[] payload) {
		this.topic = topic;
		this.payload = payload;
	}

	public SubscriberCommand appendAction(SubscriberAction action) {
		return appendAction(DEFAULT_ACTION_KEY, action);
	}
	
	public SubscriberCommand appendAction(String topic, SubscriberAction action) {
		pushAction(topic, action);
		return this;
	}

	@Override
	public void run() {
		List<SubscriberAction> actions = getActions(topic);		
		
		for (SubscriberAction action : actions) {
			if (action.doAction(topic, payload) == SubscriberAction.STOP) {
				break;
			}
		}
	}
	
	private void pushAction(String topic, SubscriberAction action) {
		List<SubscriberAction> aList = actionsMap.get(topic);
		if (aList == null) {
			aList = new ArrayList<SubscriberAction>();
			actionsMap.put(topic, aList);
		}
		
		aList.add(action);
	}
	
	private List<SubscriberAction> getActions(String topic) {
		List<SubscriberAction> res = actionsMap.get(topic);
		if (res == null) {
			res = actionsMap.get(DEFAULT_ACTION_KEY);
		}
		if (res == null) {
			res = new ArrayList<SubscriberAction>(0);
		}
		
		return res;
	}

}
