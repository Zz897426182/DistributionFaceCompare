package com.hzgc.compare.demo;

import java.util.LinkedList;
import java.util.List;

public class Test2 {
    public static void main(String[] args) throws InterruptedException {
        LinkedList<String> list = new LinkedList<>();
        list.add("1");
        list.add("2");
        list.add("3");
        list.add("4");
        list.add("5");
        list.add("6");
        List<String> pp = list.subList(0, 3);
        System.out.println(pp);
//        System.out.println(pp);
//        System.out.println(list);
        for (int i = 0; i < 3; i++) {
            list.remove(0);
        }
        System.out.println(list);
    }
}
