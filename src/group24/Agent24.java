package group24;

import genius.core.AgentID;
import genius.core.actions.*;
import genius.core.bidding.BidDetails;
import genius.core.boaframework.SortedOutcomeSpace;
import genius.core.parties.NegotiationInfo;
import genius.core.timeline.DiscreteTimeline;
import group24.acceptance.impl.DefaultAcceptanceStrategyImpl;
import group24.offering.impl.DefaultOfferingStrategyBasedOpponentImpl;
import group24.opponentmodel.core.OpponentModelManager;
import group24.usermodel.impl.UserModelInterfaceImpl;

import java.util.*;
import java.util.List;

/**
 * The agent for group 24.
 * opponent model : Jonny Black
 * user model : lab 4
 *
 * life cycle: init() -> receiveMessage() -> chooseAction()
 * @author Fei Pan
 */
public class Agent24 extends CourseworkNegotiationParty {

    /**
     * Initializes a new instance of the agent.
     */
    @Override
    public void init(NegotiationInfo info)
    {
        super.init(info);

        // initializes User Model and Opponent Model
        userModelInterface = new UserModelInterfaceImpl(this);
        utilitySpace = userModelInterface.estimateUtilitySpace();

        // search all outcomes and sort
        sortedOutcomeSpace = new SortedOutcomeSpace(utilitySpace);
        feasibleBids = sortedOutcomeSpace.getAllOutcomes();

        // initializes acceptance and offering strategy
        acceptanceStrategy = new DefaultAcceptanceStrategyImpl(this);
        offeringStrategy = new DefaultOfferingStrategyBasedOpponentImpl(this);
    }

    /**
     * Makes a random offer above the minimum utility target
     * Accepts everything above the reservation value at the very end of the negotiation; or breaks off otherwise.
     */
    @Override
    public Action chooseAction(List<Class<? extends Action>> possibleActions)
    {
        // if receive a offer from opponent
        if (null != lastOffer) {

            // check whether choose acceptance
            Action action = acceptanceStrategy.handleOfferFromOpponent(lastOffer);

            if (null != action) {
                return action;
            }
        }

        // if we offer first or we can not accept offer from opponent, deliver to offering strategy
        return offeringStrategy.offer(opponentModelInterface);
    }

    /**
     * Remembers the offers received by the opponent.
     */
    @Override
    public void receiveMessage(AgentID sender, Action action)
    {
        super.receiveMessage(sender, action);

        // update the value of Care_Value、Reluctance、Agreement_Value every ten rounds
        if (getTimeLine() instanceof DiscreteTimeline) {
            DiscreteTimeline discreteTimeline = (DiscreteTimeline)getTimeLine();
            int cRound = discreteTimeline.getRound();
            if (cRound % 10 == 0) {

                // care_value increase 4% every ten rounds
                Care_Value = Care_Value * (1 + 0.04);
                if (Care_Value > 0.7) {
                    Care_Value = 0.7;
                }

                // reluctance decrease 4% every ten rounds
                Reluctance = Reluctance * (1 - 0.06);
                if (timeline.getTime() < 7 && Reluctance < 0.9) {
                    Reluctance = 0.9;
                }

                List<BidDetails> allBids = new ArrayList<>(feasibleBids);
                // find a common set for all opponent
                Set<BidDetails> commonBids = opponentModelInterface.queryCommonBestBidsSet(allBids, Number_of_Bids);
                List<BidDetails> commonBidsList = new ArrayList<>(commonBids);
                // sort the common set
                Collections.sort(commonBidsList);
                // AV = Utility(BidBest) * Reluctance
                Agreement_Value = (commonBidsList.get(commonBidsList.size() - 1).getMyUndiscountedUtil()) * Reluctance;
                if (Agreement_Value > 0.85) {
                    Agreement_Value = 0.85;
                }

                if (Agreement_Value < Minimum_Offer_Threshold * (2 - timeline.getTime())) {
                    Agreement_Value = Minimum_Offer_Threshold * (2 - timeline.getTime());
                }
            }
        }

        if (action instanceof Inform) {
            // receive message first time, update the number of opponent
            opponentModelInterface = new OpponentModelManager(getNumberOfParties());
        }else if (action instanceof Offer) {
            // save the last offer
            lastOffer = ((Offer) action).getBid();
            // update opponent model by new bid from opponent
            opponentModelInterface.handleBidFromSender(sender.toString(), lastOffer, getDomain());
        }
    }

    @Override
    public String getDescription()
    {
        return "coursework agent" ;
    }
}