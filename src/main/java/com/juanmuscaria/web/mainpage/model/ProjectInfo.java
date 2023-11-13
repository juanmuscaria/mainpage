package com.juanmuscaria.web.mainpage.model;

import lombok.With;

@With
public record ProjectInfo(String owner, String id, String displayName, String description, String lang, int stars,
                          int forks) {

  @Override
  public boolean equals(Object other) {
    if (this == other) return true;
    if (other instanceof ProjectInfo that) {
      return owner.equals(that.owner) && id.equals(that.id);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int result = owner.hashCode();
    result = 31 * result + id.hashCode();
    return result;
  }
}
