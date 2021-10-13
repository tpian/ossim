package gui;

import com.sun.org.apache.xpath.internal.operations.Variable;
import hardware.Clock;
import kernel.Instruction;
import kernel.JobManager;
import kernel.PCB;
import kernel.Process;
import os.Manager;

import java.awt.*;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;


import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Random;
import java.awt.event.ActionEvent;


public class Dashboard extends JFrame {
    private final Manager manager;
    public int newProcessNum = 0;

    //// 全局控件
    private JButton button1;
    private JButton button2;
    private JButton button3;
    private JButton button4;
    private JButton button5;
    private JTextArea console;

    ////文本路径
    final String inputPath = Manager.inputFilePath;
    final String savePath = Manager.outputFilePath;


    public Dashboard(Manager manager) {
        this.manager = manager;
        this.initComponents();
    }

    // 启动该界面
    public void start() {
        EventQueue.invokeLater(() -> {
            try {
                this.bindHandlers();  //绑定控件与处理函数
                this.setVisible(true); // 界面设为可见的
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }


    // 控件初始化
    private void initComponents() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 870, 750);
        JPanel contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);

        JPanel panel = new JPanel();
        contentPane.add(panel, BorderLayout.NORTH);

        //开始
        this.button1 = new JButton("开始");
        panel.add(button1);

        //创建新进程
        this.button2 = new JButton("进程创建");
        panel.add(button2);

        //暂停按钮
        this.button3 = new JButton("暂停");
        panel.add(button3);

        //继续
        this.button4 = new JButton("继续");
        panel.add(button4);

        //保存结果
        this.button5 = new JButton("保存结果");
        panel.add(button5);


        //可滚动面板
        JPanel panel2 = new JPanel();
        contentPane.add(panel2, BorderLayout.CENTER);
        panel2.setLayout(new GridLayout(1, 2, 5, 5));

        JScrollPane scrollPane1 = new JScrollPane();
        panel2.add(scrollPane1);

        console = new JTextArea();
        scrollPane1.setViewportView(console);
    }

    // 日志写
    public synchronized void consoleWriteln(String msg) {
        this.console.append(msg);
        this.console.append("\n");

    }

    // 日志区域滚动
    public void consoleScroll() {
        this.console.setCaretPosition(this.console.getText().length());
    }
    // 打印分隔符号
    public void consoleBar(){
        this.consoleWriteln("=====================================");
    }
    // 随机生成进程
    private synchronized Process generateProcess() {
        console.setCaretPosition(console.getText().length());
        Random rand = new Random();
        int ProID = 6 + newProcessNum++;  //手动创建的进程id从6开始计数
        int Priority = rand.nextInt(5) + 1;  //随机生成优先级介于1到5之间
        int InTimes = Clock.getCurrentTime(); //进入时间为当前时间
        int InstructNum = rand.nextInt(20) + 1;  //随机生成指令数目在1到20之间
        ArrayList<Instruction> instructionSegment = new ArrayList<>();
        for (int i = 0; i < InstructNum; i++) {
            //每条新生成的指令在0到3之间
            Instruction instruction = new Instruction(i + 1, rand.nextInt(4));
            instructionSegment.add(instruction);
        }
        return Process.createProcess(new PCB(ProID, Priority, InTimes, InstructNum), instructionSegment);
    }

    //// 绑定处理函数
    private void bindHandlers() {
        // 启动
        this.button1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //资源加载写在了Manager.start()当中，现在绑定
                manager.start();
                button1.setEnabled(false);
            }
        });
        // 添加
        this.button2.addActionListener(e -> {
            generateProcess();
        });
        // 暂停
        this.button3.addActionListener(e -> {
            manager.getDashboard().consoleWriteln("\n===暂停===\n");
            manager.getClock().suspendTime(true);
            button3.setEnabled(false);
            button4.setEnabled(true);
            manager.getSchedule().displayAllQueue();
        });
        // 继续
        this.button4.addActionListener(e -> {
            manager.getDashboard().consoleWriteln("\n===继续===\n");
            manager.getClock().suspendTime(false);
            button4.setEnabled(false);
            button3.setEnabled(true);
        });
        // 保存
        this.button5.addActionListener(e -> {
            JobManager.SaveLog(console.getText(), savePath);
        });

    }
}
