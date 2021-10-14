package kernel;

import hardware.Clock;
import os.Manager;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * 完成对PCB类的代理
 * 数据存放在PCB当中
 * 但是操作原语应当是存在于Process类当中
 */
public class Process implements Comparable<Process> {
    private static final Schedule schedule = Schedule.getInstance();// 调度器部分
    private static final Manager manager = Manager.getInstance();// 资源管理器部分
    // 进程的组成部分
    public PCB pcb;//控制数据部分
    public List<Instruction> instructionList;// 指令段部分
    /**
     * 内存空间的栈，没模拟堆内存；
     * 栈元素为整数，是cpu保留现场所需要的寄存器的值。
     */
    private Stack<Integer> coreStack;    //核心栈
    private Stack<Integer> userStack;    //用户栈

    // 调度相关参数
    private int timeSliceLeft;    //当前进程在cpu运行的剩余时间片，若非运行态则为0
    private int RqNum;      //当前进程在就绪队列位置编号
    private int RqTimes;    //当前进程在就绪队列进入时间
    private int BqNum1;        //当前进程在阻塞队列1位置编号
    private int BqTimes1;    //当前进程在阻塞队列1进入时间
    private int BqNum2;        //当前进程在阻塞队列2位置编号
    private int BqTimes2;    //当前进程在阻塞队列2进入时间
    private int BqNum3;        //当前进程在阻塞队列3位置编号
    private int BqTimes3;    //当前进程在阻塞队列3进入时间

    // 有参数构造
    public Process(PCB pcb, List<Instruction> instructionList) {
        this.pcb = pcb;
        this.instructionList = instructionList;
        this.coreStack = new Stack<>();
        this.userStack = new Stack<>();
    }


    // 进程创建原语；创建成功返回实例
    public synchronized static Process createProcess(PCB pcb, List<Instruction> instructionList) {
        //pcb
        Process ret = new Process(pcb, instructionList);
        ret.pcb.setRunTimes(0);
        ret.pcb.setTurnTimes(0);
        ret.pcb.setPSW(2);
        // sch
        ret.setTimeSliceLeft(0);
        ret.setRqNum(schedule.readyQueue.size());
        ret.setRqTimes(Clock.getCurrentTime());

        ret.setBqNum1(-1);
        ret.setBqTimes1(0);
        ret.setBqNum2(-1);
        ret.setBqTimes2(0);
        ret.setBqNum3(-1);
        ret.setBqTimes3(0);
        ret.pcb.setPC(0);
        ret.pcb.setIR(0);

//        schedule.readyQueue.offer(ret);
        schedule.joinReadyQueue(ret);
        schedule.processList.add(ret);
        schedule.processCount++;
        //进程创建信息
        String createMsg = "进程" + ret.pcb.getProID() + "已创建，详细信息如下：\n";
        createMsg += ret.pcb.toString();
        createMsg += ret.getInstructionListDetail();
        manager.getDashboard().consoleWriteln(createMsg);
        return ret;
    }

    // 进程撤销原语
    public synchronized void cancel() {
        this.pcb.setPSW(0);
        this.pcb.setTurnTimes(Clock.getCurrentTime() - this.pcb.getInTimes() + 1);
        schedule.processList.remove(this);
        schedule.processCount--;
        schedule.readyQueue.remove(this);
        manager.getDashboard().consoleWriteln("进程" + this.pcb.getProID() + "被撤销，执行了" + this.pcb.getInstructNum() + "条指令。\n" +
                "进入时间为" + this.pcb.getInTimes() + "，运行时间为" + this.pcb.getRunTimes() + "，撤销时间为：" + Clock.getCurrentTime() + "，周转时间为:" + this.pcb.getTurnTimes() + "\n");
    }

    // 进程阻塞原语
    public synchronized void block() {
        this.pcb.setPSW(3);
        this.setTimeSliceLeft(0);
        switch (this.pcb.getIR()) {
            case 0:
                break;
            case 1:
                schedule.joinBlockedQueue1(this);
                break;
            case 2:
                schedule.joinBlockedQueue2(this);
                break;
            case 3:
                schedule.joinBlockedQueue3(this);
                break;
            default:
                throw new NotImplementedException();
        }
    }

    // 进程唤醒原语
    public synchronized void wakeup() {
        this.pcb.setPSW(2);
        schedule.joinReadyQueue(this);
        switch (this.pcb.getIR()) {
            case 0:
                break;
            case 1:
                this.setBqNum1(-1);
                break;
            case 2:
                this.setBqNum2(-1);
                break;
            case 3:
                this.setBqNum3(-1);
                break;
            default:
                throw new NotImplementedException();
        }
    }

