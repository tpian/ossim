package com.java;

import java.util.ArrayList;
import java.util.Stack;

/**
 * @Description �����࣬�̳���PCB�࣬�ڴ˻����������˳���κ����ݶΡ�
 * 				ʹ�ü̳ж����ǽ�PCB����Ϊ�����Կ������Ӵ��븴����
 * 				Process��ʵ��ʹ�ÿ���ԭ��ʱ������ø���PCB�ķ���
 *				
 */
public class Process extends PCB {
    private static ArrayList<Process> allProcess = new ArrayList<>();  //��̬��������������һ���㹻����ڴ�ռ䣬�洢����PCB������κ����ݶ� 
	private ArrayList<Instruction> instructionSegments;   //����� java����ַ���ݣ�Process
	private Stack<Integer> userStack;    //�û�ջ
	private Stack<Integer> coreStack;	//����ջ
	
	
	/**
	* @Description: ������Ĺ��캯��  
	*/
	public Process(int ProID, int Priority, int inTimes, int instrNum, ArrayList<Instruction> segment) {
		super(ProID, Priority, inTimes, instrNum);   //��������PCB
		this.instructionSegments = segment;       //�������� �û�ջ�ͺ���ջ
		userStack = new Stack<>();
		coreStack = new Stack<>();
		getAllProcess().add(this);       
	}

	
	
	/**
	* ��IR���ó�PCָ�����εĵ�ָ�������Ϣ��
	*/
	public void setIRNewInstructionState() {
			this.setIR(instructionSegments.get(getPC()).getInstructionState());
	}
	
	/**
	* @Description: ��ȡ��ǰ���̵�pcָ��ĳ���εľ���ָ��
	* @return  int
	*/
	public int getCurrentInstructionID() {
		return instructionSegments.get(getPC()).getInstructionID();
	}
	
	/**
	* @Description: ��ӡҪִ�еĳ����ۺϣ������ã�    
	*/
	public void showInstructionSegment() {
		for(Instruction e:instructionSegments) {
			System.out.println(e);
		}	
	}
	
	/**
	* @Description: ���ݽ������ջ
	* @param register     
	*/
	public void inCoreStack(int register) {
		coreStack.push(register);
	}
	
	/**
	* @Description: ����ջ��ջ
	* @return int    
	*/
	public int outCoreStack() {
		return coreStack.pop();
	}



	/**
	 * @return the allProcess
	 */
	public static ArrayList<Process> getAllProcess() {
		return allProcess;
	}



	/**
	 * @return the userStack
	 */
	public Stack<Integer> getUserStack() {
		return userStack;
	}



	/**
	 * @param userStack the userStack to set
	 */
	public void setUserStack(Stack<Integer> userStack) {
		this.userStack = userStack;
	}
	
}