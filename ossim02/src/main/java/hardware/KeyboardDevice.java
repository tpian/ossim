package hardware;

import kernel.Process;
import kernel.Schedule;
import os.Manager;

public class KeyboardDevice extends Thread {
    private static final Manager manager = Manager.getInstance();

    private static boolean ifKeyboardWork = false; //表示键盘输入线程状态，false为空闲，true为忙碌
    private static Process usingProcess = null;    //正在等待键盘结果的线程
    private static int lastUseTime = 0;            //用来计数等待的线程已经等了多久

    public void run() {
        while (true) {
            manager.getGlobalLock().lock();//请求锁
            try {
                manager.getTimerCondition().await();        //等到时钟进程发出时钟中断，再开始执行下面操作
                doWhatKeyBoardDoEverySecond();                   //执行每秒此线程被唤醒后该执行的程序
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                manager.getGlobalLock().unlock();//释放锁
            }

        }
    }

    /**
     * @Description: 根据不同的状态，执行每次此线程被唤醒后该执行的程序
     */
    private void doWhatKeyBoardDoEverySecond() {
        if (ifKeyboardWork && Clock.getCurrentTime() - lastUseTime == 5) {   //键盘中断需要4秒，如果阻塞队列为空，运行指令那一秒也会计算，因此此处差应为5秒
            usingProcess.wakeup();   //系统调用结束，唤醒此进程
            usingProcess.interruptCPUPlusPCAndCheckCancel();   //此进程的pc指向下一条
            usingProcess = manager.getSchedule().leaveBlockedQueue1();  //调入阻塞队列其他进程，如果有返回队头，如果没有返回空地址
            if (usingProcess != null)                 //如果阻塞队列不空队头得到键盘，空则空闲
                lastUseTime = Clock.getCurrentTime() - 1;       //1秒作为阻塞队列后续进程获得键盘的补偿
            else
                ifKeyboardWork = false;
        }
        if (!ifKeyboardWork) {                    //根据键盘的状态输出信息
            manager.getDashboard().consoleWriteln("键盘状态：无进程请求" + "\n");
        } else {
            manager.getDashboard().consoleWriteln("键盘状态：进程" + usingProcess.pcb.getProID() + "正在请求\n");
            // bq1 状态
            Schedule.displayQueueStatus(manager.getSchedule().blockQueue01, 1);
        }
    }


    /**
     * @return boolean
     * @Description: 静态返回键盘状态
     */
    public static boolean getKeyBoardState() {
        return ifKeyboardWork;
    }

    /**
     * @Description: 设置键盘状态
     */
    public static void setKeyBoardState(boolean state) {
        ifKeyboardWork = state;
    }

    /**
     * @param p 要是用键盘的进程
     * @Description: 让一个进程得到键盘后执行的操作
     */
    public static void setKeyBoardWorkForAProcess(Process p) {
        ifKeyboardWork = true;
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
