package os;

import gui.Dashboard;
import hardware.CPU;
import hardware.Clock;
import hardware.DisplayDevice;
import hardware.KeyboardDevice;
import kernel.JobManager;
import kernel.PV;
import kernel.Schedule;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

// 全局资源管理器
public class Manager {
    public static Manager manager = new Manager();
    private Dashboard dashboard;
    /// 同步
    // 递归锁，主要用于同步时钟线程和其他线程之间的通信
    private static final ReentrantLock globalLock = new ReentrantLock();
    // 对应于锁的条件变量
    private static final Condition timerCondition = globalLock.newCondition();

    /// 配置
    // 项目根地址
//    public static final String rootPath = "F:\\新建文件夹\\ossim02\\";
    public static final String rootPath = "D:\\my\\work\\eclipseWork\\ossim\\ossim02\\";
    public static final String inputFilePath = rootPath + "11519101-jobs-input.txt";
    public static final String outputFilePath = rootPath + "ProcessResult.txt";
    // components
    Clock clock;
    CPU cpu = CPU.getInstance();
    KeyboardDevice keyboardDevice;
    DisplayDevice displayDevice;
    PV pv;
    Schedule schedule;


    private Manager() {
   }

    // init
    private synchronized void init() {
        // 使用了单例进行初始化
        // init dashboard
        this.dashboard = new Dashboard(manager);
        this.clock = new Clock();
        this.keyboardDevice = new KeyboardDevice();
        this.displayDevice = new DisplayDevice();
        this.pv = new PV();
        this.schedule = Schedule.getInstance();
    }

    public static Manager getInstance() {
        if (manager == null) {
            return new Manager();
        }
        return manager;
    }

    // Main
    public void main() {
        this.init();// 资源初始化
        this.dashboard.start();  //启动界面
    }

    // 按下启动菜单
    public synchronized void start() {
        this.clock.start();  //启动时钟进程
        this.getDashboard().consoleWriteln("时钟模块初始化完成\n\n");
        this.keyboardDevice.start();      //启动键盘进程
        this.getDashboard().consoleWriteln("键盘模块初始化完成\n\n");
        this.displayDevice.start();      //启动显示器进程
        this.getDashboard().consoleWriteln("显示器模块初始化完成\n\n");
        this.pv.start();         //启动PV通信进程
        this.getDashboard().consoleWriteln("PV模块初始化完成\n\n");
        this.schedule.start();  //启动进程调度进程
        this.getDashboard().consoleWriteln("调度模块初始化完成\n\n");
        this.getDashboard().consoleBar();
    }

    public Dashboard getDashboard() {
        return dashboard;
    }

    public Clock getClock() {
        return clock;
    }

    public CPU getCpu() {
        return cpu;
    }

    public ReentrantLock getGlobalLock() {
        return globalLock;
    }

    public Condition getTimerCondition() {
        return timerCondition;
    }

    public Schedule getSchedule() {
        return schedule;
    }
}
