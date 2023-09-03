package com.juanmuscaria.web.mcdownload.services;

public interface MinecraftMetaProvider {
  String indexUrl(String version);

  String jarUrl(String version, boolean server);

  String jsonUrl(String version);
}
