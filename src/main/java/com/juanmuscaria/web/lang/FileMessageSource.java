package com.juanmuscaria.web.lang;

import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.support.AbstractResourceBasedMessageSource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Copy-paste of {@link ReloadableResourceBundleMessageSource} for loading whole file contents;
 */
public class FileMessageSource extends AbstractResourceBasedMessageSource
  implements ResourceLoaderAware {
  private final Logger logger = LoggerFactory.getLogger(FileMessageSource.class);
  private final ConcurrentMap<String, FileHolder> cachedFiles = new ConcurrentHashMap<>();
  private final ConcurrentMap<String, Map<Locale, Set<String>>> cachedFilenames = new ConcurrentHashMap<>();
  private final Set<String> extensions = new LinkedHashSet<>(List.of(".txt", ""));
  private ResourceLoader resourceLoader = new DefaultResourceLoader();

  @Override
  public void setResourceLoader(@Nullable ResourceLoader resourceLoader) {
    this.resourceLoader = (resourceLoader != null ? resourceLoader : new DefaultResourceLoader());
  }

  @Override
  protected String resolveCodeWithoutArguments(String code, Locale locale) {
    var fileHolder = getFileHolder(code, locale);
    return fileHolder == null ? null : fileHolder.getContent();
  }

  @Override
  protected MessageFormat resolveCode(String code, Locale locale) {
    var fileHolder = getFileHolder(code, locale);
    return fileHolder == null ? null : fileHolder.getMessageFormat(locale);
  }

  @Nullable
  private FileHolder getFileHolder(String code, Locale locale) {
    for (String basename : getBasenameSet()) {
      Set<String> filenames = calculateAllFilenames(codeToPath(basename, code), locale);
      for (String filename : filenames) {
        var file = resolveFile(filename);
        if (file.getContent() != null) {
          return file;
        }
      }
    }
    return null;
  }

  private FileHolder resolveFile(String filename) {
    var fileHolder = this.cachedFiles.get(filename);
    long originalTimestamp = -2;

    if (fileHolder != null) {
      originalTimestamp = fileHolder.getRefreshTimestamp();
      if (originalTimestamp == -1 || originalTimestamp > System.currentTimeMillis() - getCacheMillis()) {
        return fileHolder;
      }
    } else {
      fileHolder = new FileHolder();
      var existingHolder = this.cachedFiles.putIfAbsent(filename, fileHolder);
      if (existingHolder != null) {
        fileHolder = existingHolder;
      }
    }

    if (fileHolder.getRefreshTimestamp() >= 0) {
      if (!fileHolder.refreshLock.tryLock()) {
        return fileHolder;
      }
    } else {
      fileHolder.refreshLock.lock();
    }
    try {
      var existingHolder = this.cachedFiles.get(filename);
      if (existingHolder != null && existingHolder.getRefreshTimestamp() > originalTimestamp) {
        return existingHolder;
      }
      return refreshFile(filename, fileHolder);
    } finally {
      fileHolder.refreshLock.unlock();
    }
  }

  private FileHolder refreshFile(String filename, @Nullable FileHolder fileHolder) {
    long refreshTimestamp = (getCacheMillis() < 0 ? -1 : System.currentTimeMillis());
    for (String extension : extensions) {
      Resource resource = this.resourceLoader.getResource(filename + extension);
      if (resource.exists()) {
        long fileTimestamp = -1;
        if (getCacheMillis() >= 0) {
          try {
            fileTimestamp = resource.lastModified();
            if (fileHolder != null && fileHolder.getFileTimestamp() == fileTimestamp) {
              fileHolder.setRefreshTimestamp(refreshTimestamp);
              return fileHolder;
            }
          } catch (IOException ignored) {
          }
        }
        try {
          fileHolder = new FileHolder(resource.getContentAsString(Charset.defaultCharset()), fileTimestamp);
          fileHolder.setRefreshTimestamp(refreshTimestamp);
          this.cachedFiles.put(filename, fileHolder);
          return fileHolder;
        } catch (IOException ex) {
          logger.warn("Could not read file [{}]", resource.getFilename(), ex);
          fileHolder = new FileHolder();
        }
      }
    }

    fileHolder = new FileHolder();
    fileHolder.setRefreshTimestamp(refreshTimestamp);
    this.cachedFiles.put(filename, fileHolder);
    return fileHolder;
  }

  protected Set<String> calculateAllFilenames(String basePath, Locale locale) {
    logger.debug("calculating files for locale {} with basePath {}", locale, basePath);
    Map<Locale, Set<String>> localeMap = this.cachedFilenames.get(basePath);
    if (localeMap != null) {
      var filenames = localeMap.get(locale);
      if (filenames != null) {
        logger.debug("cache hit {}", filenames);
        return filenames;
      }
    }
    LinkedHashSet<String> filenames = new LinkedHashSet<>(7);
    filenames.addAll(calculateFilenamesForLocale(basePath, locale));

    Locale defaultLocale = getDefaultLocale();
    if (defaultLocale != null && !defaultLocale.equals(locale)) {
      List<String> fallbackFilenames = calculateFilenamesForLocale(basePath, defaultLocale);
      filenames.addAll(fallbackFilenames);
    }

    filenames.add(basePath);

    if (localeMap == null) {
      localeMap = new ConcurrentHashMap<>();
      Map<Locale, Set<String>> existing = this.cachedFilenames.putIfAbsent(basePath, localeMap);
      if (existing != null) {
        localeMap = existing;
      }
    }
    localeMap.put(locale, filenames);
    logger.debug("cache miss {}", filenames);
    return filenames;
  }

  protected List<String> calculateFilenamesForLocale(String basename, Locale locale) {
    List<String> result = new ArrayList<>(3);
    String language = locale.getLanguage();
    String country = locale.getCountry();
    String variant = locale.getVariant();
    StringBuilder temp = new StringBuilder(basename);

    temp.append('_');
    if (!language.isEmpty()) {
      temp.append(language);
      result.add(0, temp.toString());
    }

    temp.append('_');
    if (!country.isEmpty()) {
      temp.append(country);
      result.add(0, temp.toString());
    }

    if (!variant.isEmpty() && (!language.isEmpty() || !country.isEmpty())) {
      temp.append('_').append(variant);
      result.add(0, temp.toString());
    }
    return result;
  }

  public void setFileExtensions(String... extensions) {
    this.extensions.clear();
    this.extensions.addAll(Arrays.asList(extensions));
  }

  public void addFileExtensions(String... extensions) {
    this.extensions.addAll(Arrays.asList(extensions));
  }

  private static String codeToPath(String basePath, String code) {
    var path = new StringBuilder(basePath.length() + code.length() + 1);
    if (!basePath.isEmpty()) {
      path.append(basePath);
      if (basePath.charAt(basePath.length() - 1) != '/') {
        path.append('/');
      }
    }

    path.append(code.replace('.', '/'));
    return path.toString();
  }

  @Getter
  protected class FileHolder {
    @Nullable
    private final String content;
    private final long fileTimestamp;
    @Setter
    private volatile long refreshTimestamp = -2;
    private final ReentrantLock refreshLock = new ReentrantLock();
    private final ConcurrentMap<Locale, MessageFormat> cachedMessageFormats =
      new ConcurrentHashMap<>();

    public FileHolder() {
      this.content = null;
      this.fileTimestamp = -1;
    }

    public FileHolder(String content, long fileTimestamp) {
      this.content = content;
      this.fileTimestamp = fileTimestamp;
    }

    @Nullable
    public MessageFormat getMessageFormat(Locale locale) {
      if (this.content == null) {
        return null;
      }
      return cachedMessageFormats.computeIfAbsent(locale, (__) -> createMessageFormat(content, locale));
    }
  }
}
