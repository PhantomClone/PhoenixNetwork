/*
 * Copyright (c) 2021 by PhantomClone.me to present. All rights reserved.
 */

package me.phantomclone.phoenixnetwork.backendcore.database.mongodb.result;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.function.Consumer;

/**
 * @author PhantomClone
 */
public class UpdateResultSubscriber implements Subscriber<com.mongodb.client.result.UpdateResult> {

    private int requests = 1;
    private Consumer<Throwable> throwableConsumer;
    private Consumer<Boolean> completeConsumer;
    private boolean filterMatched = false;

    public UpdateResultSubscriber() {}

    public UpdateResultSubscriber(Consumer<Boolean> completeConsumer) {
        this.completeConsumer = completeConsumer;
    }

    public UpdateResultSubscriber(Consumer<Boolean> completeConsumer, Consumer<Throwable> throwableConsumer) {
        this.completeConsumer = completeConsumer;
        this.throwableConsumer = throwableConsumer;
    }

    @Override
    public void onSubscribe(Subscription s) {
        s.request(requests);
    }

    @Override
    public void onNext(com.mongodb.client.result.UpdateResult updateResult) {
        this.filterMatched = updateResult.getMatchedCount() > 0;
    }

    @Override
    public void onError(Throwable throwable) {
        if (this.throwableConsumer != null) {
            this.throwableConsumer.accept(throwable);
        }
    }

    @Override
    public void onComplete() {
        this.completeConsumer.accept(this.filterMatched);
    }

    public UpdateResultSubscriber setRequests(int requests) {
        this.requests = requests;
        return this;
    }

    public UpdateResultSubscriber setThrowableConsumer(Consumer<Throwable> throwableConsumer) {
        this.throwableConsumer = throwableConsumer;
        return this;
    }

    public UpdateResultSubscriber setCompleteConsumer(Consumer<Boolean> completeConsumer) {
        this.completeConsumer = completeConsumer;
        return this;
    }
}
