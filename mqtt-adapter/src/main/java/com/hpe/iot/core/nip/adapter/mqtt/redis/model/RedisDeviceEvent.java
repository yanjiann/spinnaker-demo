package com.hpe.iot.core.nip.adapter.mqtt.redis.model;
/*
 * Copyright (c) Hewlett Packard Enterprise Company, L.P. Limited 2018.
 */

import lombok.Data;

/**
 * RedisDeviceEvent.
 */

@Data
public class RedisDeviceEvent {
  private String id;
  private String payload;
  private long timestamp;
}
