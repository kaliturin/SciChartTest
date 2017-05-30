package com.kaliturin.scicharttest;

import android.app.Service;

/**
 * Rates data service
 */
abstract class RateService extends Service {
    public static final String RECEIVER = "RECEIVER";
    public static final String DATE = "DATE";
    public static final String RATE = "RATE";
    public static final String TIME_INTERVAL = "TIME_INTERVAL";
}
