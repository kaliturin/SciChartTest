package com.kaliturin.scicharttest;

import android.support.annotation.Nullable;

/**
 * Rate services factory interface
 */
interface IRateServiceFactory {
    @Nullable
    Class<? extends RateService> getService();
}
