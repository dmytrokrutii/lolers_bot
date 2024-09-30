package org.lolers;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.lolers.infrastructure.LolersApi;
import org.lolers.infrastructure.config.AppModule;

public class Main {

    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new AppModule());
        injector.getInstance(LolersApi.class).start();
    }
}