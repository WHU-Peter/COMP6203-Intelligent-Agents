package group24.offering.impl;

import genius.core.Bid;
import genius.core.actions.Action;
import genius.core.actions.Offer;
import genius.core.bidding.BidDetails;
import genius.core.uncertainty.ExperimentalUserModel;
import group24.CourseworkNegotiationParty;
import group24.offering.OfferingStrategy;
import group24.opponentmodel.OpponentModelInterface;

import java.util.List;

public class DefaultOfferingStrategyBasedOpponentImpl extends OfferingStrategy {

    private CourseworkNegotiationParty agent;
    private List<BidDetails> feasibleBids;

    private int lastBid;
    private int agentToFavor;

    public DefaultOfferingStrategyBasedOpponentImpl(CourseworkNegotiationParty agent) {
        this.agent = agent;
        this.feasibleBids = agent.getFeasibleBids();
    }


    /**
     * Traverse all feasibleBids
     * find the first that both the utility of bid is more than agreement_value and the predict utility of opponent is more than care_value
     * otherwise, return the first of all feasibleBids
     */
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
                        && (agent.getTimeLine().getTime() >= 0.1
                        && opponentModelInterface.predict(bid, opponentModelInterface.getOpponent(realOpponentIndex)) >= agent.getCare_Value())) {
                    lastBid = i;
                    agentToFavor++;
                    if (agent.hasPreferenceUncertainty() && agent.getUserModel() instanceof ExperimentalUserModel) {
                        System.out.println("realUSpace : " + agent.getRealUSpace().getUtility(bid));
                        System.out.println("estimation : " + agent.getUtility(bid));
                    }
                    return new Offer(agent.getPartyId(), bid);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                break;
            }
        }

        agentToFavor++;
        lastBid = 0;
        if (agent.hasPreferenceUncertainty() && agent.getUserModel() instanceof ExperimentalUserModel) {
            System.out.println("realUSpace : " + agent.getRealUSpace().getUtility(feasibleBids.get(0).getBid()));
            System.out.println("estimation : " + agent.getUtility(feasibleBids.get(0).getBid()));
        }
        return new Offer(agent.getPartyId(), feasibleBids.get(0).getBid());
    }
}
