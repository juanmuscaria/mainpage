package com.juanmuscaria.web.mcdownload.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Map;

@Getter
@ToString
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
public class VersionMetadata {
  private AssetIndex assetIndex;
  private Map<String, Download> downloads;

  @Getter
  @ToString
  @EqualsAndHashCode
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Download {
    private String url;
  }

  @Getter
  @ToString
  @EqualsAndHashCode
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class AssetIndex {
    private String url;
  }
}
