package com.java;

import java.util.ArrayList;

/**
 * @Description ���̵�����
 *
 */
public class ProcessSchedule extends Thread{

	/**
	 *  ���̵����౻���Ѻ�ִ�еĳ���
	 */
	public void run() {
		
		String fileName = "E:/input/11519102-jobs-input.txt";
		ArrayList<int[]> readFromProcessFile = IO.readProcessFromFile(fileName);

		while(true) {                  	
			GUI.timerLock.lock();//������
            try{
                GUI.timerCondition.await();  //�������еȴ��߳�
            }			
            catch (InterruptedException e) {
				e.printStackTrace();
			}
            finally{
            	GUI.timerLock.unlock();//�ͷ���
            }
			if(CPU.getTime()%5 == 0)            //ÿ5�����Ƿ�������ҵ
				analyseWhichToCreate(readFromProcessFile);
			roundRobinScheduling();       //ִ��ʱ��Ƭ��ת�㷨��һϵ�в���						
		}
		
	}
	
	/**
	* @Description: ʱ��Ƭ��ת�㷨   
	*/
	public void roundRobinScheduling() {
		PCB.staticPriority();  //�����ȼ���С�Ծ������н��������Ŷ�
		if(CPU.getCpuWorkState() == true) {
			if(CPU.workingProcess.ifTimeSliceLeft()) {   //����������еĽ���ʱ��Ƭ����ʣ�࣬��ôһ��ʱ���ж������ڴ˽��̼�������
				CPU.doInstruction();             //���ݲ�ͬ��ָ��ִ�ж�Ӧ�Ĳ���
			}
			else {                                  //ʱ��Ƭ�������˽����Ƶ����������Ŷӣ����н����������л����ٴӾ�������ȡ�����ȼ���ߵĽ���ִ��
				CPU.workingProcess.setPSW(2);       
				CPU.workingProcess.setReadyQueueInTime(CPU.getTime());
				PCB.joinReadyQueue(CPU.workingProcess);                                 //��ǰ���̽����������
				PCB.staticPriority();  //�����ȼ���С�Ծ������н��������Ŷ�
				processContextSwitch(PCB.findProcessWithPCB(PCB.pollReadyQueue()));   //���н����������л�
				CPU.workingProcess.reSetTimeSlice(); 
				CPU.doInstruction();            //���ݲ�ͬ��ָ��ִ�ж�Ӧ�Ĳ���
			}
		}
		else {
			PCB readyPcb = PCB.pollReadyQueue();      //���CPU�˿̲��������ʹӾ���������λȡԪ��,��������Ϊ�ջ᷵��һ���յ�ַ
			if(readyPcb == null) {                     //����������пգ���ӡCPU����״̬
				GUI.textArea.append("CPU����\n\n");
			}
			else {                                 //�������в��գ����н����������л����ٴӾ�������ȡ�����ȼ���ߵĽ���ִ��			
				processContextSwitch(PCB.findProcessWithPCB(readyPcb));
				CPU.setCpuWorkState(true);       //��⵽�˻���ָ��û���꣬CPU״̬��Ϊwork
				CPU.workingProcess.reSetTimeSlice();
				CPU.doInstruction();            //���ݲ�ͬ��ָ��ִ�ж�Ӧ�Ĳ���
			}
		}
	}
	
	/**
	* @Description: �����������л�,��CPU���ֳ��ĳ��½��̵��ֳ�,   �����޸��½��ɵ�״̬
	* @throws
	*/
	public void processContextSwitch(Process newRunProcess) {
		CPU.workingProcess = PCB.findProcessWithPCB(newRunProcess);
		CPU.switchUserModeToKernelMode();       //�����������л���Ҫ��CPU����̬��ʵ�ֵ�
		newRunProcess.setPSW(1);
		CPU.workingProcess = newRunProcess;
		CPU.switchKernelModeToUserMode();
		CPU.setPC(newRunProcess.getPC());
		CPU.setIR(newRunProcess.getIR());
		CPU.setPSW(newRunProcess.getPSW());
	}
	
	
	/**
	* @Description: ��鵱ǰʱ���Ƿ����½��̲���
	* @param readFromProcessFile   ���ļ��ж������йؽ��̵�����  
	* @throws
	*/
	public void analyseWhichToCreate(ArrayList<int[]> readFromProcessFile) {
		int time = CPU.getTime();
		for(int[] e:readFromProcessFile) {
			if(e[2] > time-5 && e[2] <= time)
				{
					String id = Integer.toString(e[0]);
					id = "E:/input/" + id + ".txt";    //���ݽ���id��ȥ��������̶�Ӧ��ָ����ļ�
					ArrayList<int[]> readFromInstructionFile = IO.readInstructionFromFile(id);
					createTask(e,readFromInstructionFile);
				}
		}
	}
	
	/**
	* @Description: ����һ���½���
	* @param infomationPCB   Ҫ�����Ľ��̵���Ϣ 
	*/
	public void createTask(int[] infomationPCB, ArrayList<int[]> readFromInstructionFile) {
		ArrayList<Instruction> instructionSegment = new ArrayList<>(); 
		for(int[] e : readFromInstructionFile) {     //�����ļ��������������ɳ����
			Instruction instruction = new Instruction(e[0], e[1]);
			instructionSegment.add(instruction);
		}
		new Process(infomationPCB[0], infomationPCB[1], infomationPCB[2], infomationPCB[3], instructionSegment);  //�����½���
	}
}