    /**
     * 静态优先级比较
     * 进程的优先级数字大返回正数，小返回负数，相等返回0
     */
    public int compareTo(Process process) {
        return Integer.compare(this.pcb.Priority, process.pcb.Priority);
    }

    // 指令段信息
    private String getInstructionListDetail() {
        StringBuilder ret = new StringBuilder("指令段详细信息如下：\n");
        for (Instruction ins :
                this.instructionList) {
            ret.append(ins.toString());
        }
        return ret.toString();

    }

    // 栈内存的处理

    /**
     * @Description: 将进程压入核心栈
     */
    public synchronized void pushCoreStack(int register) {
     //   manager.getDashboard().consoleWriteln("正在将进程" + this.pcb.getProID() + "压入核心栈");
        this.coreStack.push(register);
    }

    /**
     * @return int
     * @Description: 将进程弹出核心栈
     */
    public synchronized int popCoreStack() {
      //  manager.getDashboard().consoleWriteln("正在将进程" + this.pcb.getProID() + "弹出核心栈");
        return coreStack.pop();
    }

    // 时间片相关

    /**
     * 返回时间片是否用完
     */

    public boolean isTimeSliceLeft() {
        return this.timeSliceLeft != 0;
    }

    /**
     * 重设当前进程时间片为2
     */
    public void resetTimeSlice() {
        this.timeSliceLeft = 2;
    }

    /**
     * 这个时钟内此进程占用了时间片，剩余时间片-1
     */
    public void useTimeSlice() {
        this.timeSliceLeft--;
    }

    ////  执行指令段相关

    /**
     * 增加轮转时间
     */
    public void plusProcessRunTimes() {
        this.pcb.setRunTimes(this.pcb.getRunTimes() + 1);
    }

    /**
     * 将IR设置成PC指向程序段的的指令类别信息。
     */
    public void setIRNewInstruction() {
        this.pcb.setIR(this.instructionList.get(this.pcb.getPC()).getInstructionState());
    }

    /**
     * 当前指令信息
     */
    public int getCurrentInstructionID() {
        return this.instructionList.get(this.pcb.getPC()).getInstructionID();
    }

    /**
     * 使cpu.pc+1，并检查是否需要cancel
     */
    public void letCPUPlusPCAndCheckCancel() {
        if (this.pcb.getPC() < this.pcb.getInstructNum() - 1)
            this.pcb.setPC(this.pcb.getPC() + 1);
        else {
            this.cancel();
            //一个进程结束被撤销，短时间内CPU可以视作不工作
            manager.getCpu().setWorking(false);
        }
    }

    /**
     * 中断CPU的执行，并检查是否需要cancel
     */
    public void interruptCPUPlusPCAndCheckCancel() {
        if (this.pcb.getPC() < this.pcb.getInstructNum() - 1)
            this.pcb.setPC(this.pcb.getPC() + 1);
        else {
            this.cancel();
        }
    }

    //// accessor

    /**
     * @return the userStack
     */
    public Stack<Integer> getUserStack() {
        return userStack;
    }


    /**
     * @param userStack the userStack to set
     */
    public void setUserStack(Stack<Integer> userStack) {
        this.userStack = userStack;
    }

    public List<Instruction> getInstructionList() {
        return instructionList;
    }

    public void setInstructionList(List<Instruction> instructionList) {
        this.instructionList = instructionList;
    }

    public int getTimeSliceLeft() {
        return timeSliceLeft;
    }

    public void setTimeSliceLeft(int timeSliceLeft) {
        this.timeSliceLeft = timeSliceLeft;
    }

    public int getRqNum() {
        return RqNum;
    }

    public void setRqNum(int rqNum) {
        RqNum = rqNum;
    }

    public int getRqTimes() {
        return RqTimes;
    }

    public void setRqTimes(int rqTimes) {
        RqTimes = rqTimes;
    }

    public int getBqNum1() {
        return BqNum1;
    }

    public void setBqNum1(int bqNum1) {
        BqNum1 = bqNum1;
    }

    public int getBqTimes1() {
        return BqTimes1;
    }

    public void setBqTimes1(int bqTimes1) {
        BqTimes1 = bqTimes1;
    }

    public int getBqNum2() {
        return BqNum2;
    }

    public void setBqNum2(int bqNum2) {
        BqNum2 = bqNum2;
    }

    public int getBqTimes2() {
        return BqTimes2;
    }

    public void setBqTimes2(int bqTimes2) {
        BqTimes2 = bqTimes2;
    }

    public int getBqNum3() {
        return BqNum3;
    }

    public void setBqNum3(int bqNum3) {
        BqNum3 = bqNum3;
    }

    public int getBqTimes3() {
        return BqTimes3;
    }

    public void setBqTimes3(int bqTimes3) {
        BqTimes3 = bqTimes3;
    }
}
