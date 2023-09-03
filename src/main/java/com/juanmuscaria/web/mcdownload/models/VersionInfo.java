package com.juanmuscaria.web.mcdownload.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
public class VersionInfo {
  private String id;
  private String type;
  private String url;
  private String time;
  private String releaseTime;
}
