package com.chriniko.jsonmerger.sample;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

public class Bootstrap {

    public static void main(String[] args) throws Exception {

        Injector injector = Guice.createInjector(new BasicModule());

        // Note: playground...
        TypeConverter typeConverter = injector.getInstance(TypeConverter.class);

        StateMerger stateMerger = injector.getInstance(StateMerger.class);
        System.out.println(stateMerger);

        stateMerger = injector.getInstance(StateMerger.class);
        System.out.println(stateMerger);

        stateMerger = injector.getInstance(StateMerger.class);
        System.out.println(stateMerger);


        JsonNode jsonNode = typeConverter.toJsonNode(new Dog("name", 12, new Color("brown")));
        Set<String> allJsonFields = stateMerger.extractAllFields(jsonNode);
        System.out.println(allJsonFields);


        Thread.sleep(10_000);
    }

    public static class BasicModule implements Module {

        @Override
        public void configure(Binder binder) {
            binder.bind(TypeConverter.class).asEagerSingleton();
            binder.bind(StateMerger.class).asEagerSingleton();
        }
    }


    // --- sample ---
    @Data
    @AllArgsConstructor
    public static class Dog {
        private String name;
        private int age;
        private Color color;
    }

    @Data
    @AllArgsConstructor
    public static class Color {
        private String color;
    }
}
