package com.juanmuscaria.tooling.npm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PackageJson {
  private String name;
  private String version;
  private String description;
  private String main;
  private String module;
  private String sass;
  private String style;
}
