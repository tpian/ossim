package hardware;

import javafx.concurrent.Worker;
import kernel.Instruction;
import kernel.PV;
import kernel.Schedule;
import os.Manager;
import kernel.Process;
import sun.security.util.Debug;


public class CPU {
    private static CPU cpu;
    private static final Manager manager = Manager.getInstance();
    private int PC;           //程序计数器
    private int IR;        //指令寄存器
    private int PSW;      //状态寄存器
    private boolean isWorking;  // cpu是否工作
    /**
     * 中断标志位
     * false时cpu处于用户态，true时处于核心态。
     */
    private boolean closeInterruptFlag;
    public Process currentProcess;          //正在CPU工作的进程


    private CPU() {
        this.setPC(-1);
        this.setIR(-1);
        this.setPSW(-1);
        this.setWorking(false);
        this.setCloseInterruptFlag(false);
        this.currentProcess = null;
    }

    // 单例
    public static CPU getInstance() {
        if (cpu == null) {
            return new CPU();
        }
        return cpu;
    }

    /**
     * 根据指令寄存器，执行对应的操作
     *
     * @return 指令执行是否成功
     */
    public synchronized void execute() {
        // 更新时间片相关
        this.currentProcess.plusProcessRunTimes();
        this.currentProcess.useTimeSlice();
        // 更新指令和寄存器相关
        this.currentProcess.setIRNewInstruction();
        this.setIR(this.currentProcess.pcb.getIR());
        switch (this.IR) {
            case 0://正常执行
            {
                this.setWorking(true);
                this.currentProcess.letCPUPlusPCAndCheckCancel();
            }
            case 1: //检查键盘
            {
                this.switchUserModeToKernelMode();
                if (KeyboardDevice.getKeyBoardState())
                    this.currentProcess.block();
                else
                    KeyboardDevice.setKeyBoardWorkForAProcess(this.currentProcess);

                this.switchKernelModeToUserMode();
                setWorking(false);
            }
            case 2:// 检查PV控制
            {
                if (PV.getPVState())
                    this.currentProcess.block();
                else
                    PV.setPVWork(this.currentProcess);
                setWorking(false);
            }
            case 3:// 检查显示器
            {
                this.switchUserModeToKernelMode();
                if (DisplayDevice.getDisplayState())
                    this.currentProcess.block();
                else
                    DisplayDevice.setDisplayWork(this.currentProcess);
                this.switchKernelModeToUserMode();
                setWorking(false);
            }
            // 显示指令执行状态
//            manager.getDashboard().consoleWriteln("CPU状态：用户态，正在执行进程" + this.currentProcess.pcb.getProID() + "的" + this.currentProcess.getCurrentInstructionID() + "号指令，类型为" + this.getIR() + "\n");
            // 显示就绪队列状态
            Schedule.displayQueueStatus(Schedule.readyQueue, 0);
        }

    }


    //// interrupt implement
    public synchronized void switchUserModeToKernelMode() {     //CPU用户态转内核态
        this.setCloseInterruptFlag(true);  //关中断
        this.currentProcess.pushCoreStack(PC);   //模拟现场保护
        this.currentProcess.pushCoreStack(IR);   //模拟现场保护
        this.currentProcess.pushCoreStack(PSW);   //模拟现场保护
    }

    public synchronized void switchKernelModeToUserMode() {     //CPU内核态转用户态
        PSW = this.currentProcess.popCoreStack();  //模拟返回现场
        IR = this.currentProcess.popCoreStack();
        PC = this.currentProcess.popCoreStack();
        this.setCloseInterruptFlag(false);      //开中断
    }

    public synchronized void processContextSwitch(Process newRunProcess) {
        setCurrentProcess(newRunProcess);
        // 初始时刻，cpu.currentProcess==null，不需要进行保留现场，直接返回。
        if (this.currentProcess == null) {
            return;
        }
        //进程上下文切换是要在CPU核心态下实现的
        switchUserModeToKernelMode();
        newRunProcess.pcb.setPSW(1);
        setCurrentProcess(newRunProcess);
        switchKernelModeToUserMode();
        setPC(newRunProcess.pcb.getPC());
        setIR(newRunProcess.pcb.getIR());
        setPSW(newRunProcess.pcb.getPSW());
    }


    //// accessor
    public int getPC() {
        return PC;
    }

    public void setPC(int PC) {
        this.PC = PC;
    }

    public int getIR() {
        return IR;
    }

    public void setIR(int IR) {
        this.IR = IR;
    }

    public int getPSW() {
        return PSW;
    }

    public void setPSW(int PSW) {
        this.PSW = PSW;
    }

    public boolean isWorking() {
        return isWorking;
    }

    public void setWorking(boolean working) {
        isWorking = working;
    }

    public boolean isCloseInterruptFlag() {
        return closeInterruptFlag;
    }

    public void setCloseInterruptFlag(boolean closeInterruptFlag) {
        this.closeInterruptFlag = closeInterruptFlag;
    }

    public Process getCurrentProcess() {
        return currentProcess;
    }

    public void setCurrentProcess(Process currentProcess) {
        this.currentProcess = currentProcess;
    }
}
