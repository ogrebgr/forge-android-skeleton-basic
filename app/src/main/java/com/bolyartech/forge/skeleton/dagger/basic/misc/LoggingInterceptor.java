package com.bolyartech.forge.skeleton.dagger.basic.misc;

import org.slf4j.LoggerFactory;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;


public class LoggingInterceptor implements Interceptor {
    private final org.slf4j.Logger mLogger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    @Override public Response intercept(Interceptor.Chain chain) throws IOException {
        Request request = chain.request();

        long t1 = System.nanoTime();
        mLogger.info(String.format("Sending request %s on %s%n%s",
                request.url(), chain.connection(), request.headers()));

        try {
            Response response = chain.proceed(request);
            long t2 = System.nanoTime();
            mLogger.info(String.format("Received response for %s in %.1fms%n%s",
                    response.request().url(), (t2 - t1) / 1e6d, response.headers()));

            return response;

        } catch (Exception e) {
            mLogger.error("Problem executing HTTP request {}", e);
            throw new RuntimeException("Problem executing HTTP request");
        }

    }
}