package com.java;
/**
 * @Description ģ��CPU�࣬����Ĭ�ϵ��ǵ��˴������������з��������Զ���Ϊ��̬��
 *
 */
public class CPU {
	private static int PC;    	   //���������
	private static int IR;        //ָ��Ĵ���
	private static int PSW;      //״̬�Ĵ���
	private static int cpuTime = 0;  //���������е���ʱ��
	private static boolean ifCpuWork = false;  // cpu�Ƿ���
	private static boolean ifCpuCloseInterrupt = false;  //���жϱ�־λ��Ϊ�˼�ģ�⣬���Ե���falseʱcpu�����û�̬��trueʱ���ں���̬
	static Process workingProcess = null;          //����CPU�����Ľ���,��Ϊpublic�������඼֪���ĸ��ڹ�����
	
	/**
	* @Description: ���ݽ��̵�ǰ��ָ�����Ӧ�Ĳ���   
	* @throws
	*/
	public static void doInstruction() {
		workingProcess.plusProcessRunTime();
		workingProcess.useTimeSlice();
		workingProcess.setIRNewInstructionState();  //ȷ��ÿ��ִ��ָ��ʱ����ǰָ������µ�
		IR = workingProcess.getIR();
		GUI.textArea.append("CPU״̬���û�̬������ִ�н���" + workingProcess.getID() + "��" + workingProcess.getCurrentInstructionID() + "��ָ�����Ϊ" + IR +"\n");
		if(IR == 0) {              //����ִ��ָ��
			CPU.setCpuWorkState(true);
			workingProcess.cpuPlusPCAndCheckIfNeedToCancelTheProcess();                 	  //����PCBָ����һ��ָ�
		}
		else if(IR == 1 ) {                                                             	 //ϵͳ���ü���
			switchUserModeToKernelMode();     												//CPU�û�̬ת������̬
			if(KeyBoard.getKeyBoardState())
				workingProcess.blockProcess(); 											    //������ԭ������Ӧ�����������Ŷ�
			else
				KeyBoard.setKeyBoardWorkForAProcess(workingProcess);
			switchKernelModeToUserMode();   												 //����̬ת��Ϊ�û�̬
			CPU.ifCpuWork = false;             												 //����false����Ϊ��ǿ������ʣ��ʱ��Ƭ�����ҷ����ж�ָ���Ƿ�ȫ��ִ����
		}else if(IR == 3) {       															 //ϵͳ������ʾ��
			switchUserModeToKernelMode();    												//CPU�û�̬ת������̬
			if(Display.getDisplayState())
				workingProcess.blockProcess();  											 //������ԭ������Ӧ�����������Ŷ�
			else 
				Display.setDisplayWork(workingProcess);
			switchKernelModeToUserMode();   												//����̬ת��Ϊ�û�̬
			CPU.ifCpuWork = false;
		}
		else if(IR == 2) {        														    //ϵͳ����PVͨ���߳�
			if(PV.getPVState())
				workingProcess.blockProcess();
			else 
				PV.setPVWork(workingProcess);
			CPU.ifCpuWork = false;
		}
		GUI.textArea.append("����������" + PCB.getReadyQueueLength() + "������:");
		PCB.showReadyQueueIds();
	}
	
	/**
	* @Description: CPU�û�̬ת�ں�̬  
	* @throws
	*/
	public static void switchUserModeToKernelMode() {   
		CPU.ifCpuCloseInterrupt = true;  //���ж�
		workingProcess.inCoreStack(PC);    //ģ���ֳ�����
		workingProcess.inCoreStack(IR);
		workingProcess.inCoreStack(PSW);
	}
	
	/**
	* @Description: CPU�ں�̬ת�û�̬   
	* @throws
	*/
	public static void switchKernelModeToUserMode() {     //CPU�ں�̬ת�û�̬
		PSW = workingProcess.outCoreStack();
		IR = workingProcess.outCoreStack();     //ģ�ⷵ���ֳ�
		PC = workingProcess.outCoreStack();
		CPU.ifCpuCloseInterrupt = false;      //ģ�⿪�ж�
	}

	/**
	* @Description: ���ú���ʱ�ӹ�ȥһ��  
	* @throws
	*/
	public static void passTime() {
		cpuTime++;
	}
	
	/**
	* @Description: ��ȡ��ǰʱ��
	* @return int    
	* @throws
	*/
	public static int getTime() {
		return CPU.cpuTime;
	}
	
	/**
	* @Description: ��ȡCPU����״̬
	* @return boolean    
	* @throws
	*/
	public static boolean getCpuWorkState() {
		return ifCpuWork;
	}
	
	/**
	* @Description: ����CPU����״̬
	* @param state   �����õ�״̬    
	* @throws
	*/
	public static void setCpuWorkState(boolean state) {
		ifCpuWork = state;
	}
	
	/**
	* @Description:  ����CPU��PC�Ĵ���������Ҫִ����һ����ַ��ָ��
	* @param tempPC �����õ�pc    
	* @throws
	*/
	public static void setPC(int tempPC) {
		PC = tempPC;
	}
	
	/**
	* @Description: ����CPU��IR�Ĵ���������Ҫִ��ʲôָ��
	* @param tempIR     
	* @throws
	*/
	public static void setIR(int tempIR) {
		IR = tempIR;
	}
	
	/**
	* @Description: ����CPU��PSW�Ĵ���
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
