package group24.acceptance.impl;

import genius.core.Bid;
import genius.core.actions.Accept;
import genius.core.actions.Action;
import genius.core.actions.EndNegotiation;
import genius.core.uncertainty.ExperimentalUserModel;
import group24.CourseworkNegotiationParty;
import group24.acceptance.AcceptanceStrategy;

public class DefaultAcceptanceStrategyImpl extends AcceptanceStrategy {

    private CourseworkNegotiationParty agent;

    public DefaultAcceptanceStrategyImpl(CourseworkNegotiationParty agent) {
        this.agent = agent;
    }

    @Override
    public Action handleOfferFromOpponent(Bid lastOffer) {
        // Check for acceptance if we have received an offer
        if (lastOffer != null) {
            if (agent.getTimeLine().getTime() > 0.99) {
                if (agent.getUtility(lastOffer) >= agent.getUtilitySpace().getReservationValue()) {
                    if (agent.hasPreferenceUncertainty() && agent.getUserModel() instanceof ExperimentalUserModel) {
                        System.out.println("realUSpace : " + agent.getRealUSpace().getUtility(lastOffer));
                        System.out.println("estimation : " + agent.getUtility(lastOffer));
                    }
                    return new Accept(agent.getPartyId(), lastOffer);
                }else {
                    if (agent.hasPreferenceUncertainty() && agent.getUserModel() instanceof ExperimentalUserModel) {
                        System.out.println("realUSpace : " + agent.getRealUSpace().getUtility(lastOffer));
                        System.out.println("estimation : " + agent.getUtility(lastOffer));
                    }
                    return new EndNegotiation(agent.getPartyId());
                }

            }else if (agent.getUtility(lastOffer) >= agent.getAgreement_Value()) {
                if (agent.hasPreferenceUncertainty() && agent.getUserModel() instanceof ExperimentalUserModel) {
                    System.out.println("realUSpace : " + agent.getRealUSpace().getUtility(lastOffer));
                    System.out.println("estimation : " + agent.getUtility(lastOffer));
                }
                return new Accept(agent.getPartyId(), lastOffer);
            }
        }

        return null;
    }
}
