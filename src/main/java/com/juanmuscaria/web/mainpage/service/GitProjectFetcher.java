package com.juanmuscaria.web.mainpage.service;

import com.juanmuscaria.web.mainpage.model.ProjectInfo;
import org.springframework.lang.NonNull;

public interface GitProjectFetcher {

    @NonNull
    ProjectInfo fetch(String gitUser, String projectId);
}
