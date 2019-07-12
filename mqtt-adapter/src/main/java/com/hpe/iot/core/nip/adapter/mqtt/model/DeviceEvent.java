package com.hpe.iot.core.nip.adapter.mqtt.model;
/*
 * Copyright (c) Hewlett Packard Enterprise Company, L.P. Limited 2018.
 */

import lombok.Data;

/**
 * DeviceEvent.
 */
@Data
public class DeviceEvent {
  private Header header = new Header();
  private Payload payload = new Payload();

  @Data
  public static class Header {
    private String deviceId = "";
    private String messageId = "";
    private String payLoadVersion = "";
  }

  @Data
  public static class Payload {
    private String name = "";
    private String type = "";
    private String deviceType = "";
    private String attribute = "";
    private String value = "";
  }

}
