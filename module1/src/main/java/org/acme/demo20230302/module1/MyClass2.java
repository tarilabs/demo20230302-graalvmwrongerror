package org.acme.demo20230302.module1;

public class MyClass2 implements TraitB {

    @Override
    public String x() {
        return Utils.hello() + "World from MyClass2";
    }

}
