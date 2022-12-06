package com.virjar.dungproxy.server.repository;

import com.virjar.dungproxy.server.entity.Source;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SourceRepository {
    List<Source> selectAllSource();
}