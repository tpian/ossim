package hardware;

import os.Manager;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 时钟类，用于系统时钟
 * <p>
 * 继承Thread用于间隔固定时间无限循环进行操作
 */
public class Clock extends Thread {
    /**
     * 系统时间间隔，单位 ms
     */
    private static final int INTERVAL = 1000;
    /**
     * 系统最长运行时间
     */
    private static final int MAXTIME = 1000;
    /**
     * 当前系统时间，单位为1s
     */
    private static volatile int currentTime;
    /**
     * 暂停标志
     */
    private static volatile boolean pause;

    public Clock() {
        super("Clock");
        Clock.currentTime = -1;
        Clock.pause = false;
    }

    /**
     * 时钟增加
     */
    public synchronized void addTime() {
        assert !Clock.isPause();
        ++Clock.currentTime;
    }

    /**
     * 时钟暂停
     */
    public synchronized void suspendTime(boolean suspend) {
        // 当唤醒时，提示全部线程
        if (!suspend) {
            notifyAll();
        }
        Clock.pause = suspend;
    }

    /**
     * 开始进行时钟中断
     */
    @Override
    public void run() {
        while (Clock.currentTime < Clock.MAXTIME) {
            if (Clock.isPause()) {// 暂停状态
                try {
                    Thread.sleep(Clock.INTERVAL);
                    // 暂停时不需要addTime
                } catch (InterruptedException exception) {
                    exception.printStackTrace();
                }
                continue;
            }
            Manager.getGlobalLock().lock();//请求
            try {
            	Manager.getDashboard().consoleBar();
            	Manager.getDashboard().consoleWriteln("\n系统时间：" + Clock.getCurrentTime() + "\n");
            	Manager.getTimerCondition().signalAll();

            } finally {
            	Manager.getGlobalLock().unlock();//释放
            }
            // 未暂停，正常工作
            try {
                Thread.sleep(Clock.INTERVAL);
                Manager.getDashboard().consoleScroll();
                Clock.currentTime++;
            } catch (InterruptedException exception) {
                exception.printStackTrace();
            }
        }
    }

    ////
    public static int getCurrentTime() {
        return currentTime;
    }

    public static boolean isPause() {
        return pause;
    }
}

