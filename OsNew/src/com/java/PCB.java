package com.java;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;


/**
 * @Description PCB��
 *
 */
public class PCB implements Comparable<PCB>{
	private int ProID; //���̱��,ֵΪ1,2,3,4,5,6...
	private int Priority; //����������
	private int InTimes; //���̴���ʱ��
	private int EndTimes; //���̽���ʱ��
	private int PSW;      //����״̬ 0Ϊδ֪ 1Ϊ���� 2Ϊ���� 3Ϊ����
	private int RunTimes; //��������ʱ���б�
	private int TurnTimes; //������תʱ��ͳ��
	private int InstrucNum; //�����а�����ָ����Ŀ
	private int PC;    	   //�����������Ϣ����¼��һ��ָ���ַ
	private int IR;        //ָ��Ĵ�����Ϣ����¼��ǰִ�е�ָ������
	
	private int timeSliceLeft;    //��ǰ������cpu���е�ʣ��ʱ��Ƭ����������̬��Ϊ0
	private int RqNum;      //��������λ�ñ��
	private int RqTimes;    //�������н���ʱ��
	private int BqNum1;		//��������1λ�ñ��
	private int BqTimes1;	//��������1����ʱ��
	private int BqNum2;		//��������2λ�ñ��
	private int BqTimes2;	//��������2����ʱ��
	private int BqNum3;		//��������3λ�ñ��
	private int BqTimes3;	//��������3����ʱ��
	
	private static LinkedList<PCB> Rq = new LinkedList<PCB>();	//��������  ʹ�þ�̬����������PCB�����ĸ�����
	private static LinkedList<PCB> Bq1 = new LinkedList<PCB>();	//��������1  java��Linkedlistʵ����Queue�ӿڣ����Ե�����ʹ��
	private static LinkedList<PCB> Bq2 = new LinkedList<PCB>();	//��������2
	private static LinkedList<PCB> Bq3 = new LinkedList<PCB>(); 	//��������3
	private static ArrayList<PCB> allPCB = new ArrayList<>();    //PCB��
	private static int PCBNum = 0;                //PCB����PCB����
	
	/**
	*  PCB�Ĺ��캯�� 
	*/
	public PCB(int ProID, int Priority, int InTimes, int InstrucNum) {
		while(!isValidPCB(ProID))    //�����ظ��Ľ���ProID����ProID��1ֱ�����봴������PCB��id�ظ���
			ProID++;
		createProcess(ProID, Priority, InTimes, InstrucNum);
	} 
	
	/**
	*  ���һ������ProID�Ƿ����������ڹ��Ľ����ظ����ظ�����false
	*/
	public boolean isValidPCB(int ProID) {
		for(PCB e : allPCB) {
			if(e.getID() == ProID)
				return false;
		}
		return true;
	}
	
	
	/**
	*  ��������ԭ��
	*/
	protected synchronized void createProcess(int ProID, int Priority, int InTimes, int InstrucNum) {
		this.ProID = ProID;
		this.Priority = Priority;
		this.InTimes = CPU.getTime();
		this.InstrucNum = InstrucNum;
		this.RunTimes = 0;
		this.TurnTimes = 0;
		this.PSW = 2;
		this.timeSliceLeft = 0;
		this.RqNum = Rq.size();
		this.RqTimes = CPU.getTime();
		this.BqNum1 = -1;
		this.BqTimes1 = 0;
		this.BqNum2 = -1;
		this.BqTimes2 = 0;
		this.BqNum3 = -1;
		this.BqTimes3 = 0;
		this.PC = 0;             //������ָ���ļ���1��ʼ������½���PCB��pcҲӦ�ô�1��ʼ
		this.IR = 0;
		Rq.offer(this);    //�������̺�����������
		allPCB.add(this);
		PCBNum++;
		GUI.textArea.append("����״̬������"+ProID+"�Ѵ���������ʱ��Ϊ"+this.InTimes+"�����ȼ�Ϊ"+Priority+"��ָ������Ϊ"+InstrucNum+"\n");
} 
	
	/**
	*  ��������ԭ��  

	*/
	public synchronized void cancelProcess() {
		this.PSW = 0;
		this.TurnTimes = CPU.getTime() - this.InTimes + 1;
		allPCB.remove(this);
		Rq.remove(this); //�������Ľ���Ҫô��CPU������Ҫôϵͳ���ý����Ƚ�����������ٱ�����
		Process.getAllProcess().remove(findProcessWithPCB(this));
		GUI.textArea.append("����״̬������" + ProID + "��������ִ����" + InstrucNum + "��ָ�" + "����ʱ��Ϊ��" + RunTimes + "����ʱ��Ϊ��" + CPU.getTime() + ",��תʱ��Ϊ:" + TurnTimes + "\n");
		PCBNum--;
	}
	
