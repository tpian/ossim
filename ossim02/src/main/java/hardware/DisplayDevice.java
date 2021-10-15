package hardware;

import kernel.Process;
import kernel.Schedule;
import os.Manager;

public class DisplayDevice extends Thread {

    private static boolean ifDisplayWork = false; //表示键盘输入线程状态，false为空闲，true为忙碌
    private static Process usingProcess = null;
    private static int lastUseTime = 0;

    public void run() {
        while (true) {
        	Manager.getGlobalLock().lock();//请求锁
            try {
            	Manager.getTimerCondition().await();
                doWhatDisplayDoEverySecond();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
            	Manager.getGlobalLock().unlock();//释放锁
            }
        }
    }

    /**
     * @throws
     * @Description: 根据不同的状态，执行每次此线程被唤醒后该执行的程序
     */
    private void doWhatDisplayDoEverySecond() {
        if (ifDisplayWork && Clock.getCurrentTime() - lastUseTime == 4) {
            usingProcess.wakeup();   //系统调用结束，唤醒此进程
            usingProcess.interruptCPUPlusPCAndCheckCancel();   //此进程的pc指向下一条
            usingProcess = Schedule.leaveBlockedQueue2();  //调入阻塞队列其他进程，如果有返回队头，如果没有返回空地址
            if (usingProcess != null)
                lastUseTime = Clock.getCurrentTime() - 1;
            else
                ifDisplayWork = false;
        }
        if (!ifDisplayWork) {
        	Manager.getDashboard().consoleWriteln("显示器状态： 无进程请求" + "\n");
        } else {
        	Manager.getDashboard().consoleWriteln("显示器状态：进程" + usingProcess.pcb.getProID() + "正在请求\n");
            Schedule.displayQueueStatus(Schedule.blockQueue02, 2);
        }
    }


    /**
     * @return boolean
     * @throws
     * @Description: 静态返回显示器状态
     */
    public static boolean getDisplayState() {
        return ifDisplayWork;
    }

    public static void setDisplayWork(Process p) {
        ifDisplayWork = true;
        usingProcess = p;
        p.pcb.setPSW(3);
        lastUseTime = Clock.getCurrentTime();
    }

    /**
     * @return the usingProcess
     */
    public static Process getUsingProcess() {
        return usingProcess;
    }


}
