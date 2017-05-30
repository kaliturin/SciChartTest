package com.kaliturin.scicharttest;

import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Random rates generation service
 */
public class RandomRateService extends RateService {
    private final ScheduledExecutorService scheduledExecutorService =
            Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> schedule = null;
    private final Random random = new Random();
    private long date = System.currentTimeMillis();
    private double rate = 1000;
    private ResultReceiver receiver = null;
    private long timeInterval = 0;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        if (schedule != null) {
            schedule.cancel(true);
        }
        scheduledExecutorService.shutdown();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        receiver = intent.getParcelableExtra(RECEIVER);
        timeInterval = intent.getLongExtra(TIME_INTERVAL, 0);

        if (receiver != null && timeInterval > 0) {
            // run rates generation
            schedule = scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    if (receiver != null) {
                        Point point = getRandomPoint();
                        Bundle bundle = new Bundle();
                        bundle.putLong(DATE, point.date);
                        bundle.putDouble(RATE, point.rate);
                        receiver.send(0, bundle);
                    }
                }
            }, 0, timeInterval, TimeUnit.MILLISECONDS);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    private Point getRandomPoint() {
        date += timeInterval;
        rate += random.nextDouble() * (random.nextBoolean() ? -1 : 1);
        if (rate < 0) {
            rate = 0;
        }
        return new Point(date, rate);
    }
}
