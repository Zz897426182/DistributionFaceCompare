package com.hzgc.compare.worker.common;

import javafx.beans.NamedArg;

import java.io.Serializable;

public class Triplet<A, B, C> implements Serializable{
    private A first;
    private B second;
    private C third;

    public Triplet(@NamedArg("first") A first, @NamedArg("second") B second, @NamedArg("third") C third) {
        this.first = first;
        this.second = second;
        this.third = third;
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

    @Override
    public int hashCode() {
        int result = first != null ? first.hashCode() : 0;
        result = 31 * result + (second != null ? second.hashCode() : 0);
        result = 31 * result + (third != null ? third.hashCode() : 0);
        return result;
    }

    public String toString() {
        return "(" + first + " , " + second + " , " + third + ")";
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof Triplet) {
            Triplet triplet = (Triplet) o;
            if (first != null ? !first.equals(triplet.first) : triplet.first != null) return false;
            if (second != null ? !second.equals(triplet.second) : triplet.second != null) return false;
            if (third != null ? !third.equals(triplet.third) : triplet.third != null) return false;
            return true;
        }
        return false;
    }
}