package os;

public class Main {
    // 主程序入口
    public static void main(String[] args) {
        // 创建系统管理器
        Manager manager = Manager.getInstance();
        // 系统启动完毕，开始运行
        manager.main();
    }
}
