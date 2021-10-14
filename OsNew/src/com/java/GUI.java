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
 * @Description 图形界面类
 * @author 陈军
 *
 */

public class GUI extends JFrame {

	private JPanel contentPane;
	public static JFrame frame;
	public static JTextArea textArea;
	public static ReentrantLock timerLock=new ReentrantLock();           //重入锁，主要用于控制时钟进程与其他进程的通信
    public static Condition timerCondition =timerLock.newCondition();
    public static int newProcessNum = 0;                             //手动创建的进程的数目
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
		
		//开始
		JButton button1 = new JButton("开始");
		button1.addActionListener(new ActionListener() {  
			public void actionPerformed(ActionEvent e) {
				new Clock().start();   //启动时钟进程
				new KeyBoard().start();      //启动键盘进程
				new Display().start();      //启动显示器进程
				new PV().start();         //启动PV通信进程
				new ProcessSchedule().start();  //启动进程调度进程
			}
		});
		panel.add(button1);
		
		//创建新进程 
		JButton button2 = new JButton("进程创建");
		button2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textArea.setCaretPosition(textArea.getText().length());
				Random rand = new Random();			
				int ProID = 6 + newProcessNum++;  //手动创建的进程id从6开始计数
				int Priority = rand.nextInt(5) + 1;  //随机生成优先级介于1到5之间
				int InTimes = CPU.getTime();
				int InstrucNum = rand.nextInt(20) + 1;  //随机生成指令数目在1到20之间
				ArrayList<Instruction> instructionSegment = new ArrayList<>(); 
				for(int i = 0; i < InstrucNum ; i++) {
					Instruction instruction = new Instruction(i+1, rand.nextInt(4));     //每条新生成的指令在0到3之间
					instructionSegment.add(instruction);
				}
				new Process(ProID, Priority, InTimes, InstrucNum, instructionSegment);
			}
		});
		panel.add(button2);
		
		//暂停
		JButton button3 = new JButton("暂停");
		button3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Clock.setsuspend(true);
			}
		});
		panel.add(button3);
		
		//继续
		JButton button4 = new JButton("继续");
		button4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Clock.setsuspend(false);
			}
		});
		panel.add(button4);
		
		//关闭并保存结果
		JButton button5 = new JButton("关闭并保存结果");
		button5.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				IO.Save(textArea.getText(), "/ProcessResultPart.txt");
				System.exit(0);//关闭窗口
			}
		});
		panel.add(button5);
		
		
		
		//可滚动面板
		JPanel panel2 = new JPanel();
		contentPane.add(panel2, BorderLayout.CENTER);
		panel2.setLayout(new GridLayout(1, 2, 5, 5));
		
		JScrollPane scrollPane1 = new JScrollPane();
		panel2.add(scrollPane1);
		
		textArea = new JTextArea();
		textArea.setFont(new Font("方正姚体", Font.PLAIN, 18));
		scrollPane1.setViewportView(textArea);
	}

}
