package com.java;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;


/**
 * @Description PCB类
 *
 */
public class PCB implements Comparable<PCB>{
	private int ProID; //进程编号,值为1,2,3,4,5,6...
	private int Priority; //进程优先数
	private int InTimes; //进程创建时间
	private int EndTimes; //进程结束时间
	private int PSW;      //进程状态 0为未知 1为运行 2为就绪 3为阻塞
	private int RunTimes; //进程运行时间列表
	private int TurnTimes; //进程周转时间统计
	private int InstrucNum; //进程中包含的指令数目
	private int PC;    	   //程序计数器信息，记录下一条指令地址
	private int IR;        //指令寄存器信息，记录当前执行的指令类型
	
	private int timeSliceLeft;    //当前进程在cpu运行的剩余时间片，若非运行态则为0
	private int RqNum;      //就绪队列位置编号
	private int RqTimes;    //就绪队列进入时间
	private int BqNum1;		//阻塞队列1位置编号
	private int BqTimes1;	//阻塞队列1进入时间
	private int BqNum2;		//阻塞队列2位置编号
	private int BqTimes2;	//阻塞队列2进入时间
	private int BqNum3;		//阻塞队列3位置编号
	private int BqTimes3;	//阻塞队列3进入时间
	
	private static LinkedList<PCB> Rq = new LinkedList<PCB>();	//就绪队列  使用静态变量，所有PCB共享四个队列
	private static LinkedList<PCB> Bq1 = new LinkedList<PCB>();	//阻塞队列1  java中Linkedlist实现了Queue接口，可以当队列使用
	private static LinkedList<PCB> Bq2 = new LinkedList<PCB>();	//阻塞队列2
	private static LinkedList<PCB> Bq3 = new LinkedList<PCB>(); 	//阻塞队列3
	private static ArrayList<PCB> allPCB = new ArrayList<>();    //PCB池
	private static int PCBNum = 0;                //PCB池中PCB数量
	
	/**
	*  PCB的构造函数 
	*/
	public PCB(int ProID, int Priority, int InTimes, int InstrucNum) {
		while(!isValidPCB(ProID))    //遇到重复的进程ProID，给ProID加1直至不与创建过的PCB的id重复。
			ProID++;
		createProcess(ProID, Priority, InTimes, InstrucNum);
	} 
	
	/**
	*  检查一个进程ProID是否与其他存在过的进程重复，重复返回false
	*/
	public boolean isValidPCB(int ProID) {
		for(PCB e : allPCB) {
			if(e.getID() == ProID)
				return false;
		}
		return true;
	}
	
	
	/**
	*  创建进程原语
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
		this.PC = 0;             //给定的指令文件从1开始，因此新建的PCB的pc也应该从1开始
		this.IR = 0;
		Rq.offer(this);    //创建进程后进入就绪队列
		allPCB.add(this);
		PCBNum++;
		GUI.textArea.append("进程状态：进程"+ProID+"已创建，进入时间为"+this.InTimes+"，优先级为"+Priority+"，指令数量为"+InstrucNum+"\n");
} 
	
	/**
	*  撤销进程原语  

	*/
	public synchronized void cancelProcess() {
		this.PSW = 0;
		this.TurnTimes = CPU.getTime() - this.InTimes + 1;
		allPCB.remove(this);
		Rq.remove(this); //被撤销的进程要么在CPU被撤销要么系统调用结束先进入就绪队列再被撤销
		Process.getAllProcess().remove(findProcessWithPCB(this));
		GUI.textArea.append("进程状态：进程" + ProID + "被撤销，执行了" + InstrucNum + "条指令。" + "运行时间为：" + RunTimes + "撤销时间为：" + CPU.getTime() + ",周转时间为:" + TurnTimes + "\n");
		PCBNum--;
	}
	