	/**
	*  ��������ԭ�Ϊ�˱�֤�̰߳�ȫ�����ô�ԭ������
	*/
	public synchronized void blockProcess() { 
		this.PSW = 3;
		this.timeSliceLeft = 0;
		if(IR == 1) {            //����ir�����ݾ��������ĸ���������
			joinBlockedQueue1(this);  //���÷�װ�õĽ�����������1�ķ���
		}
		else if(IR == 3){         //irΪ3ʱΪϵͳ��������豸
			joinBlockedQueue2(this);
		} 
		else if(IR == 2) {       //irΪ2ʱΪPVͨ��
			joinBlockedQueue3(this);
		};
	}
	
	/**
	* ���ѽ���ԭ�Ϊ�˱�֤�̰߳�ȫ�����ô�ԭ������
	*/
	public synchronized void awakeProcess() {
		this.PSW = 2;                //���ݵ����㷨�������ѵ�ԭ��һ��������������Ķ�ͷ
		if(IR == 1) {
			this.BqNum1 = -1;
		}
		else if(IR == 3){
			this.BqNum2 = -1;
		}
		else if(IR == 2) {
			this.BqNum3 = -1;
		}
		joinReadyQueue(this);          //�����ѵĽ��̼����������
	}
	
	/**
	* ʵ��Comparable�ӿڣ�����ʹ��Collections.sort������PCB�б���������������ȼ�����С��Ӧ����ǰ��
	* ��һ�����Ƚϵ�PCB
	* ���ȼ����ִ󷵻�������С���ظ�������ȷ���0
	 */
	public int compareTo(PCB p) {
		if(this.Priority>p.Priority)
			return 1;
		else if(this.Priority<p.Priority)
			return -1;
		else
			return 0;
	}
	
	
	/**
	*  ��̬���ȼ��㷨�������������е�PCB�������ȼ�����   
	*/
	public synchronized static void staticPriority() {
		Collections.sort(Rq);
	}
	
	/**
	*  ���赱ǰ����ʱ��ƬΪ2   
	*/
	public void reSetTimeSlice() {
		this.timeSliceLeft = 2;
	}
	
	/**
	*  ���ʱ���ڴ˽���ռ����ʱ��Ƭ��ʣ��ʱ��Ƭ-1 
	*/
	public void useTimeSlice() {
		this.timeSliceLeft--;
	}
	
	/**
	*  ʱ��Ƭǿ����������µ��ô˺��� 
	*/
	public void setTimeSliceUseOut() {
		this.timeSliceLeft = 0;
	}
	
	/**
	* ����ʱ��Ƭ�Ƿ�����  
	*/
	public boolean ifTimeSliceLeft() {
		if(this.timeSliceLeft==0)
			return false;
		else {
			return true;
		}
	}
	
	/**
	* @Description: �ı�PC��ֵ
	* @param tempPC     
	*/
	public void setPC(int tempPC) {
		this.PC = tempPC;
	}
	
	/**
	* @Description: PC+1,���Ҽ������Ƿ�ִ���꣬�����������������������̣߳�
	*/
	public void interruptPlusPCAndCheckIfNeedToCancelTheProcess() {    
		if(PC < InstrucNum - 1)       
			this.PC++;
		else {
			this.cancelProcess();
		}
	}
	
	/**
	* @Description: PC+1,���Ҽ������Ƿ�ִ���꣬������������������CPU�� ���������Ƿ�Ӱ��cpu״̬
	*/
	public void cpuPlusPCAndCheckIfNeedToCancelTheProcess() {    
		if(PC < InstrucNum - 1)       
			this.PC++;
		else {
			this.cancelProcess();
			CPU.setCpuWorkState(false);  //һ�����̽�������������ʱ����CPU��������������
		}
	}
	
	/**
	* @Description: ��ȡ��ǰpcֵ
	* @return int    
	*/
	public int getPC() {
		return this.PC;
	}
	
	
	/**
	* @Description: �ı�ir��ֵ
	* @param tempIR     
	*/
	public void setIR(int tempIR) {
		this.IR = tempIR;
	}
	
	
	/**
	* @Description: ��ȡ��ǰirֵ
	* @return int    
	*/
	public int getIR() {
		return this.IR;
	}
	
	/**
	* @Description: ��ȡpsw״̬
	* @return int    
	*/
	public int getPSW() {
		return this.PSW;
	}
	
	/**
	* @Description: �趨psw״̬
	* @param tempPSW void    
	*/
	public void setPSW(int tempPSW) {
		this.PSW = tempPSW;
	}
	
