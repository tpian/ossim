package com.java;

/**
 * @Description 每秒发送一次中断信号的计时器线程
 * @author 陈军
 *
 */
public class Clock extends Thread{
	private static boolean suspend = false;   //控制时钟是否运行
	private static String control = ""; // 只是需要一个对象而已，这个对象没有实际意义
	
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
	 *  线程激活后，每秒发送一次中断信号
	 */
	public void run() {
		while(true) {
			if(suspend){    //如果被GUI的按钮手动暂停了，时钟不发送信号，每一秒检查是否还在被暂停
				try {
					Thread.sleep(1000);//线程睡眠1秒
				}
				catch (InterruptedException e) {//如果线程在睡眠状态被中断，将会抛出IterruptedException中断异常。
					e.printStackTrace();
				}
				continue;
		}
			
			GUI.timerLock.lock();//请求锁
			try {
				GUI.textArea.append("CPU时间：" + CPU.getTime() + "\n");
				GUI.timerCondition.signalAll();//唤醒其他所有加锁线程
				
			}
			finally{
				GUI.timerLock.unlock();//释放锁
			}
			try {
				Thread.sleep(1000);//进程休眠1秒，模拟时钟过去了1秒
				GUI.textArea.setCaretPosition(GUI.textArea.getText().length());   //为了让GUI的文本框滚动起来
				CPU.passTime();// 设置CPU过去了1秒 
	
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
        }               
	}

}
