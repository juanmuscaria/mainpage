package com.juanmuscaria.web.mainpage.service;

import com.juanmuscaria.web.mainpage.TeapotConfig;
import com.juanmuscaria.web.mainpage.model.ProjectInfo;
import lombok.Getter;
import org.kohsuke.github.GitHubBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CountDownLatch;

@Service
public class TeapotProjectProvider {
  private static final Logger logger = LoggerFactory.getLogger(TeapotProjectProvider.class);
  @Getter
  private final SortedSet<ProjectInfo> projects = new ConcurrentSkipListSet<>((o1, o2) -> {
    var comp = Integer.compare(o2.stars(), o1.stars());
    if (comp == 0) {
      // Tiebreaker, prevent two distinct projects from being "equal" and preserve a fixed ordering
      return (o2.owner() + o2.id()).compareTo(o1.owner() + o1.id());
    }
    return comp;
  });
  private final CountDownLatch latch = new CountDownLatch(1);
  @Autowired
  private TeapotConfig config;

  @Scheduled(fixedRate = 12 * 60 * 60 * 1_000, initialDelay = 0)
  @Retryable(backoff = @Backoff(delay = 60 * 1_000), recover = "logError")
  public void fetchProjects() throws Throwable {
    try {
      var github = new GitHubBuilder().build();
      try {
        config.getProjects().forEach((org, orgRepos) -> orgRepos.forEach((repo, metadata) -> {
          try {
            var ghRepository = github.getRepository(org + '/' + repo);
            var displayName = metadata == null || metadata.getDisplayName() == null
              ? ghRepository.getName() : metadata.getDisplayName();
            var project = new ProjectInfo(org, repo, displayName, ghRepository.getDescription(), ghRepository.getLanguage(),
              ghRepository.getStargazersCount(), ghRepository.getForksCount());
            projects.add(project); // Overwrite old info
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        }));
        latch.countDown();
      } catch (RuntimeException e) {
        throw e.getCause(); // Unwrap the original exception
      }
    } catch (Throwable e) {
      logger.warn("Fetch failed!", e);
      throw e;
    }
  }

  public void awaitReady() throws InterruptedException {
    latch.await();
  }

  @Recover
  public void logError(Throwable e) {
    logger.warn("Unable to fetch projects!", e);
  }
}
