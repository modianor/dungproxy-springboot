package com.virjar.dungproxy.server.service.impl;

import com.virjar.dungproxy.server.entity.Source;
import com.virjar.dungproxy.server.repository.SourceRepository;
import com.virjar.dungproxy.server.service.SourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SourceServiceImpl implements SourceService {
    @Autowired
    private SourceRepository sourceRepository;

    @Override
    public List<Source> selectAllSource() {
        List<Source> sources = sourceRepository.selectAllSource();
        return sources;
    }
}