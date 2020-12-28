package group24.opponentmodel.core.impl;

import genius.core.Bid;
import genius.core.Domain;
import genius.core.issue.*;
import genius.core.utility.Evaluator;
import genius.core.utility.EvaluatorDiscrete;
import group24.opponentmodel.core.OpponentModel;

import java.util.*;

/**
 * opponent model entity implement by Jonny Black algorithm
 */
public class JonnyBlackOpponentModel extends OpponentModel {

    private Map<Issue, Map<Value, Integer>> frequencyTable = new HashMap<>();

    public JonnyBlackOpponentModel(Domain domain) {

        super(domain);

        List<Issue> issueList = domain.getIssues();
        if (null == issueList || issueList.isEmpty()) {
            return;
        }

        for (Issue issue : issueList) {
            HashMap<Value, Integer> valueFrequencyMap = new HashMap<>();
            List<ValueDiscrete> valueList = ((IssueDiscrete)issue).getValues();
            if (null == valueList || valueList.isEmpty()) {
                continue;
            }

            for (ValueDiscrete valueDiscrete : valueList) {
                valueFrequencyMap.put(valueDiscrete, 0);
            }

            frequencyTable.put(issue, valueFrequencyMap);
        }
    }

    @Override
    public void handleBidInternal(Bid bid) {
        bidHistory.add(bid);
        List<Issue> issueList = bid.getIssues();
        if (null == issueList || issueList.isEmpty()) {
            return;
        }
        for (Issue issue : issueList) {
            Value value = bid.getValue(issue);
            Map<Value, Integer> valueFrequencyMap = frequencyTable.get(issue);
            Integer count = valueFrequencyMap.get(value);
            count ++;

            valueFrequencyMap.put(value, count);

            EvaluatorDiscrete evaluator = new EvaluatorDiscrete();
            List<Map.Entry<Value, Integer>> entrySortedList = new ArrayList(valueFrequencyMap.entrySet());
            Collections.sort(entrySortedList, new Comparator<Map.Entry<Value, Integer>>() {
                @Override
                public int compare(Map.Entry<Value, Integer> o1, Map.Entry<Value, Integer> o2) {
                    return Integer.compare(o2.getValue(), o1.getValue());
                }
            });
            int totalCounts = 0;
            for (Map.Entry<Value, Integer> entry : entrySortedList) {
                totalCounts += entry.getValue();
            }
            double weight = 0;
            for (int i = 0; i < entrySortedList.size(); i++) {
                Map.Entry<Value, Integer> entry = entrySortedList.get(i);
                weight += Math.pow(entry.getValue()/(double)totalCounts, 2);
                double optionWeight = (entrySortedList.size() - i)/(double)entrySortedList.size();
                evaluator.setEvaluationDouble((ValueDiscrete)entry.getKey(), optionWeight);
            }

            evaluator.setWeight(weight);
            additiveUtilitySpace.addEvaluator(issue, evaluator);
        }

        double weightSum = 0;
        for (Map.Entry<Objective, Evaluator> fEvaluator : additiveUtilitySpace.getfEvaluators().entrySet()) {
            weightSum += fEvaluator.getValue().getWeight();
        }

        if (weightSum != 0) {
            for (Map.Entry<Objective, Evaluator> fEvaluator : additiveUtilitySpace.getfEvaluators().entrySet()) {
                fEvaluator.getValue().setWeight(fEvaluator.getValue().getWeight() / weightSum);
            }
        }
    }
}
