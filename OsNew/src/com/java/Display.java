package com.java;
/**
 * @Description ��ʾ���࣬��һ���߳�ģ�⣬��Ҫ�ṹ��ע�ͼ�������
 *
 */
public class Display extends Thread {
	private static boolean ifDisplayWork = false; //��ʾ���������߳�״̬��falseΪ���У�trueΪæµ
    private static Process usingProcess = null;
	private static int lastUseTime = 0;

	public void run() {
		while(true) {
			GUI.timerLock.lock();//������
			try {
				GUI.timerCondition.await();
				doWhatDisplayDoEverySecond();
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
	private void doWhatDisplayDoEverySecond() {
		if(ifDisplayWork && CPU.getTime()-lastUseTime==4) {
			usingProcess.awakeProcess();   //ϵͳ���ý��������Ѵ˽���
			usingProcess.interruptPlusPCAndCheckIfNeedToCancelTheProcess();   //�˽��̵�pcָ����һ��
			usingProcess = PCB.findProcessWithPCB(PCB.pollBlockedQueue2());
			if(usingProcess != null)
				lastUseTime = CPU.getTime()-1;
			else
				ifDisplayWork = false;
		}
		if(!ifDisplayWork){
			GUI.textArea.append("��ʾ��״̬�� �޽�������" + "\n");
		}
		else {
			GUI.textArea.append("��ʾ��״̬������" + usingProcess.getID() + "�����������л���" + PCB.getBlockedQueue2Length() + "�����̣�");
			PCB.showBlockedQueue2Ids();
		} 
	}
	
	
	
	/**
	* @Description: ��̬������ʾ��״̬
	* @return boolean    
	* @throws
	*/
	public static boolean getDisplayState() {
		return ifDisplayWork;
	}
	
	public static void setDisplayWork(Process p) {
		ifDisplayWork = true;
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
		Display.usingProcess = usingProcess;
	}
}

