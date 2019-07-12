package com.hpe.iot.core.nip.adapter.mqtt.redis.model;
/*
 * Copyright (c) Hewlett Packard Enterprise Company, L.P. Limited 2018.
 */

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * RedisDeviceCache.
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RedisDeviceCache {
  private String id;
  private String label;
  private String tid;
  private long timestamp;
}
