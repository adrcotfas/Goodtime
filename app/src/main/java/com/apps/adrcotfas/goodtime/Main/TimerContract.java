package com.apps.adrcotfas.goodtime.Main;

public interface TimerContract {

    interface View {
        void start();
        void stop();
        void toggle();
        void skip();
        void updateTime(Long seconds);
    }

    interface Presenter {
        void start();
        void stop();
        void toggle();
        void skip();
    }
}