	/**
	*  阻塞进程原语，为了保证线程安全，调用此原语会加锁
	*/
	public synchronized void blockProcess() { 
		this.PSW = 3;
		this.timeSliceLeft = 0;
		if(IR == 1) {            //根据ir的内容决定进入哪个阻塞队列
			joinBlockedQueue1(this);  //调用封装好的进入阻塞队列1的方法
		}
		else if(IR == 3){         //ir为3时为系统调用输出设备
			joinBlockedQueue2(this);
		} 
		else if(IR == 2) {       //ir为2时为PV通信
			joinBlockedQueue3(this);
		};
	}
	
	/**
	* 唤醒进程原语，为了保证线程安全，调用此原语会加锁
	*/
	public synchronized void awakeProcess() {
		this.PSW = 2;                //根据调度算法，被唤醒的原语一定是阻塞队列里的队头
		if(IR == 1) {
			this.BqNum1 = -1;
		}
		else if(IR == 3){
			this.BqNum2 = -1;
		}
		else if(IR == 2) {
			this.BqNum3 = -1;
		}
		joinReadyQueue(this);          //被唤醒的进程加入就绪队列
	}
	
	/**
	* 实现Comparable接口，方便使用Collections.sort函数对PCB列表进行排序，其中优先级数字小着应排在前面
	* 另一个被比较的PCB
	* 优先级数字大返回正数，小返回负数，相等返回0
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
	*  静态优先级算法，将就绪队列中的PCB按照优先级排序   
	*/
	public synchronized static void staticPriority() {
		Collections.sort(Rq);
	}
	
	/**
	*  重设当前进程时间片为2   
	*/
	public void reSetTimeSlice() {
		this.timeSliceLeft = 2;
	}
	
	/**
	*  这个时钟内此进程占用了时间片，剩余时间片-1 
	*/
	public void useTimeSlice() {
		this.timeSliceLeft--;
	}
	
	/**
	*  时间片强制用完情况下调用此函数 
	*/
	public void setTimeSliceUseOut() {
		this.timeSliceLeft = 0;
	}
	
	/**
	* 返回时间片是否用完  
	*/
	public boolean ifTimeSliceLeft() {
		if(this.timeSliceLeft==0)
			return false;
		else {
			return true;
		}
	}
	
	/**
	* @Description: 改变PC的值
	* @param tempPC     
	*/
	public void setPC(int tempPC) {
		this.PC = tempPC;
	}
	
	/**
	* @Description: PC+1,并且检查进程是否执行完，是则撤销（此请求来自其他线程）
	*/
	public void interruptPlusPCAndCheckIfNeedToCancelTheProcess() {    
		if(PC < InstrucNum - 1)       
			this.PC++;
		else {
			this.cancelProcess();
		}
	}
	
	/**
	* @Description: PC+1,并且检查进程是否执行完，是则撤销（此请求来自CPU） 区别在于是否影响cpu状态
	*/
	public void cpuPlusPCAndCheckIfNeedToCancelTheProcess() {    
		if(PC < InstrucNum - 1)       
			this.PC++;
		else {
			this.cancelProcess();
			CPU.setCpuWorkState(false);  //一个进程结束被撤销，短时间内CPU可以视作不工作
		}
	}
	
	/**
	* @Description: 获取当前pc值
	* @return int    
	*/
	public int getPC() {
		return this.PC;
	}
	
	
	/**
	* @Description: 改变ir的值
	* @param tempIR     
	*/
	public void setIR(int tempIR) {
		this.IR = tempIR;
	}
	
	
	/**
	* @Description: 获取当前ir值
	* @return int    
	*/
	public int getIR() {
		return this.IR;
	}
	
	/**
	* @Description: 获取psw状态
	* @return int    
	*/
	public int getPSW() {
		return this.PSW;
	}
	
	/**
	* @Description: 设定psw状态
	* @param tempPSW void    
	*/
	public void setPSW(int tempPSW) {
		this.PSW = tempPSW;
	}
	
	/**
	* @Description: 静态方法，进程加入就绪队列
	* @param aPCB     
	*/
	public static void joinReadyQueue(PCB aPCB) {
		Rq.offer(aPCB);
		aPCB.setReadyQueueNum(Rq.indexOf(aPCB));
		aPCB.setReadyQueueInTime(CPU.getTime());
		PCB.staticPriority();  //按优先级大小对就绪队列进行重新排队
	}
	
