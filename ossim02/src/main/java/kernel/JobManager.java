package kernel;

import hardware.Clock;
import os.Manager;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.*;
import java.util.ArrayList;

/**
 * 文件读写类；数据驱动；
 */
public class JobManager {
    //输入文件内容缓存
    private static final ArrayList<int[]> inputJobs = readJobsInputFile();

    /**
     * @param content  文本内容
     * @param fileName 文本路径
     * @Description: 将输出结果保存到路径上，若不存在则创建路径
     */
    public static void SaveLog(String content, String fileName) {
        byte[] sourceByte = content.getBytes();
        String path = "";
        try {
            File file = new File(path + fileName);//文件路径（路径+文件名）
            if (!file.exists()) {   //文件不存在则创建文件，先创建目录
                File dir = new File(file.getParent());
                if (dir.mkdirs())
                    if (file.createNewFile())
                    	Manager.getDashboard().consoleWriteln("保存成功");
            }
            FileOutputStream outStream = new FileOutputStream(file); //文件输出流将数据写入文件
            outStream.write(sourceByte);
            outStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //todo 追加input file，来记录新的job的创建
    public static void appendJobsInputFile() {
        throw new NotImplementedException();
    }

    //todo 创建新的文件来记录进程和指令段
    public static void newJobFile(PCB pcb) {
        throw new NotImplementedException();
    }

    /**
     * 加载全部作业；
     * 一次性加载完成。
     * @param nowTime 
     */
    public static void loadJobsAndIns(int nowTime) {
        JobManager.createJobs(JobManager.inputJobs,nowTime);
        Manager.getDashboard().consoleWriteln("作业和指令加载完成");
        Schedule.displayAllQueue();
    }

    /**
     * @return ArrayList<int [ ]> 返回一个二维数组
     * 通过BufferedReader 流的形式进行流缓存，之后通过readLine方法获取到缓存的内容。
     */
    private static ArrayList<int[]> readJobsInputFile() {
    	Manager.getDashboard().consoleWriteln("正在读取作业");
        return inputPaste(Manager.inputFilePath);
    }

    /**
     * @return ArrayList<int [ ]>   返回一个二维数组，外层用ArrayList设置要写入进程的数量
     * 每一行都是一个进程，每一个进程（二维数组的成员）分别存储
     * 进程id，进程优先级，进程运行时间和进程内指令数量。
     * @Description: 从每个进程的文件中读取待实现的指令
     */
    private static ArrayList<int[]> readInstructions(String fileName) {
        return inputPaste(fileName);
    }

    // 处理文件
    private static ArrayList<int[]> inputPaste(String fileName) {
        File file = new File(fileName);
        BufferedReader reader = null;
        String[] inputInstructionString;
        ArrayList<int[]> inputInstructionInt = new ArrayList<>();
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString;
            reader.readLine();                  //第一行是说明行，没有实际意义
            while ((tempString = reader.readLine()) != null) {  // 一次读入一行，直到读入null为文件结束
                inputInstructionString = tempString.split(",");  //按逗号分割字符串，将每一行的输入转化成4个子字符串形成的数组
                inputInstructionInt.add(StringToInt(inputInstructionString));  //将str数组转化成int数组
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return inputInstructionInt;
    }


    // String数组内数字都转化成int类型
    private static int[] StringToInt(String[] arrs) {
        int[] ints = new int[arrs.length];
        for (int i = 0; i < arrs.length; i++) {
            try {                 //对非数字进行异常处理
                ints[i] = Integer.parseInt(arrs[i]);    //String数组内数字都转化成int类型
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return ints;
    }


    /**
     * 每次创建一个新作业，并考虑到是单进程作业，于是一个作业就是一个进程；
     * 并对job根据intime字段进行过滤。
     * @param currentTime 
     */
    private static void createJobs(ArrayList<int[]> sourcePCBList, int currentTime) {
        // 过滤
    	System.out.println(Clock.getCurrentTime());
        for (int[] sourcePCB : sourcePCBList) {
            int jobInTime = sourcePCB[2];
            if ((jobInTime <= currentTime)&&(jobInTime>(currentTime-5) ) ) {// 根据时间判断，当前是否创建作业
            	long A=System.currentTimeMillis();
                String id = Integer.toString(sourcePCB[0]);
                String jobFile = Manager.rootPath + id + ".txt";    //根据进程id，去读这个进程对应的指令的文件
                ArrayList<int[]> sourceInsList = readInstructions(jobFile);
                // create a job
                createJob(sourcePCB, sourceInsList);
                long B=System.currentTimeMillis();
                System.out.println("运行时间： "+(B-A)+"ms");
            }
        }
    }

    // 创建单个作业，根据文件
    private static void createJob(int[] sourcePCB, ArrayList<int[]> sourceInsList) {
        ArrayList<Instruction> instructionSegment = new ArrayList<>();
        for (int[] e : sourceInsList) {     //根据文件读出的内容生成程序段
            Instruction instruction = new Instruction(e[0], e[1]);
            instructionSegment.add(instruction);
        }
        // 调用进程创建原语
        Process.createProcess(new PCB(sourcePCB[0], sourcePCB[1], sourcePCB[2], sourcePCB[3]), instructionSegment);
    }
}