package com.hzgc.compare.worker.common;

import javafx.beans.NamedArg;

public class Quintuple<A, B, C, D ,E> {
    private A first;
    private B second;
    private C third;
    private D fourth;
    private E fifth;

    public Quintuple(@NamedArg("first") A first, @NamedArg("second") B second,
                     @NamedArg("third") C third, @NamedArg("fourth") D fourth, @NamedArg("fifth") E fifth) {
        this.first = first;
        this.second = second;
        this.third = third;
        this.fourth = fourth;
        this.fifth = fifth;
    }

    public A getFirst() {
        return first;
    }

    public B getSecond() {
        return second;
    }

    public C getThird() {
        return third;
    }
    public D getFourth(){return  fourth;}
    public E getFifth(){return  fifth;}


    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof Quintuple) {
            Quintuple triplet = (Quintuple) o;
            if (first != null ? !first.equals(triplet.first) : triplet.first != null) return false;
            if (second != null ? !second.equals(triplet.second) : triplet.second != null) return false;
            if (third != null ? !third.equals(triplet.third) : triplet.third != null) return false;
            if (fourth != null ? !fourth.equals(triplet.fourth) : triplet.fourth != null) return false;
            if (fifth != null ? !fifth.equals(triplet.fifth) : triplet.fifth != null) return false;
            return true;
        }
        return false;
    }
}
