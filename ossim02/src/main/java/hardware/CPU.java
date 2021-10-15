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
    static int PC;           //程序计数器
    private static int IR;        //指令寄存器
    private static int PSW;      //状态寄存器
    private static boolean isWorking;  // cpu是否工作
    /**
     * 中断标志位
     * false时cpu处于用户态，true时处于核心态。
     */
    private static boolean closeInterruptFlag;
    public static Process currentProcess;          //正在CPU工作的进程


    private CPU() {
        this.setPC(-1);
        this.setIR(-1);
        this.setPSW(-1);
        this.setWorking(false);
        this.setCloseInterruptFlag(false);
        this.currentProcess = null;
    }

    /**
     * 根据指令寄存器，执行对应的操作
     *
     * @return 指令执行是否成功
     */
    public synchronized static void execute() {
        // 更新时间片相关
        currentProcess.plusProcessRunTimes();
        currentProcess.useTimeSlice();
        // 更新指令和寄存器相关
        //TODO 先后顺序
        currentProcess.setIRNewInstruction();
        setIR(currentProcess.pcb.getIR());
        switch (IR) {
            case 0://正常执行
            {
                setWorking(true);
                currentProcess.letCPUPlusPCAndCheckCancel();
                break;
            }
            case 1: //检查键盘
            {
                switchUserModeToKernelMode();
                boolean processIsInKeyBoard = KeyboardDevice.getUsingProcess().pcb.getProID()!=currentProcess.pcb.getProID();
                if (KeyboardDevice.getKeyBoardState())
                	if (!processIsInKeyBoard) {
                		currentProcess.block();
					}
                else
                    KeyboardDevice.setKeyBoardWorkForAProcess(currentProcess);
                switchKernelModeToUserMode();
                setWorking(false);
            }
            case 2:// 检查PV控制
            {
                if (PV.getPVState())
                    currentProcess.block();
                else
                    PV.setPVWork(currentProcess);
                setWorking(false);
            }
            case 3:// 检查显示器
            {
                switchUserModeToKernelMode();
                if (DisplayDevice.getDisplayState())
                    currentProcess.block();
                else
                    DisplayDevice.setDisplayWork(currentProcess);
                switchKernelModeToUserMode();
                setWorking(false);
            }
            // 显示指令执行状态
//            manager.getDashboard().consoleWriteln("CPU状态：用户态，正在执行进程" + this.currentProcess.pcb.getProID() + "的" + this.currentProcess.getCurrentInstructionID() + "号指令，类型为" + this.getIR() + "\n");
            // 显示就绪队列状态
            Schedule.displayQueueStatus(Schedule.readyQueue, 0);
        }

    }


    //// interrupt implement
    public synchronized static void switchUserModeToKernelMode() {     //CPU用户态转内核态
        setCloseInterruptFlag(true);  //关中断
        currentProcess.pushCoreStack(PC);   //模拟现场保护
        currentProcess.pushCoreStack(IR);   //模拟现场保护
        currentProcess.pushCoreStack(PSW);   //模拟现场保护
    }

    public synchronized static void switchKernelModeToUserMode() {     //CPU内核态转用户态
        PSW = currentProcess.popCoreStack();  //模拟返回现场
        IR = currentProcess.popCoreStack();
        PC = currentProcess.popCoreStack();
        setCloseInterruptFlag(false);      //开中断
    }

    public synchronized static void processContextSwitch(Process newRunProcess) {
        setCurrentProcess(newRunProcess);
        // 初始时刻，cpu.currentProcess==null，不需要进行保留现场，直接返回。
        if (currentProcess == null) {
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

    public static void setPC(int PCTMP) {
        PC = PCTMP;
    }

    public int getIR() {
        return IR;
    }

    public static void setIR(int IRTMP) {
        IR = IRTMP;
    }

    public int getPSW() {
        return PSW;
    }

    public static void setPSW(int PSWTMP) {
        PSW = PSWTMP;
    }

    public static boolean isWorking() {
        return isWorking;
    }

    public static void setWorking(boolean working) {
        isWorking = working;
    }

    public boolean isCloseInterruptFlag() {
        return closeInterruptFlag;
    }

    public static void setCloseInterruptFlag(boolean closeInterruptFlagTMP) {
        closeInterruptFlag = closeInterruptFlagTMP;
    }

    public Process getCurrentProcess() {
        return currentProcess;
    }

    public static void setCurrentProcess(Process currentProcessTMP) {
        currentProcess = currentProcessTMP;
    }
}
