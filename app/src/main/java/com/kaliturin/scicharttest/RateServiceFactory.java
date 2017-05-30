package com.kaliturin.scicharttest;

import android.support.annotation.Nullable;

/**
 * Rate services factory
 */
class RateServiceFactory implements IRateServiceFactory {
    @Nullable
    public Class<? extends RateService> getService() {
        // returning random rates-service
        return RandomRateService.class;
    }
}
