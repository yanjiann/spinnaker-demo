package com.hpe.iot.core.nip.adapter.mqtt.model;
/*
 * Copyright (c) Hewlett Packard Enterprise Company, L.P. Limited 2018.
 */

import com.hpe.iot.core.nip.adapter.data.IData;

import lombok.Data;

/**
 * KafkaDeviceEvent.
 */
@Data
public class KafkaDeviceEvent implements IData {
  private String id;
  private String type;
  private String payload;
  private long timestamp;
}
