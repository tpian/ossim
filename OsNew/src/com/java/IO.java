package com.java;
import java.io.*;
import java.util.ArrayList;

/**
 *文件读写类
 *
 */
public class IO {
	
	/**
	* 从input文件中读取待实现的进程
	* @return ArrayList<int[]> 返回一个二维数组
	* 通过BufferedReader 流的形式进行流缓存，之后通过readLine方法获取到缓存的内容。
	*/
	public static ArrayList<int[]> readProcessFromFile(String fileName) {  
        File file = new File(fileName);  
        BufferedReader reader = null;  
        String[] inputProcessString;
        ArrayList<int[]> inputProcessInt = new ArrayList<int[]>();
        try {   
            reader = new BufferedReader(new FileReader(file));  
            String tempString = null;
            tempString = reader.readLine();//第一行是说明行，没有实际意义
            while ((tempString = reader.readLine()) != null) {  // 一次读入一行，直到读入null为文件结束 
            	inputProcessString = tempString.split(",");  //按逗号分割字符串，将每一行的输入转化成4个子字符串形成的数组
            	inputProcessInt.add(StringToInt(inputProcessString));  //将str数组转化成int数组
            }  
            reader.close();  
        } catch (IOException e) {  
            e.printStackTrace();  
        } finally {  
            if (reader != null) {  
                try {  
                    reader.close();  
                } catch (IOException e1) {  
                }  
            }  
        } 
        return inputProcessInt; 
    }
	
	/**
	* @Description: 从每个进程的文件中读取待实现的指令
	* @param fileName
	* @return ArrayList<int[]>   返回一个二维数组，外层用ArrayList灵活设置要写入进程的数量
	* 					每一行都是一个进程，每一个进程（二维数组的成员）分别存储
	* 					 进程id，进程优先级，进程运行时间和进程内指令数量。
	* @throws
	*/
	public static ArrayList<int[]> readInstructionFromFile(String fileName) {  
        File file = new File(fileName);  
        BufferedReader reader = null;  
        String[] inputInstructionString;
        ArrayList<int[]> inputInstructionInt = new ArrayList<int[]>();
        try {   
            reader = new BufferedReader(new FileReader(file));  
            String tempString = null;
            tempString = reader.readLine();                  //第一行是说明行，没有实际意义
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
                } catch (IOException e1) {  
                }  
            }  
        } 
        return inputInstructionInt; 
    }
	
	
	/**
	* String数组内数字都转化成int类型
	*/
	public static int[] StringToInt(String[] arrs){
	    int[] ints = new int[arrs.length];
	    for(int i=0;i<arrs.length;i++){
	        try {                 //对非数字进行异常处理
	        	ints[i]= Integer.parseInt(arrs[i]);    //String数组内数字都转化成int类型
        	} catch (NumberFormatException e) {
        	    e.printStackTrace();
        	}
	    }
	    return ints;
	}
	
	/**
	* @Description: 将输出结果保存到路径上，若不存在则创建路径
	* @param content  文本内容
	* @param fileName 文本路径
	* @throws
	*/
	public static void Save(String content, String fileName) {
		byte[] sourceByte = content.getBytes();//新方法
	     String path = "E:/output";
	     if(null != sourceByte){
	         try {
	             File file = new File(path+fileName);//文件路径（路径+文件名）
	             if (!file.exists()) {   //文件不存在则创建文件，先创建目录
	                 File dir = new File(file.getParent());
	                 dir.mkdirs();
	                 file.createNewFile();
	             }
	             FileOutputStream outStream = new FileOutputStream(file); //文件输出流将数据写入文件
	             outStream.write(sourceByte);
	             outStream.close();
	         } catch (Exception e) {
	             e.printStackTrace();
	         } 
	     }
	}
}