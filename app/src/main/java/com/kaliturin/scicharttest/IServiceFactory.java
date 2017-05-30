package com.kaliturin.scicharttest;

import android.app.Service;
import android.support.annotation.Nullable;

/**
 * Service factory interface
 */
interface IServiceFactory {
    @Nullable
    Class<? extends Service> getServiceClass();
}
