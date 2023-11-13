package com.juanmuscaria.web.mainpage.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.juanmuscaria.web.mainpage.model.ProjectInfo;
import org.kohsuke.github.GitHub;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Service
public class CachedGithubProjectFetcher implements GitProjectFetcher {

  private final Cache<String, ProjectInfo> infoCache = Caffeine.newBuilder()
    .expireAfterWrite(1, TimeUnit.DAYS)
    .build();

  @Override
  @NonNull
  public ProjectInfo fetch(String org, String projectId) {
    var project = infoCache.getIfPresent(org + '/' + projectId);
    if (project != null) {
      return project;
    }
    try {
      var github = GitHub.connectAnonymously();
      var repo = github.getRepository(org + '/' + projectId);
      project = new ProjectInfo(org, projectId, repo.getName(), repo.getDescription(), repo.getLanguage(), repo.getStargazersCount(), repo.getForksCount());
      infoCache.put(org + '/' + projectId, project);
    } catch (IOException e) {
      project = new ProjectInfo(org, projectId, projectId, "Unable to connect to connect to github :(",
        "X", -1, -1);
    }

    return project;
  }
}
