package com.java;
/**
 * @Descriptio ģ����̽��̣���java��һ���߳�ģ��
 *
 */
public class KeyBoard extends Thread {
	private static boolean ifKeyboardWork = false; //��ʾ���������߳�״̬��falseΪ���У�trueΪæµ
    private static Process usingProcess = null;    //���ڵȴ����̽�����߳�
	private static int lastUseTime = 0;            //���������ȴ����߳��Ѿ����˶��
	public void run() {
		while(true) {
			GUI.timerLock.lock();//������
			try {
				GUI.timerCondition.await();        //�ȵ�ʱ�ӽ��̷���ʱ���жϣ��ٿ�ʼִ���������
				doWhatKeyBoardDoEverySecond();                   //ִ��ÿ����̱߳����Ѻ��ִ�еĳ���   
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
			finally{
				GUI.timerLock.unlock();//�ͷ���
			}
			
		}
	}
	
	/**
	* @Description: ���ݲ�ͬ��״̬��ִ��ÿ�δ��̱߳����Ѻ��ִ�еĳ���   
	* @throws
	*/
	private void doWhatKeyBoardDoEverySecond() {
		if(ifKeyboardWork && CPU.getTime()-lastUseTime==5) {   //�����ж���Ҫ4�룬�����������Ϊ�գ�����ָ����һ��Ҳ����㣬��˴˴���ӦΪ5��
			usingProcess.awakeProcess();   //ϵͳ���ý��������Ѵ˽���
			usingProcess.interruptPlusPCAndCheckIfNeedToCancelTheProcess();   //�˽��̵�pcָ����һ��
			usingProcess = PCB.findProcessWithPCB(PCB.pollBlockedQueue1());  //�������������������̣�����з��ض�ͷ�����û�з��ؿյ�ַ
			if(usingProcess != null)                 //����������в��ն�ͷ�õ����̣��������
				lastUseTime = CPU.getTime()-1;       //1����Ϊ�������к������̻�ü��̵Ĳ���
			else
				ifKeyboardWork = false;
		}
		if(!ifKeyboardWork){                    //���ݼ��̵�״̬�����Ϣ
			GUI.textArea.append("����״̬���޽�������" + "\n");
		}
		else {
			GUI.textArea.append("����״̬������" + usingProcess.getID() + "�����������л���" + PCB.getBlockedQueue1Length() + "������:");
			PCB.showBlockedQueue1Ids();
		}
	}
	
	
	/**
	* @Description: ��̬���ؼ���״̬
	* @return boolean    
	* @throws
	*/
	public static boolean getKeyBoardState() {
		return ifKeyboardWork;
	}
	/**
	* @Description: ���ü���״̬
	* @param state     
	* @throws
	*/
	public static void setKeyBoardState(boolean state) {
		ifKeyboardWork = state;
	}
	
	/**
	* @Description: ��һ�����̵õ����̺�ִ�еĲ���
	* @param p    Ҫ���ü��̵Ľ���
	* @throws
	*/
	public static void setKeyBoardWorkForAProcess(Process p) {
		ifKeyboardWork = true;
		usingProcess = p;
		p.setPSW(3);
		lastUseTime = CPU.getTime();
	}

	/**
	 * @return the usingProcess
	 */
	public static Process getUsingProcess() {
		return usingProcess;
	}

	/**
	 * @param usingProcess the usingProcess to set
	 */
	public static void setUsingProcess(Process usingProcess) {
		KeyBoard.usingProcess = usingProcess;
	}

	/**
	 * @return the lastUseTime
	 */
	public static int getLastUseTime() {
		return lastUseTime;
	}

	/**
	 * @param lastUseTime the lastUseTime to set
	 */
	public static void setLastUseTime(int lastUseTime) {
		KeyBoard.lastUseTime = lastUseTime;
	}
}