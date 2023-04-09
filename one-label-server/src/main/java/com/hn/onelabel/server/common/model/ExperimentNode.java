package com.hn.onelabel.server.common.model;

import lombok.Data;

@Data
public class ExperimentNode implements Comparable<ExperimentNode> {
    private String experimentId;
    private Integer weight;
    private Integer effectiveWeight;
    private Integer currentWeight;

    public ExperimentNode(String experimentId,Integer weight){
        this.experimentId = experimentId;
        this.weight = weight;
        this.effectiveWeight = weight;
        this.currentWeight = weight;
    }

    public ExperimentNode(String experimentId, Integer weight, Integer effectiveWeight, Integer currentWeight) {
        this.experimentId = experimentId;
        this.weight = weight;
        this.effectiveWeight = effectiveWeight;
        this.currentWeight = currentWeight;
    }

    @Override
    public int compareTo(ExperimentNode node) {
        return currentWeight > node.currentWeight ? 1 : (currentWeight.equals(node.currentWeight) ? 0 : -1);
    }
}
