package com.hzgc.compare.worker.common;

import javafx.beans.NamedArg;

import java.io.Serializable;

public class Quad<A, B, C, D> implements Serializable {
    private A first;
    private B second;
    private C third;
    private D fourth;

    public Quad(@NamedArg("first") A first, @NamedArg("second") B second, @NamedArg("third") C third, @NamedArg("fourth") D fourth) {
        this.first = first;
        this.second = second;
        this.third = third;
        this.fourth = fourth;
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

    public String toString() {
        return "(" + first + " , " + second + " , " + third + ")";
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof Quad) {
            Quad triplet = (Quad) o;
            if (first != null ? !first.equals(triplet.first) : triplet.first != null) return false;
            if (second != null ? !second.equals(triplet.second) : triplet.second != null) return false;
            if (third != null ? !third.equals(triplet.third) : triplet.third != null) return false;
            if (fourth != null ? !fourth.equals(triplet.fourth) : triplet.fourth != null) return false;
            return true;
        }
        return false;
    }
}