package group24.usermodel.impl;

import genius.core.bidding.BidDetails;
import genius.core.boaframework.OutcomeSpace;
import genius.core.uncertainty.ExperimentalUserModel;
import genius.core.uncertainty.User;
import genius.core.utility.AbstractUtilitySpace;
import genius.core.utility.UncertainAdditiveUtilitySpace;
import group24.CourseworkNegotiationParty;
import group24.usermodel.UserModelInterface;

import java.lang.reflect.Field;
import java.util.List;

public class UserModelInterfaceImpl implements UserModelInterface {

    private CourseworkNegotiationParty agent;

    private UncertainAdditiveUtilitySpace realUSpace;

    public UserModelInterfaceImpl(CourseworkNegotiationParty agent){
        this.agent = agent;
    }

    /**
     * update Utility Space by all outcomes
     * @return
     */
    @Override
    public AbstractUtilitySpace estimateUtilitySpace() {
        if (agent.hasPreferenceUncertainty()) {
            if (agent.getUserModel() instanceof ExperimentalUserModel) {
                ExperimentalUserModel e = (ExperimentalUserModel) agent.getUserModel();
                realUSpace = e.getRealUtilitySpace();
            }

            OutcomeSpace outcomeSpace = new OutcomeSpace(agent.getUtilitySpace());
            List<BidDetails> allOutcomes = outcomeSpace.getAllOutcomes();

            for (BidDetails bid : allOutcomes) {
                agent.setUserModel(agent.getUser().elicitRank(bid.getBid(), agent.getUserModel()));
            }

            try {
                User user = agent.getUser();
                Field f = User.class.getDeclaredField("elicitationBother");
                f.setAccessible(true);
                f.set(user, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                User user = agent.getUser();
                Field f = User.class.getDeclaredField("utilspace");
                f.setAccessible(true);
                return (AbstractUtilitySpace)f.get(user);
            } catch (Exception e) {
                e.printStackTrace();
                return agent.estimateUtilitySpace();
            }
        }
        return agent.getUtilitySpace();
    }

    @Override
    public UncertainAdditiveUtilitySpace queryRealUtilitySpace() {
        return realUSpace;
    }
}
