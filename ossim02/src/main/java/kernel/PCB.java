package kernel;

public class PCB {

    private int ProID; //进程编号,值为1,2,3,4,5,6...
    public int Priority; //进程优先数
    private int InTimes; //进程创建时间
    private int EndTimes; //进程结束时间
    private int PSW;      //进程状态 0为未知 1为运行 2为就绪 3为阻塞
    private int RunTimes; //进程运行时间列表
    private int TurnTimes; //进程周转时间统计
    private int InstructNum; //进程中包含的指令数目
    private int PC;           //程序计数器信息，记录下一条指令地址
    private int IR;        //指令寄存器信息，记录当前执行的指令类型

    public PCB(int proID, int priority, int inTimes, int instructNum) {
        this.ProID = proID;
        this.Priority = priority;
        this.InTimes = inTimes;
        this.InstructNum = instructNum;
    }

    @Override
    public String toString() {
        return "PCB状态：进程号为" + ProID + "，进入时间为" + this.InTimes + "，优先级为" + Priority + "，指令数量为" + InstructNum + "\n";
    }

    ////accessor
    public int getProID() {
        return ProID;
    }

    public void setProID(int proID) {
        ProID = proID;
    }

    public int getPriority() {
        return Priority;
    }

    public void setPriority(int priority) {
        Priority = priority;
    }

    public int getInTimes() {
        return InTimes;
    }

    public void setInTimes(int inTimes) {
        InTimes = inTimes;
    }

    public int getEndTimes() {
        return EndTimes;
    }

    public void setEndTimes(int endTimes) {
        EndTimes = endTimes;
    }

    public int getPSW() {
        return PSW;
    }

    public void setPSW(int PSW) {
        this.PSW = PSW;
    }

    public int getRunTimes() {
        return RunTimes;
    }

    public void setRunTimes(int runTimes) {
        RunTimes = runTimes;
    }

    public int getTurnTimes() {
        return TurnTimes;
    }

    public void setTurnTimes(int turnTimes) {
        TurnTimes = turnTimes;
    }

    public int getInstructNum() {
        return InstructNum;
    }

    public void setInstructNum(int instructNum) {
        InstructNum = instructNum;
    }

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


}
