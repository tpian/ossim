package kernel;

public class Instruction {
    private int instructionID;      //指令序号
    private int instructionState;   //指令类型

    public Instruction(int id, int state) {
        this.instructionID = id;
        this.instructionState = state;
    }

    public int getInstructionID() {
        return instructionID;
    }

    public int getInstructionState() {
        return instructionState;
    }

    @Override
    public String toString() {
        return "指令序号为" + instructionID + " 指令类型为" + instructionState + "\n";
    }

}
