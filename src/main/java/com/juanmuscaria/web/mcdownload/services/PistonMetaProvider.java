package com.juanmuscaria.web.mcdownload.services;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.juanmuscaria.web.mcdownload.models.MinecraftVersion;
import com.juanmuscaria.web.mcdownload.models.VersionInfo;
import com.juanmuscaria.web.mcdownload.models.VersionManifest;
import com.juanmuscaria.web.mcdownload.models.VersionMetadata;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

@Service
public class PistonMetaProvider implements MinecraftMetaProvider {
  private static final String PISTON_URL = "https://piston-meta.mojang.com/";
  private final RestTemplate rest = new RestTemplateBuilder().rootUri(PISTON_URL)
    .additionalMessageConverters(new MappingJackson2HttpMessageConverter()).build();
  private final Cache<String, MinecraftVersion> versionCache = Caffeine.newBuilder()
    .expireAfterWrite(1, TimeUnit.DAYS)
    .maximumSize(100L)
    .build();

  private VersionManifest getManifest() {
    return rest.getForObject("/mc/game/version_manifest.json", VersionManifest.class);
  }

  private MinecraftVersion getVersion(String version) {
    return versionCache.get(version, key -> {
      var manifest = getManifest();
      for (VersionInfo info : manifest.getVersions()) {
        if (info.getId().equals(version)) {
          return new MinecraftVersion(info, rest.getForObject(info.getUrl(), VersionMetadata.class));
        }
      }
      return null;
    });
  }

  @Override
  public String indexUrl(String version) {
    return getVersion(version).getMeta().getAssetIndex().getUrl();
  }

  @Override
  public String jarUrl(String version, boolean server) {
    return getVersion(version).getMeta().getDownloads().get(server ? "server" : "client").getUrl();
  }

  @Override
  public String jsonUrl(String version) {
    return getVersion(version).getInfo().getUrl();
  }
}
