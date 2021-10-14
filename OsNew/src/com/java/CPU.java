package com.java;
/**
 * @Description 模拟CPU类，由于默认的是单核处理器，将所有方法和属性都设为静态的
 *
 */
public class CPU {
	private static int PC;    	   //程序计数器
	private static int IR;        //指令寄存器
	private static int PSW;      //状态寄存器
	private static int cpuTime = 0;  //启动后运行的总时间
	private static boolean ifCpuWork = false;  // cpu是否工作
	private static boolean ifCpuCloseInterrupt = false;  //关中断标志位，为了简化模拟，可以当作false时cpu处于用户态，true时处于核心态
	static Process workingProcess = null;          //正在CPU工作的进程,设为public让所有类都知道哪个在工作中
	
	/**
	* @Description: 根据进程当前的指令，做对应的操作   
	* @throws
	*/
	public static void doInstruction() {
		workingProcess.plusProcessRunTime();
		workingProcess.useTimeSlice();
		workingProcess.setIRNewInstructionState();  //确保每次执行指令时，当前指令都是最新的
		IR = workingProcess.getIR();
		GUI.textArea.append("CPU状态：用户态，正在执行进程" + workingProcess.getID() + "的" + workingProcess.getCurrentInstructionID() + "号指令，类型为" + IR +"\n");
		if(IR == 0) {              //正常执行指令
			CPU.setCpuWorkState(true);
			workingProcess.cpuPlusPCAndCheckIfNeedToCancelTheProcess();                 	  //进程PCB指向下一条指令。
		}
		else if(IR == 1 ) {                                                             	 //系统调用键盘
			switchUserModeToKernelMode();     												//CPU用户态转化核心态
			if(KeyBoard.getKeyBoardState())
				workingProcess.blockProcess(); 											    //用阻塞原语加入对应的阻塞队列排队
			else
				KeyBoard.setKeyBoardWorkForAProcess(workingProcess);
			switchKernelModeToUserMode();   												 //核心态转化为用户态
			CPU.ifCpuWork = false;             												 //设置false更多为了强制用完剩余时间片，并且方便判断指令是否全部执行完
		}else if(IR == 3) {       															 //系统调用显示器
			switchUserModeToKernelMode();    												//CPU用户态转化核心态
			if(Display.getDisplayState())
				workingProcess.blockProcess();  											 //用阻塞原语加入对应的阻塞队列排队
			else 
				Display.setDisplayWork(workingProcess);
			switchKernelModeToUserMode();   												//核心态转化为用户态
			CPU.ifCpuWork = false;
		}
		else if(IR == 2) {        														    //系统调用PV通信线程
			if(PV.getPVState())
				workingProcess.blockProcess();
			else 
				PV.setPVWork(workingProcess);
			CPU.ifCpuWork = false;
		}
		GUI.textArea.append("就绪队列有" + PCB.getReadyQueueLength() + "个进程:");
		PCB.showReadyQueueIds();
	}
	
	/**
	* @Description: CPU用户态转内核态  
	* @throws
	*/
	public static void switchUserModeToKernelMode() {   
		CPU.ifCpuCloseInterrupt = true;  //关中断
		workingProcess.inCoreStack(PC);    //模拟现场保护
		workingProcess.inCoreStack(IR);
		workingProcess.inCoreStack(PSW);
	}
	
	/**
	* @Description: CPU内核态转用户态   
	* @throws
	*/
	public static void switchKernelModeToUserMode() {     //CPU内核态转用户态
		PSW = workingProcess.outCoreStack();
		IR = workingProcess.outCoreStack();     //模拟返回现场
		PC = workingProcess.outCoreStack();
		CPU.ifCpuCloseInterrupt = false;      //模拟开中断
	}

	/**
	* @Description: 调用后让时钟过去一秒  
	* @throws
	*/
	public static void passTime() {
		cpuTime++;
	}
	
	/**
	* @Description: 获取当前时间
	* @return int    
	* @throws
	*/
	public static int getTime() {
		return CPU.cpuTime;
	}
	
	/**
	* @Description: 获取CPU工作状态
	* @return boolean    
	* @throws
	*/
	public static boolean getCpuWorkState() {
		return ifCpuWork;
	}
	
	/**
	* @Description: 设置CPU工作状态
	* @param state   待设置的状态    
	* @throws
	*/
	public static void setCpuWorkState(boolean state) {
		ifCpuWork = state;
	}
	
	/**
	* @Description:  设置CPU的PC寄存器，决定要执行哪一个地址的指令
	* @param tempPC 待设置的pc    
	* @throws
	*/
	public static void setPC(int tempPC) {
		PC = tempPC;
	}
	
	/**
	* @Description: 设置CPU的IR寄存器，决定要执行什么指令
	* @param tempIR     
	* @throws
	*/
	public static void setIR(int tempIR) {
		IR = tempIR;
	}
	
	/**
	* @Description: 设置CPU的PSW寄存器
	* @param tempPSW void    
	* @throws
	*/
	public static void setPSW(int tempPSW) {
		PSW = tempPSW;
	}

	/**
	 * @return the ifCpuCloseIntrrupt
	 */
	public static boolean getIfCpuCloseIntrrupt() {
		return ifCpuCloseInterrupt;
	}

	/**
	 * @param ifCpuCloseIntrrupt
	 */
	public static void setIfCpuCloseIntrrupt(boolean ifCpuCloseIntrrupt) {
		CPU.ifCpuCloseInterrupt = ifCpuCloseIntrrupt;
	}
}
