package com.virjar.dungproxy.client.ippool.strategy.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.virjar.dungproxy.client.ippool.strategy.Scoring;
import com.virjar.dungproxy.client.model.AvProxy;

/**
 * Created by virjar on 16/11/16.
 */
public class DefaultScoring implements Scoring {
    private static final Logger logger = LoggerFactory.getLogger(DefaultScoring.class);

    @Override
    public double newAvgScore(AvProxy score, int factory, boolean isSuccess) {
        if (score.getAvgScore() == 0D && isSuccess) {
            return 1D;
        }
        double newScore = isSuccess ? 1D : 0D;
        return (score.getAvgScore() * (factory - 1) + newScore) / factory;
    }
}
