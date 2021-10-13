package kernel;

import hardware.Clock;
import kernel.Process;
import os.Manager;

// PV原语的实现，类似于全局的deadlock detection
public class PV extends Thread {
    private static final Manager manager = Manager.getInstance();

    private static boolean ifPVWork = false; //表示键盘输入线程状态，false为空闲，true为忙碌
    private static Process usingProcess = null;
    private static int lastUseTime = 0;

    public void run() {
        while (true) {
            manager.getGlobalLock().lock();//请求锁
            try {
                manager.getTimerCondition().await();
                doWhatPVDoEverySecond();

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
    private void doWhatPVDoEverySecond() {
        if (ifPVWork && Clock.getCurrentTime() - lastUseTime == 3) {
            usingProcess.wakeup();   //PV调用结束，唤醒此进程
            usingProcess.interruptCPUPlusPCAndCheckCancel();   //此进程的pc指向下一条
            usingProcess = manager.getSchedule().leaveBlockedQueue3();  //调入阻塞队列其他进程，如果有返回队头，如果没有返回空地址

            if (usingProcess != null)
                lastUseTime = Clock.getCurrentTime() - 1;
            else
                ifPVWork = false;
        }
        if (!ifPVWork) {
            manager.getDashboard().consoleWriteln("PV状态：无进程请求");
        } else {
            manager.getDashboard().consoleWriteln("PV状态：进程" + usingProcess.pcb.getProID() + "正在请求");
            Schedule.displayQueueStatus(manager.getSchedule().blockQueue03, 3);
        }
    }

    /**
     * @return boolean
     * @Description: 静态返回PV状态
     */
    public static boolean getPVState() {
        return ifPVWork;
    }

    public static void setPVWork(Process p) {
        ifPVWork = true;
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
