package com.cariochi.recordo.interceptor;

import uk.co.jemos.podam.api.AbstractRandomDataProviderStrategy;

class RandomDataProviderStrategy extends AbstractRandomDataProviderStrategy {
    public RandomDataProviderStrategy() {
        super(2);
        setMaxDepth(3);
    }
}
