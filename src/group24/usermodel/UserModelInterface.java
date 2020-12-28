package group24.usermodel;

import genius.core.utility.AbstractUtilitySpace;
import genius.core.utility.UncertainAdditiveUtilitySpace;

public interface UserModelInterface {

    /**
     * get untility space after estimate
     * @return
     */
    AbstractUtilitySpace estimateUtilitySpace();

    /**
     * query real utility space, if exist
     * @return
     */
    UncertainAdditiveUtilitySpace queryRealUtilitySpace();
}
