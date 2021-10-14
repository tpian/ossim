package kernel;

import com.sun.jndi.ldap.pool.Pool;
import hardware.CPU;
import hardware.Clock;
import os.Manager;

import java.util.*;

public class Schedule extends Thread {
    // 单例
    private static Schedule schedule;
    private static final Manager manager = Manager.getInstance();// 资源管理器部分
    private final CPU cpu = CPU.getInstance();

    public static LinkedList<Process> readyQueue = new LinkedList<>();   //就绪队列  使用静态变量，所有Process共享四个队列
    public static LinkedList<Process> blockQueue01 = new LinkedList<>();    //阻塞队列1  java中 list实现了Queue接口，可以当队列使用
    public static LinkedList<Process> blockQueue02 = new LinkedList<>();    //阻塞队列2
    public static LinkedList<Process> blockQueue03 = new LinkedList<>();    //阻塞队列3
    public static List<Process> processList = new Vector<>();    //全局进程表，为了保证线程安全，选取为Vector结构
    public static int processCount = 0;
    private static final String[] queueTypeNameMap = new String[]{"就绪队列", "阻塞队列1", "阻塞队列2", "阻塞队列3"};


    private Schedule() {
    }

    // 单例化
    public static synchronized Schedule getInstance() {
        if (schedule == null) {
            return new Schedule();
        }
        return schedule;
    }


    @Override
    public void run() {
        //! 调度主循环；程序核心
        while (true) {
            manager.getGlobalLock().lock();//请求锁
            try {
                manager.getTimerCondition().await();  //唤醒所有等待线程
                // 检查新作业的到来
                checkNewJobs();
                //执行时间片轮转算法
                roundSchedule();
            } catch (InterruptedException exception) {
                exception.printStackTrace();
            } finally {
                manager.getGlobalLock().unlock();//释放锁
            }

        }
    }


    //// 调度算法实现
    // 检查新作业的到来
    private synchronized void checkNewJobs() {
        manager.getDashboard().consoleWriteln("检查是否有作业到来");
        JobManager.loadJobsAndIns();
    }

    /**
     * 静态优先级算法
     * 将就绪队列中的PCB按照优先级排序
     */
    private synchronized void staticPriority() {
        Collections.sort(readyQueue);
        System.out.print(readyQueue);
    }


    /**
     * 时间片轮转算法
     */
    private void roundSchedule() {
        this.staticPriority();  //按优先级大小对就绪队列进行重新排队
        if (this.cpu.isWorking()) {//cpu工作
            if (this.cpu.currentProcess.isTimeSliceLeft()) {   //如果正在运行的进程时间片还有剩余，那么一个时钟中断周期内此进程继续运行
                this.cpu.execute();             //根据不同的指令执行对应的操作
            } else {                                  //时间片到，将此进程移到就绪队列排队，进行进程上下文切换，再从就绪队列取出优先级最高的进程执行
                this.cpu.currentProcess.pcb.setPSW(2);
                this.cpu.currentProcess.setRqTimes(Clock.getCurrentTime());
                schedule.joinReadyQueue(this.cpu.currentProcess);//当前进程进入就绪队列,按优先级大小对就绪队列进行重新排队
                Process readyProcess = this.leaveReadyQueue();
                if (readyProcess == null) {                     //如果就绪队列空，打印this.cpu空闲状态
                    manager.getDashboard().consoleWriteln("\n===当前CPU空闲，并保持空闲状态===\n");
                } else {
                    this.cpu.processContextSwitch(readyProcess);   //进行进程上下文切换
                    this.cpu.currentProcess.resetTimeSlice();
                    this.cpu.execute();            //根据不同的指令执行对应的操作
                }
            }
        } else {
            // cpu不工作
            // 如果this.cpu此刻不工作，就从就绪队列首位取元素,切换设置为cpu工作进程；
            // 如果就绪队列为空会返回一个空地址
            Process readyProcess = this.leaveReadyQueue();
            if (readyProcess == null) {                     //如果就绪队列空，打印this.cpu空闲状态
                manager.getDashboard().consoleWriteln("\n===当前CPU空闲，并保持空闲状态===\n");
            } else {                                 //就绪队列不空，进行进程上下文切换，再从就绪队列取出优先级最高的进程执行
                this.cpu.processContextSwitch(readyProcess);
                this.cpu.setWorking(true);       //检测到了还有指令没做完，this.cpu状态设为work
                manager.getDashboard().consoleWriteln("\n当前CPU工作\n");
                this.cpu.currentProcess.resetTimeSlice();
                this.cpu.execute();            //根据不同的指令执行对应的操作
            }
        }
    }

    //// 队列相关方法

    /**
     * @Description: 进程加入阻塞队列1
     */
    public void joinBlockedQueue1(Process process) {
        blockQueue01.offer(process);
        process.setBqNum1(blockQueue01.indexOf(process));
        process.setBqTimes1(Clock.getCurrentTime());
    }

    /**
     * @Description: 进程加入阻塞队列2
     */
    public void joinBlockedQueue2(Process process) {
        blockQueue02.offer(process);
        process.setBqNum2(blockQueue02.indexOf(process));
        process.setBqTimes2(Clock.getCurrentTime());
    }

    /**
     * @Description: 进程加入阻塞队列3
     */
    public void joinBlockedQueue3(Process process) {
        blockQueue03.offer(process);
        process.setBqNum3(blockQueue03.indexOf(process));
        process.setBqTimes3(Clock.getCurrentTime());
    }

    /**
     * @Description: 进程加入就绪队列
     */
    public void joinReadyQueue(Process process) {
        readyQueue.offer(process);
        process.setRqNum(readyQueue.indexOf(process));
        process.setRqTimes(Clock.getCurrentTime());
        this.staticPriority();  //按优先级大小对就绪队列进行重新排队
    }

    public Process leaveBlockedQueue1() {
        Process process = blockQueue01.poll();
        for (Process p : blockQueue01) {
            p.setBqNum1(blockQueue01.indexOf(p));
        }
        return process;
    }

    public Process leaveBlockedQueue2() {
        Process process = blockQueue02.poll();
        for (Process p : blockQueue02) {
            p.setBqNum2(blockQueue02.indexOf(p));
        }
        return process;
    }

    public Process leaveBlockedQueue3() {
        Process process = blockQueue03.poll();
        for (Process p : blockQueue03) {
            p.setBqNum3(blockQueue03.indexOf(p));
        }
        return process;
    }

    public Process leaveReadyQueue() {
        Process process = readyQueue.poll();
        for (Process p : readyQueue) {
            p.setRqNum(readyQueue.indexOf(p));
        }
        return process;
    }

    // 展示指定队列的状态
    public static void displayQueueStatus(LinkedList<Process> queue, int queueType) {
        manager.getDashboard().consoleWriteln("当前" + queueTypeNameMap[queueType] + "存在" + queue.size() + "个进程。进程号分别是：\n");
        for (Process e : queue) {
            manager.getDashboard().consoleWriteln(e.pcb.getProID() + "\t");
        }
        manager.getDashboard().consoleWriteln("\n");
    }

    // 展示当前schedule的四条队列信息
    public void displayAllQueue() {
        manager.getDashboard().consoleWriteln("当前调度模块状态如下：");
        displayQueueStatus(readyQueue, 0);
        displayQueueStatus(blockQueue01, 1);
        displayQueueStatus(blockQueue02, 2);
        displayQueueStatus(blockQueue03, 3);
    }
}
