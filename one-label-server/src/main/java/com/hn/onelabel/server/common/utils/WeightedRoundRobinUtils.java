package com.hn.onelabel.server.common.utils;

import com.hn.onelabel.server.common.model.ExperimentNode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class WeightedRoundRobinUtils {

    @Getter
    private final List<ExperimentNode> experimentNodes;
    private Integer totalWeight = 0;

    public WeightedRoundRobinUtils(List<ExperimentNode> experimentNodes) {
        this.experimentNodes = experimentNodes;
        experimentNodes.forEach(node -> this.totalWeight += node.getEffectiveWeight());
    }

    public ExperimentNode selectNode(){
        if (CollectionUtils.isEmpty(experimentNodes)) {
            return null;
        }
        if (experimentNodes.size() == 1) {
            return experimentNodes.get(0);
        }

        ExperimentNode nodeOfMaxWeight;
        synchronized (experimentNodes) {
            StringBuilder sb = new StringBuilder();
            sb.append(Thread.currentThread().getName()).append("#currentWeight:").append(printCurrentWeight(experimentNodes));

            ExperimentNode tempNodeOfMaxWeight = null;
            for (ExperimentNode node : experimentNodes) {
                if (tempNodeOfMaxWeight == null) {
                    tempNodeOfMaxWeight = node;
                } else {
                    tempNodeOfMaxWeight = tempNodeOfMaxWeight.compareTo(node) > 0 ? tempNodeOfMaxWeight : node;
                }
            }
            assert tempNodeOfMaxWeight != null;
            nodeOfMaxWeight = new ExperimentNode(tempNodeOfMaxWeight.getExperimentId(), tempNodeOfMaxWeight.getWeight(), tempNodeOfMaxWeight.getEffectiveWeight(), tempNodeOfMaxWeight.getCurrentWeight());

            tempNodeOfMaxWeight.setCurrentWeight(tempNodeOfMaxWeight.getCurrentWeight() - totalWeight);
            sb.append(" -> ").append(printCurrentWeight(experimentNodes));

            experimentNodes.forEach(node -> node.setCurrentWeight(node.getCurrentWeight()+node.getEffectiveWeight()));

            sb.append(" -> ").append(printCurrentWeight(experimentNodes));
            log.info("[WeightedRoundRobinUtils] - selectNode | {}", sb.toString());
            System.out.println("[WeightedRoundRobinUtils] - selectNode | " + sb.toString());
        }

        return nodeOfMaxWeight;
    }

    private String printCurrentWeight(List<ExperimentNode> nodes){
        StringBuffer stringBuffer = new StringBuffer("[");
        nodes.forEach(node -> stringBuffer.append(node.getCurrentWeight()).append(","));
        return stringBuffer.substring(0, stringBuffer.length() - 1) + "]";
    }

    public static void main(String[] args) {
        List<ExperimentNode> experimentNodes = new ArrayList<>();
        experimentNodes.add(new ExperimentNode("1", 20));
        experimentNodes.add(new ExperimentNode("2", 30));
        experimentNodes.add(new ExperimentNode("3", 50));
        WeightedRoundRobinUtils weightedRoundRobinUtils = new WeightedRoundRobinUtils(experimentNodes);
        for (int i=0; i<10; i++) {
            weightedRoundRobinUtils.selectNode();
        }
    }

}