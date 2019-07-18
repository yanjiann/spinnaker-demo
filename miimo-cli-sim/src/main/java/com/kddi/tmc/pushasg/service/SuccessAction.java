package com.kddi.tmc.pushasg.service;

@FunctionalInterface
public interface SuccessAction<T> {

    public void doAction(T res);
}
