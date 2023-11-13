package com.juanmuscaria.web.lang;

import jakarta.annotation.Nullable;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class MultiMessageSource implements MessageSource {
  private final List<MessageSource> sources;
  private final boolean useCodeAsDefaultMessage;

  public MultiMessageSource(Collection<MessageSource> sources, boolean useCodeAsDefaultMessage) {
    this.sources = new ArrayList<>(sources);
    this.useCodeAsDefaultMessage = useCodeAsDefaultMessage;
  }

  @Override
  public String getMessage(String code, @Nullable Object[] args, @Nullable String defaultMessage, Locale locale) {
    var msg = tryGetMessage(code, args, locale);
    if (msg == null) {
      if (useCodeAsDefaultMessage && defaultMessage == null) {
        return code;
      }
      return defaultMessage;
    }
    return msg;
  }

  @Override
  public String getMessage(String code, @Nullable Object[] args, Locale locale) throws NoSuchMessageException {
    var msg = tryGetMessage(code, args, locale);
    if (msg == null) {
      if (useCodeAsDefaultMessage) {
        return code;
      }
      throw new NoSuchMessageException(code, locale);
    }
    return msg;
  }

  @Override
  public String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException {
    String[] codes = resolvable.getCodes();
    var suppressed = new ArrayList<Exception>(sources.size());
    for (MessageSource source : sources) {
      try {
        return source.getMessage(resolvable, locale);
      } catch (NoSuchMessageException exception) {
        suppressed.add(exception);
      }
    }
    var exception = new NoSuchMessageException(!ObjectUtils.isEmpty(codes) ? codes[codes.length - 1] : "", locale);
    for (Exception e : suppressed) {
      exception.addSuppressed(e);
    }
    throw exception;
  }

  @Nullable
  private String tryGetMessage(String code, @Nullable Object[] args, Locale locale) {
    for (MessageSource source : sources) {
      var msg = source.getMessage(code, args, null, locale);
      if (msg == null) {
        continue;
      }
      return msg;
    }
    return null;
  }
}
