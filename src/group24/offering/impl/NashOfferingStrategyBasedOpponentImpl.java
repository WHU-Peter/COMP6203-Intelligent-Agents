package group24.offering.impl;

import genius.core.Bid;
import genius.core.actions.Action;
import genius.core.actions.Offer;
import genius.core.bidding.BidDetails;
import genius.core.uncertainty.ExperimentalUserModel;
import group24.CourseworkNegotiationParty;
import group24.offering.OfferingStrategy;
import group24.opponentmodel.OpponentModelInterface;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class NashOfferingStrategyBasedOpponentImpl extends OfferingStrategy {

    private CourseworkNegotiationParty agent;
    private List<BidDetails> feasibleBids;

    private int lastBid;
    private int agentToFavor;

    public NashOfferingStrategyBasedOpponentImpl(CourseworkNegotiationParty agent) {
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

        if (agent.getTimeLine().getTime() <= 0.3) {
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
        }else {

            Bid nashPoint = feasibleBids.get(0).getBid();
            List<Bid> possibleBidDetails = new ArrayList<>();

            for (BidDetails bidDetails : feasibleBids) {
                if (agent.getUtility(bidDetails.getBid()) >= agent.getAgreement_Value()
                        && opponentModelInterface.predict(bidDetails.getBid()) >= agent.getCare_Value()) {
                    possibleBidDetails.add(bidDetails.getBid());

                    if (null == nashPoint
                            || calculateNash(nashPoint, opponentModelInterface.calculateNash(nashPoint)) < calculateNash(bidDetails.getBid(), opponentModelInterface.calculateNash(bidDetails.getBid()))) {
                        nashPoint = bidDetails.getBid();
                    }
                }
            }

            final Bid finalNash = nashPoint;

            if (null != possibleBidDetails && possibleBidDetails.size() > 0) {
                Collections.sort(possibleBidDetails, new Comparator<Bid>() {
                    @Override
                    public int compare(Bid o1, Bid o2) {
                        return Double.compare(Math.sqrt(calculateDistanceNash(o1, finalNash) + opponentModelInterface.calculateDistanceNash(o1, finalNash)), Math.sqrt(calculateDistanceNash(o2, finalNash) + opponentModelInterface.calculateDistanceNash(o2, finalNash)));
                    }
                });

                List<Bid> nashBidDetails = possibleBidDetails.subList(0, possibleBidDetails.size() < 20 ? possibleBidDetails.size() : 20);
                return new Offer(agent.getPartyId(), nashBidDetails.get(agent.getRandom().nextInt(nashBidDetails.size())));
            }

            return new Offer(agent.getPartyId(), nashPoint);
        }
    }

    private double calculateNash(Bid bid, double opponentUtilities) {
        return agent.getUtility(bid) * opponentUtilities;
    }

    private double calculateDistanceNash(Bid bid, Bid nash) {
        return Math.pow(agent.getUtility(bid) - agent.getUtility(nash), 2);
    }
}
