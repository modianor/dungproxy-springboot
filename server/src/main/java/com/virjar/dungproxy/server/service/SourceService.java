package com.virjar.dungproxy.server.service;

import com.virjar.dungproxy.server.entity.Source;

import java.util.List;

public interface SourceService {
    List<Source> selectAllSource();
}