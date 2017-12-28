package com.apps.adrcotfas.goodtimeplus.Main;

public class TimerPresenter implements TimerContract.Presenter{

    private TimerContract.View mView;

    public TimerPresenter(TimerContract.View view) {
        mView = view;
    }

    @Override
    public void start() {
        mView.start();
    }

    @Override
    public void stop() {
        mView.stop();
    }

    @Override
    public void toggle() {
        mView.toggle();
    }

    @Override
    public void skip() {
        mView.skip();
    }
}
