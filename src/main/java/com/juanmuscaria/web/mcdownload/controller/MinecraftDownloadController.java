package com.juanmuscaria.web.mcdownload.controller;

import com.juanmuscaria.web.mcdownload.services.MinecraftMetaProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping(MinecraftDownloadController.BASE_PATH)
@RestController
public class MinecraftDownloadController {
  static final String BASE_PATH = "/Minecraft.Download";

  @Autowired
  MinecraftMetaProvider provider;

  @RequestMapping("/indexes/{id}.json")
  ResponseEntity<Void> indexes(@PathVariable("id") String id) {
    return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
      .header(HttpHeaders.LOCATION, provider.indexUrl(id)).build();
  }

  @RequestMapping("/versions/{version}/{file}")
  ResponseEntity<Void> versions(@PathVariable("version") String version, @PathVariable("file") String file) {
    if (file.equals(version + ".json")) {
      return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
        .header(HttpHeaders.LOCATION, provider.jsonUrl(version)).build();
    } else if (file.endsWith(version + ".jar")) {
      return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
        .header(HttpHeaders.LOCATION, provider.jarUrl(version, file.startsWith("minecraft_server."))).build();
    }
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
  }
}
