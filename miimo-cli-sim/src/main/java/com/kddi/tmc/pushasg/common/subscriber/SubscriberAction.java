package com.kddi.tmc.pushasg.common.subscriber;

@FunctionalInterface
public interface SubscriberAction {
	boolean CONTINUE = true;
	boolean STOP = false;

	boolean doAction(String topic, byte[] payload);
}
