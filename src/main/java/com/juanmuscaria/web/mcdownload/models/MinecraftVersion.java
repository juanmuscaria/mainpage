package com.juanmuscaria.web.mcdownload.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class MinecraftVersion {
  private VersionInfo info;
  private VersionMetadata meta;
}
