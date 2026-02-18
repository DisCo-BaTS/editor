package modelspaceinterface.misc;

import java.util.ArrayList;

public class Test {

    public static void main(String[] args) {
        ArrayList test;
        test = new ArrayList<B>();

        A a = new A();
        a.test(test);
    }

    static class A {

        public void test(ArrayList test) {
            System.out.println("test");
        }
    }

    static class B extends A {

    }
}