	/**
	* @Description: 静态方法，进程加入阻塞队列1
	* @param aPCB     
	*/
	public static void joinBlockedQueue1(PCB aPCB) {
		Bq1.offer(aPCB);
		aPCB.setBlockedQueue1Num(Bq1.indexOf(aPCB));
		aPCB.setBlockedQueue1InTime(CPU.getTime());
	}
	
	/**
	* @Description: 静态方法，进程加入阻塞队列2
	* @param aPCB     
	*/
	public static void joinBlockedQueue2(PCB aPCB) {
		Bq2.offer(aPCB);
		aPCB.setBlockedQueue2Num(Bq2.indexOf(aPCB));
		aPCB.setBlockedQueue2InTime(CPU.getTime());		
	}
	
	/**
	* @Description: 静态方法，进程加入阻塞队列3
	* @param aPCB     
	*/
	public static void joinBlockedQueue3(PCB aPCB) {
		Bq3.offer(aPCB);
		aPCB.setBlockedQueue3Num(Bq3.indexOf(aPCB));
		aPCB.setBlockedQueue3InTime(CPU.getTime());
	}

	
	/**
	 * @return 
	* @Description: 就绪队列头部出队，其他进程更新序号   
	*/
	public static PCB pollReadyQueue() {
		PCB pollPcb = Rq.poll();
		for(PCB e : Rq) {
			e.setReadyQueueNum(Rq.indexOf(e));
		}
		return pollPcb;
	}
	
	/**
	* @Description: 阻塞队列1头部出队，其他进程更新序号   
	*/
	public static PCB pollBlockedQueue1() {
		PCB pollPcb = Bq1.poll();
		for(PCB e : Bq1) {
			e.setBlockedQueue1Num(Bq1.indexOf(e));
		}
		return pollPcb;
	}
	
	/**
	* @Description: 阻塞队列2头部出队，其他进程更新序号   
	*/
	public static PCB pollBlockedQueue2() {
		PCB pollPcb = Bq2.poll();
		for(PCB e : Bq2) {
			e.setBlockedQueue2Num(Bq2.indexOf(e));
		}
		return pollPcb;
	}
	
	/**
	* @Description: 阻塞队列3头部出队，其他进程更新序号   
	*/
	public static PCB pollBlockedQueue3() {
		PCB pollPcb = Bq3.poll();
		for(PCB e : Bq3) {
			e.setBlockedQueue3Num(Bq3.indexOf(e));
		}
		return pollPcb;
	}

	
	/**
	* @Description: 设置进入就绪队列时间
	* @param time    
	*/
	public void setReadyQueueInTime(int time) {
		this.RqTimes = time;
	}
	
	/**
	* @Description: 获取进入就绪队列时间
	* @return int   
	*/
	public int getReadyQueueInTime() {
		return this.RqTimes;
	}
	
	/**
	* @Description: 设置在就绪队列的位置
	* @param num      
	*/
	public void setReadyQueueNum(int num) {
		this.RqNum = num;
	}
	
	/**
	* @Description: 获取在就绪队列的位置
	* @return int    
	*/
	public int getReadyQueueNum() {
		return this.RqNum;
	}
	/**
	* @Description: 设置进入阻塞队列1时间
	* @param time    
	*/
	public void setBlockedQueue1InTime(int time) {
		this.BqTimes1 = time;
	}
	
	/**
	* @Description: 获取进入阻塞队列1时间
	* @return int   
	*/
	public int getBlockedQueue1InTime() {
		return this.BqTimes1;
	}
	
	/**
	* @Description: 设置在阻塞队列1的位置
	* @param num      
	*/
	public void setBlockedQueue1Num(int num) {
		this.BqNum1 = num;
	}
	
	/**
	* @Description: 获取在阻塞队列1的位置
	* @return int    
	*/
	public int getBlockedQueue1Num() {
		return this.BqNum1;
	}
	
