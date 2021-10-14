package com.java;
/**
 * @Description PVͨ���࣬��һ���߳�ģ�⣬��Ҫ�ṹ��ע�ͼ�������
 */
public class PV extends Thread {
	private static boolean ifPVWork = false; //��ʾ���������߳�״̬��falseΪ���У�trueΪæµ
    private static Process usingProcess = null;
	private static int lastUseTime = 0;

	public void run() {
		while(true) {
			GUI.timerLock.lock();//������
			try {
				GUI.timerCondition.await();
				doWhatPVDoEverySecond();
				
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
	*/
	private void doWhatPVDoEverySecond() {
		if(ifPVWork && CPU.getTime()-lastUseTime==3) {
			usingProcess.awakeProcess();   //PV���ý��������Ѵ˽���
			usingProcess.interruptPlusPCAndCheckIfNeedToCancelTheProcess();   //�˽��̵�pcָ����һ��
			usingProcess = PCB.findProcessWithPCB(PCB.pollBlockedQueue3());
			
			if(usingProcess != null)
				lastUseTime = CPU.getTime()-1;
			else
				ifPVWork = false;
		}
		if(!ifPVWork) {
			GUI.textArea.append("PV״̬���޽�������" + "\n");
		}
		else{
			GUI.textArea.append("PV״̬������" + usingProcess.getID() + "�����������л���"+ PCB.getBlockedQueue3Length() + "�����̵ȴ�");
			PCB.showBlockedQueue3Ids();
		}
	}
	
	/**
	* @Description: ��̬����PV״̬
	* @return boolean    
	*/
	public static boolean getPVState() {
		return ifPVWork;
	}
	
	public static void setPVWork(Process p) {
		ifPVWork = true;
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
		PV.usingProcess = usingProcess;
	}
}