	/**
	* @Description: ��̬���������̼����������
	* @param aPCB     
	*/
	public static void joinReadyQueue(PCB aPCB) {
		Rq.offer(aPCB);
		aPCB.setReadyQueueNum(Rq.indexOf(aPCB));
		aPCB.setReadyQueueInTime(CPU.getTime());
		PCB.staticPriority();  //�����ȼ���С�Ծ������н��������Ŷ�
	}
	
	/**
	* @Description: ��̬���������̼�����������1
	* @param aPCB     
	*/
	public static void joinBlockedQueue1(PCB aPCB) {
		Bq1.offer(aPCB);
		aPCB.setBlockedQueue1Num(Bq1.indexOf(aPCB));
		aPCB.setBlockedQueue1InTime(CPU.getTime());
	}
	
	/**
	* @Description: ��̬���������̼�����������2
	* @param aPCB     
	*/
	public static void joinBlockedQueue2(PCB aPCB) {
		Bq2.offer(aPCB);
		aPCB.setBlockedQueue2Num(Bq2.indexOf(aPCB));
		aPCB.setBlockedQueue2InTime(CPU.getTime());		
	}
	
	/**
	* @Description: ��̬���������̼�����������3
	* @param aPCB     
	*/
	public static void joinBlockedQueue3(PCB aPCB) {
		Bq3.offer(aPCB);
		aPCB.setBlockedQueue3Num(Bq3.indexOf(aPCB));
		aPCB.setBlockedQueue3InTime(CPU.getTime());
	}

	
	/**
	 * @return 
	* @Description: ��������ͷ�����ӣ��������̸������   
	*/
	public static PCB pollReadyQueue() {
		PCB pollPcb = Rq.poll();
		for(PCB e : Rq) {
			e.setReadyQueueNum(Rq.indexOf(e));
		}
		return pollPcb;
	}
	
	/**
	* @Description: ��������1ͷ�����ӣ��������̸������   
	*/
	public static PCB pollBlockedQueue1() {
		PCB pollPcb = Bq1.poll();
		for(PCB e : Bq1) {
			e.setBlockedQueue1Num(Bq1.indexOf(e));
		}
		return pollPcb;
	}
	
	/**
	* @Description: ��������2ͷ�����ӣ��������̸������   
	*/
	public static PCB pollBlockedQueue2() {
		PCB pollPcb = Bq2.poll();
		for(PCB e : Bq2) {
			e.setBlockedQueue2Num(Bq2.indexOf(e));
		}
		return pollPcb;
	}
	
	/**
	* @Description: ��������3ͷ�����ӣ��������̸������   
	*/
	public static PCB pollBlockedQueue3() {
		PCB pollPcb = Bq3.poll();
		for(PCB e : Bq3) {
			e.setBlockedQueue3Num(Bq3.indexOf(e));
		}
		return pollPcb;
	}

	
	/**
	* @Description: ���ý����������ʱ��
	* @param time    
	*/
	public void setReadyQueueInTime(int time) {
		this.RqTimes = time;
	}
	
	/**
	* @Description: ��ȡ�����������ʱ��
	* @return int   
	*/
	public int getReadyQueueInTime() {
		return this.RqTimes;
	}
	
	/**
	* @Description: �����ھ������е�λ��
	* @param num      
	*/
	public void setReadyQueueNum(int num) {
		this.RqNum = num;
	}
	
	/**
	* @Description: ��ȡ�ھ������е�λ��
	* @return int    
	*/
	public int getReadyQueueNum() {
		return this.RqNum;
	}
	/**
	* @Description: ���ý�����������1ʱ��
	* @param time    
	*/
	public void setBlockedQueue1InTime(int time) {
		this.BqTimes1 = time;
	}
	
	/**
	* @Description: ��ȡ������������1ʱ��
	* @return int   
	*/
	public int getBlockedQueue1InTime() {
		return this.BqTimes1;
	}
	
	/**
	* @Description: ��������������1��λ��
	* @param num      
	*/
	public void setBlockedQueue1Num(int num) {
		this.BqNum1 = num;
	}
	
	/**
	* @Description: ��ȡ����������1��λ��
	* @return int    
	*/
	public int getBlockedQueue1Num() {
		return this.BqNum1;
	}
	
	/**
	* @Description: ���ý�����������2ʱ��
	* @param time    
	*/
	public void setBlockedQueue2InTime(int time) {
		this.BqTimes2 = time;
	}
	
	/**
	* @Description: ��ȡ������������2ʱ��
	* @return int   
	*/
	public int getBlockedQueue2InTime() {
		return this.BqTimes2;
	}
	
	/**
	* @Description: ��������������2��λ��
	* @param num      
	*/
	public void setBlockedQueue2Num(int num) {
		this.BqNum2 = num;
	}
	
