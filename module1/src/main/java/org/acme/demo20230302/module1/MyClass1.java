package org.acme.demo20230302.module1;

public class MyClass1 implements TraitA {

    public MyClass1() {
        //empty
    }

    @Override
    public String a() {
        return "Hello, World";
    }

    public TraitA b() {
        return new MySubClass1();
    }

    private static class MySubClass1 implements TraitA {

        MyClass2 field2;

        @Override
        public String a() {
            field2 = new MyClass2();
            return field2.x() + "Hello, World from MySubClass1";
        }
    }
}
