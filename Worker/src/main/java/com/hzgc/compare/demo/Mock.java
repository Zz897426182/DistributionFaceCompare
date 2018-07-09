package com.hzgc.compare.demo;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.ReentrantLock;

public class Mock {

    private Sync sync = new Sync();

    private class Sync extends AbstractQueuedSynchronizer {
        @Override
        protected boolean isHeldExclusively() {
            return getState() == 1;
        }

        @Override
        protected boolean tryAcquire(int acquires) {//重写tryAcquire方法，用cas尝试将state从0置为1
            if (compareAndSetState(0, 1)){
                setExclusiveOwnerThread(Thread.currentThread());//如果将state从0置为1成功，则将当前线程设置为独占模式持有资源的线程
//                System.out.println(Thread.currentThread().getName() + " this is true");
                return true;
            }
//            System.out.println(Thread.currentThread().getName() + " this is false");
            return false;
        }

        @Override
        protected boolean tryRelease(int releases) {
            assert releases == 1;
            if (getState() == 0){//如果state为0，则说明资源没有被占用，不需要释放 抛出一个异常
                throw new IllegalMonitorStateException();
            }
            setExclusiveOwnerThread(null);//将资源拥有者线程置为null
            setState(0);//将state置为0，资源释放
            return true;
        }
    }

    public void lock(){
        sync.acquire(1);
    }
    public void lock(long timeUnit) throws InterruptedException {
        tryAcquire(timeUnit);
    }
    public boolean tryAcquire(){
        return sync.tryAcquire(1);
    }

    public boolean tryAcquire(long timeUnit) throws InterruptedException {
        return sync.tryAcquireNanos(1, timeUnit);
    }


    private void unlock(){
        sync.release(1);
    }

    public static void main(String [] args) throws InterruptedException {
        final Mock mock = new Mock();
//        final CountDownLatch countDownLatch = new CountDownLatch(2);
//        Runnable runnable = new Runnable() {
//            CountDownLatch latch;
//            @Override
//            public void run() {
//                countDownLatch.countDown();
//                mock.lock();
//                System.out.println(Thread.currentThread().getName() + " get lock");
//                mock.unlock();
//                System.out.println(Thread.currentThread().getName() + " realease lock");
//            }
//        };
//
//        new Thread(runnable).start();
//        new Thread(runnable).start();
//        countDownLatch.await();
//        mock.lock();
//        System.out.println(Thread.currentThread().getName() + " get lock");
//        mock.unlock();
//        System.out.println(Thread.currentThread().getName() + " realease lock");
        CountDownLatch latch = new CountDownLatch(1);
        System.out.println(latch.await(1000, TimeUnit.MILLISECONDS));;
    }

}