	/**
	* @Description: ��ȡ����������2��λ��
	* @return int    
	*/
	public int getBlockedQueue2Num() {
		return this.BqNum2;
	}
	
	/**
	* @Description: ���ý�����������3ʱ��
	* @param time    
	*/
	public void setBlockedQueue3InTime(int time) {
		this.BqTimes3 = time;
	}
	
	/**
	* @Description: ��ȡ������������3ʱ��
	* @return int   
	*/
	public int getBlockedQueue3InTime() {
		return this.BqTimes3;
	}
	
	/**
	* @Description: ��������������3��λ��
	* @param num      
	*/
	public void setBlockedQueue3Num(int num) {
		this.BqNum3 = num;
	}
	
	/**
	* @Description: ��ȡ����������3��λ��
	* @return int    
	*/
	public int getBlockedQueue3Num() {
		return this.BqNum3;
	}
	
	/**
	* @Description: ���ؾ������г���
	* @return int    
	*/
	public static int getReadyQueueLength() {
		return Rq.size();
	}
	
	/**
	* @Description: ������������1����
	* @return int    
	*/
	public static int getBlockedQueue1Length() {
		return Bq1.size();
	}
	
	/**
	* @Description: ������������21����
	* @return int    
	*/
	

	public static int getBlockedQueue2Length() {
		return Bq2.size();
	}
	
	/**
	* @Description: ������������3����
	* @return int    
	*/
	public static int getBlockedQueue3Length() {
		return Bq3.size();
	}
	
	/**
	* @Description: չʾ�������еĽ��̺�   
	*/
	public static void showReadyQueueIds() {
		for(PCB e :Rq) {
			GUI.textArea.append(String.valueOf(e.ProID) + " ");
		}
		GUI.textArea.append("\n\n");
	}
	
	/**
	* @Description: չʾ��������1��ĵĽ��̺�     
	*/
	public static void showBlockedQueue1Ids() {
		if(KeyBoard.getUsingProcess() != null)
		for(PCB e :Bq1) {
			GUI.textArea.append(String.valueOf(e.ProID) + " ");
		}
		GUI.textArea.append("\n");
	}
	
	/**
	* @Description: չʾ��������2��ĵĽ��̺�  
	*/
	public static void showBlockedQueue2Ids() {
		if(Display.getUsingProcess() != null)
		for(PCB e :Bq2) {
			GUI.textArea.append(String.valueOf(e.ProID) + " ");
		}
		GUI.textArea.append("\n");
	}
	
	/**
	* @Description: չʾ��������3��ĵĽ��̺�    
	*/
	public static void showBlockedQueue3Ids() {
		if(PV.getUsingProcess() != null)
		for(PCB e :Bq3) {
			GUI.textArea.append(String.valueOf(e.ProID) + " ");
		}
		GUI.textArea.append("\n");
	}
	/**
	* @Description: Ϊ������CPU����ʱ�����
	*/
	public void plusProcessRunTime() {
		this.RunTimes++;
	}
	
	
	/**
	* @Description: ��ȡ����ID
	* @return int    
	*/
	public int getID() {
		return this.ProID;
	}
	
	/**
	* @Description: ����PCBȷ�����̣�PCB����̾���һһ��Ӧ��ϵ��
	* @param pcb        �����ҵ�PCB
	* @return Process    �ҵ��Ķ�Ӧ�Ľ��̻��߿յ�ַ
	*/
	public static Process findProcessWithPCB(PCB pcb) {	
		if(pcb == null)
			return null;
		for(Process e : Process.getAllProcess()) {
			if(pcb.getID() == e.getID())
				return e;
		}
		return null;
	}


	/**
	 * @return the endTimes
	 */
	public int getEndTimes() {
		return EndTimes;
	}


	/**
	 * @param endTimes the endTimes to set
	 */
	public void setEndTimes(int endTimes) {
		this.EndTimes = endTimes;
	}


	/**
	 * @return the runTimes
	 */
	public int getRunTimes() {
		return RunTimes;
	}


	/**
	 * @param runTimes the runTimes to set
	 */
	public void setRunTimes(int runTimes) {
		this.RunTimes = runTimes;
	}


	/**
	 * @return the timeSliceLeft
	 */
	
	
	public int getTimeSliceLeft() {
		return timeSliceLeft;
	}


	/**
	 * @param timeSliceLeft the timeSliceLeft to set
	 */
	public void setTimeSliceLeft(int timeSliceLeft) {
		this.timeSliceLeft = timeSliceLeft;
	}

	/**
	 * @return the pCBNum
	 */
	public static int getPCBNum() {
		return PCBNum;
	}

	/**
	 * @param pCBNum the pCBNum to set
	 */
	public static void setPCBNum(int pCBNum) {
		PCBNum = pCBNum;
	}

}
