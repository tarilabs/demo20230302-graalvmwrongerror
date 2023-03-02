package org.acme.demo20230302.module3;

import org.acme.demo20230302.module1.MyClass1;

public class App3 {
    public static void main(String[] args) {
        if (args.length == 0) {
            org.acme.demo20230302.module2.App2.main(args);
        } else {
            System.out.println(asd());
        }
    }

    public static String asd() {
        return new MyClass1().b().a();
    }
}
