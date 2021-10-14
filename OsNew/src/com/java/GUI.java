package com.java;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;



import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import java.awt.GridLayout;

import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.awt.event.ActionEvent;
import java.awt.Font;


/**
 * @Description ͼ�ν�����
 * @author �¾�
 *
 */

public class GUI extends JFrame {

	private JPanel contentPane;
	public static JFrame frame;
	public static JTextArea textArea;
	public static ReentrantLock timerLock=new ReentrantLock();           //����������Ҫ���ڿ���ʱ�ӽ������������̵�ͨ��
    public static Condition timerCondition =timerLock.newCondition();
    public static int newProcessNum = 0;                             //�ֶ������Ľ��̵���Ŀ
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GUI frame = new GUI();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public GUI() {
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 870, 732);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JPanel panel = new JPanel();
		contentPane.add(panel, BorderLayout.NORTH);
		
		//��ʼ
		JButton button1 = new JButton("��ʼ");
		button1.addActionListener(new ActionListener() {  
			public void actionPerformed(ActionEvent e) {
				new Clock().start();   //����ʱ�ӽ���
				new KeyBoard().start();      //�������̽���
				new Display().start();      //������ʾ������
				new PV().start();         //����PVͨ�Ž���
				new ProcessSchedule().start();  //�������̵��Ƚ���
			}
		});
		panel.add(button1);
		
		//�����½��� 
		JButton button2 = new JButton("���̴���");
		button2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textArea.setCaretPosition(textArea.getText().length());
				Random rand = new Random();			
				int ProID = 6 + newProcessNum++;  //�ֶ������Ľ���id��6��ʼ����
				int Priority = rand.nextInt(5) + 1;  //����������ȼ�����1��5֮��
				int InTimes = CPU.getTime();
				int InstrucNum = rand.nextInt(20) + 1;  //�������ָ����Ŀ��1��20֮��
				ArrayList<Instruction> instructionSegment = new ArrayList<>(); 
				for(int i = 0; i < InstrucNum ; i++) {
					Instruction instruction = new Instruction(i+1, rand.nextInt(4));     //ÿ�������ɵ�ָ����0��3֮��
					instructionSegment.add(instruction);
				}
				new Process(ProID, Priority, InTimes, InstrucNum, instructionSegment);
			}
		});
		panel.add(button2);
		
		//��ͣ
		JButton button3 = new JButton("��ͣ");
		button3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Clock.setsuspend(true);
			}
		});
		panel.add(button3);
		
		//����
		JButton button4 = new JButton("����");
		button4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Clock.setsuspend(false);
			}
		});
		panel.add(button4);
		
		//�رղ�������
		JButton button5 = new JButton("�رղ�������");
		button5.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				IO.Save(textArea.getText(), "/ProcessResultPart.txt");
				System.exit(0);//�رմ���
			}
		});
		panel.add(button5);
		
		
		
		//�ɹ������
		JPanel panel2 = new JPanel();
		contentPane.add(panel2, BorderLayout.CENTER);
		panel2.setLayout(new GridLayout(1, 2, 5, 5));
		
		JScrollPane scrollPane1 = new JScrollPane();
		panel2.add(scrollPane1);
		
		textArea = new JTextArea();
		textArea.setFont(new Font("����Ҧ��", Font.PLAIN, 18));
		scrollPane1.setViewportView(textArea);
	}

}
