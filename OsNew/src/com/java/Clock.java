package com.java;

/**
 * @Description ÿ�뷢��һ���ж��źŵļ�ʱ���߳�
 * @author �¾�
 *
 */
public class Clock extends Thread{
	private static boolean suspend = false;   //����ʱ���Ƿ�����
	private static String control = ""; // ֻ����Ҫһ��������ѣ��������û��ʵ������
	
	/**
	 * @param ifTimerSuspend the ifTimerSuspend to set
	 */
	public static void setsuspend(boolean suspend) {
		if (!suspend) {  
            synchronized (control) {  
                control.notifyAll();  
            }  
        }  
		Clock.suspend = suspend;
	}
	
	/**
	 * @return the ifTimerSuspend
	 */
	public static boolean issuspend() {
		return Clock.suspend;
	}


	
	/**
	 *  �̼߳����ÿ�뷢��һ���ж��ź�
	 */
	public void run() {
		while(true) {
			if(suspend){    //�����GUI�İ�ť�ֶ���ͣ�ˣ�ʱ�Ӳ������źţ�ÿһ�����Ƿ��ڱ���ͣ
				try {
					Thread.sleep(1000);//�߳�˯��1��
				}
				catch (InterruptedException e) {//����߳���˯��״̬���жϣ������׳�IterruptedException�ж��쳣��
					e.printStackTrace();
				}
				continue;
		}
			
			GUI.timerLock.lock();//������
			try {
				GUI.textArea.append("CPUʱ�䣺" + CPU.getTime() + "\n");
				GUI.timerCondition.signalAll();//�����������м����߳�
				
			}
			finally{
				GUI.timerLock.unlock();//�ͷ���
			}
			try {
				Thread.sleep(1000);//��������1�룬ģ��ʱ�ӹ�ȥ��1��
				GUI.textArea.setCaretPosition(GUI.textArea.getText().length());   //Ϊ����GUI���ı����������
				CPU.passTime();// ����CPU��ȥ��1�� 
	
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
        }               
	}

}
