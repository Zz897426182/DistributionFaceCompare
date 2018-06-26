package com.hzgc.compare.worker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Test {
    Logger logger = LoggerFactory.getLogger(Test.class);
    public void print() {
        logger.info("abc:{}", "123");
        logger.debug("efg:{}", "456");
    }
    public static void main(String[] args) throws IllegalAccessException, InstantiationException {
        Test test = Test.class.newInstance();
        test.print();
    }
}
