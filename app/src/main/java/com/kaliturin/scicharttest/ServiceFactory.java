package com.kaliturin.scicharttest;

import android.app.Service;
import android.support.annotation.Nullable;

/**
 * Service factory
 */
class ServiceFactory implements IServiceFactory {
    @Nullable
    public Class<? extends Service> getServiceClass() {
        // returning random rates-service
        return RandomRateService.class;
    }
}
