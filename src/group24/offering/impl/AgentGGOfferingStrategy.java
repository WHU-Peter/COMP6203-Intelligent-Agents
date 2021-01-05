package group24.offering.impl;

import genius.core.Bid;
import genius.core.actions.Action;
import genius.core.actions.Offer;
import genius.core.bidding.BidDetails;
import genius.core.issue.Issue;
import genius.core.issue.IssueDiscrete;
import genius.core.issue.Value;
import genius.core.issue.ValueDiscrete;
import genius.core.utility.AdditiveUtilitySpace;
import genius.core.utility.EvaluatorDiscrete;
import group24.CourseworkNegotiationParty;
import group24.offering.OfferingStrategy;
import group24.opponentmodel.OpponentModelInterface;

import java.util.*;

public class AgentGGOfferingStrategy extends OfferingStrategy {

    private CourseworkNegotiationParty agent;
    private List<BidDetails> feasibleBids;

    private Map<Issue, List<ValueDiscrete>> frequencyTable = new HashMap<>();

    private int lastBid;
    private int agentToFavor;

    public AgentGGOfferingStrategy(CourseworkNegotiationParty agent) {
        this.agent = agent;
        this.feasibleBids = agent.getFeasibleBids();

        List<Issue> issueList = agent.getDomain().getIssues();
        if (null == issueList || issueList.isEmpty()) {
            return;
        }

        for (Issue issue : issueList) {
            List<ValueDiscrete> valueList = ((IssueDiscrete)issue).getValues();

            EvaluatorDiscrete evaluator = (EvaluatorDiscrete) ((AdditiveUtilitySpace)agent.getUtilitySpace()).getEvaluator(issue);
            List<ValueDiscrete> valueListCopy = new ArrayList<>();
            for (ValueDiscrete valueDiscrete : valueList) {

                double weight = evaluator.getDoubleValue(valueDiscrete);

                for (int i = 0; i < (int) weight; i ++) {
                    valueListCopy.add(valueDiscrete);
                }
            }
            Collections.shuffle(valueListCopy);
            frequencyTable.put(issue, valueListCopy);
        }
        System.out.println();
    }

    @Override
    public Action offer(OpponentModelInterface opponentModelInterface) {

        for (int i = lastBid + 1; i < feasibleBids.size(); i++) {
            if (opponentModelInterface.getOpponentsCounts() == 0) {
                break;
            }
            int realOpponentIndex = agentToFavor % opponentModelInterface.getOpponentsCounts();
            Bid bid = feasibleBids.get(i).getBid();

            try {
                if (agent.getUtility(bid) >= agent.getAgreement_Value()
                        && (agent.getTimeLine().getTime() >= 1
                        && opponentModelInterface.predict(bid, opponentModelInterface.getOpponent(realOpponentIndex)) >= agent.getCare_Value())) {
                    lastBid = i;
                    agentToFavor++;
                    System.out.println("bid offer, self utility : " + agent.getUtility(bid));
                    System.out.println("bid offer, opponent utility : " + opponentModelInterface.predict(bid));
                    System.out.println();
                    return new Offer(agent.getPartyId(), bid);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                break;
            }
        }

        agentToFavor++;
        lastBid = 0;

        Bid offer;
        int i = 0;
        do {
            HashMap<Integer, Value> values = new HashMap<Integer, Value>();

            // For each issue, put a random value
            for (Issue currentIssue : agent.getDomain().getIssues()) {
                values.put(currentIssue.getNumber(), frequencyTable.get(currentIssue).get(agent.getRandom().nextInt(frequencyTable.get(currentIssue).size())));
            }
            offer = new Bid(agent.getDomain(), values);
            i ++;
        }while (agent.getUtility(offer) < 0.8 && i < 10);

        if (agent.getUtility(offer) < 0.8) {
            offer = feasibleBids.get(0).getBid();
        }

        System.out.println("bid offer, self utility : " + agent.getUtility(offer));
        System.out.println("bid offer, opponent utility : " + opponentModelInterface.predict(offer));
        System.out.println();
        return new Offer(agent.getPartyId(), offer);
    }
}
