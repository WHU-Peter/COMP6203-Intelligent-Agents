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
    private BidDetails finalNash;

    public BidDetails getFinalNash() {
        return finalNash;
    }

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

            BidDetails nashPoint = null;
            List<BidDetails> possibleBidDetails = new ArrayList<>();

            for (BidDetails bidDetails : feasibleBids) {
                if (bidDetails.getMyUndiscountedUtil() >= agent.getAgreement_Value()
                        && opponentModelInterface.predict(bidDetails.getBid()) >= agent.getCare_Value()) {
                    possibleBidDetails.add(bidDetails);

                    System.out.println("nashPoint : " + (null != nashPoint ? calculateNash(nashPoint.getBid(), opponentModelInterface.calculateNash(nashPoint.getBid())): null));
                    System.out.println("bidDetails.getBid() : " + calculateNash(bidDetails.getBid(), opponentModelInterface.calculateNash(bidDetails.getBid())));

                    if (null == nashPoint
                            || calculateNash(nashPoint.getBid(), opponentModelInterface.calculateNash(nashPoint.getBid())) < calculateNash(bidDetails.getBid(), opponentModelInterface.calculateNash(bidDetails.getBid()))) {
                        nashPoint = bidDetails;
                    }
                }
            }

            if (null == nashPoint) {
                nashPoint = feasibleBids.get(0);
            }
            finalNash = nashPoint;
            System.out.println("finalNash : " + agent.getUtility(nashPoint.getBid()) + "  ,   " + opponentModelInterface.predict(nashPoint.getBid()));

            if (null != possibleBidDetails && possibleBidDetails.size() > 0) {
                Collections.sort(possibleBidDetails, new Comparator<BidDetails>() {
                    @Override
                    public int compare(BidDetails o1, BidDetails o2) {
                        return Double.compare(calculateScores(o2, finalNash.getBid(), opponentModelInterface),calculateScores(o1, finalNash.getBid(), opponentModelInterface));
                    }
                });

                List<BidDetails> nashBidDetails = possibleBidDetails.subList(0, possibleBidDetails.size() < 5 ? possibleBidDetails.size() : 5);
                return new Offer(agent.getPartyId(), nashBidDetails.get(agent.getRandom().nextInt(nashBidDetails.size())).getBid());
            }

            return new Offer(agent.getPartyId(), nashPoint.getBid());
        }
    }

    private double calculateScores(BidDetails o, Bid finalNash, OpponentModelInterface opponentModelInterface) {

        return (1 - Math.sqrt(calculateDistanceNash(o.getBid(), finalNash) + opponentModelInterface.calculateDistanceNash(o.getBid(), finalNash))) * agent.getUtility(o.getBid());
    }

    private double calculateNash(Bid bid, double opponentUtilities) {
        return agent.getUtility(bid) * opponentUtilities;
    }

    private double calculateDistanceNash(Bid bid, Bid nash) {
        return Math.pow(agent.getUtility(bid) - agent.getUtility(nash), 2);
    }
}
