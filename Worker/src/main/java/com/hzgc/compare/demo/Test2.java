package com.hzgc.compare.demo;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Test2 {
    public static void main(String[] args) throws InterruptedException {
        List<AAA> arr = new ArrayList<>();
        arr.add(new BBB());
        arr.add(new CCC());
        arr.add(new DDD());
        Class clazz = DDD.class;
        for(AAA data : arr){
            if(data.getClass() == clazz){
                data.aa();
            }
        }
    }

}

class AAA{
    public void aa(){
    }
}
class BBB extends AAA{
    public void aa(){
        System.out.println("bbb");
    }
}
class CCC extends AAA{
    public void aa(){
        System.out.println("ccc");
    }
}
class DDD extends AAA{
    public void aa(){
        System.out.println("ddd");
    }
}