	/**
	* @Description: 设置进入阻塞队列2时间
	* @param time    
	*/
	public void setBlockedQueue2InTime(int time) {
		this.BqTimes2 = time;
	}
	
	/**
	* @Description: 获取进入阻塞队列2时间
	* @return int   
	*/
	public int getBlockedQueue2InTime() {
		return this.BqTimes2;
	}
	
	/**
	* @Description: 设置在阻塞队列2的位置
	* @param num      
	*/
	public void setBlockedQueue2Num(int num) {
		this.BqNum2 = num;
	}
	
	/**
	* @Description: 获取在阻塞队列2的位置
	* @return int    
	*/
	public int getBlockedQueue2Num() {
		return this.BqNum2;
	}
	
	/**
	* @Description: 设置进入阻塞队列3时间
	* @param time    
	*/
	public void setBlockedQueue3InTime(int time) {
		this.BqTimes3 = time;
	}
	
	/**
	* @Description: 获取进入阻塞队列3时间
	* @return int   
	*/
	public int getBlockedQueue3InTime() {
		return this.BqTimes3;
	}
	
	/**
	* @Description: 设置在阻塞队列3的位置
	* @param num      
	*/
	public void setBlockedQueue3Num(int num) {
		this.BqNum3 = num;
	}
	
	/**
	* @Description: 获取在阻塞队列3的位置
	* @return int    
	*/
	public int getBlockedQueue3Num() {
		return this.BqNum3;
	}
	
	/**
	* @Description: 返回就绪队列长度
	* @return int    
	*/
	public static int getReadyQueueLength() {
		return Rq.size();
	}
	
	/**
	* @Description: 返回阻塞队列1长度
	* @return int    
	*/
	public static int getBlockedQueue1Length() {
		return Bq1.size();
	}
	
	/**
	* @Description: 返回阻塞队列21长度
	* @return int    
	*/
	

	public static int getBlockedQueue2Length() {
		return Bq2.size();
	}
	
	/**
	* @Description: 返回阻塞队列3长度
	* @return int    
	*/
	public static int getBlockedQueue3Length() {
		return Bq3.size();
	}
	
	/**
	* @Description: 展示就绪队列的进程号   
	*/
	public static void showReadyQueueIds() {
		for(PCB e :Rq) {
			GUI.textArea.append(String.valueOf(e.ProID) + " ");
		}
		GUI.textArea.append("\n\n");
	}
	
	/**
	* @Description: 展示阻塞队列1里的的进程号     
	*/
	public static void showBlockedQueue1Ids() {
		if(KeyBoard.getUsingProcess() != null)
		for(PCB e :Bq1) {
			GUI.textArea.append(String.valueOf(e.ProID) + " ");
		}
		GUI.textArea.append("\n");
	}
	
	/**
	* @Description: 展示阻塞队列2里的的进程号  
	*/
	public static void showBlockedQueue2Ids() {
		if(Display.getUsingProcess() != null)
		for(PCB e :Bq2) {
			GUI.textArea.append(String.valueOf(e.ProID) + " ");
		}
		GUI.textArea.append("\n");
	}
	
	/**
	* @Description: 展示阻塞队列3里的的进程号    
	*/
	public static void showBlockedQueue3Ids() {
		if(PV.getUsingProcess() != null)
		for(PCB e :Bq3) {
			GUI.textArea.append(String.valueOf(e.ProID) + " ");
		}
		GUI.textArea.append("\n");
	}
	/**
	* @Description: 为进程在CPU运行时间计数
	*/
	public void plusProcessRunTime() {
		this.RunTimes++;
	}
	
	
	/**
	* @Description: 获取进程ID
	* @return int    
	*/
	public int getID() {
		return this.ProID;
	}
	
	/**
	* @Description: 根据PCB确定进程（PCB与进程具有一一对应关系）
	* @param pcb        被查找的PCB
	* @return Process    找到的对应的进程或者空地址
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
