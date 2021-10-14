package com.java;
import java.io.*;
import java.util.ArrayList;

/**
 *�ļ���д��
 *
 */
public class IO {
	
	/**
	* ��input�ļ��ж�ȡ��ʵ�ֵĽ���
	* @return ArrayList<int[]> ����һ����ά����
	* ͨ��BufferedReader ������ʽ���������棬֮��ͨ��readLine������ȡ����������ݡ�
	*/
	public static ArrayList<int[]> readProcessFromFile(String fileName) {  
        File file = new File(fileName);  
        BufferedReader reader = null;  
        String[] inputProcessString;
        ArrayList<int[]> inputProcessInt = new ArrayList<int[]>();
        try {   
            reader = new BufferedReader(new FileReader(file));  
            String tempString = null;
            tempString = reader.readLine();//��һ����˵���У�û��ʵ������
            while ((tempString = reader.readLine()) != null) {  // һ�ζ���һ�У�ֱ������nullΪ�ļ����� 
            	inputProcessString = tempString.split(",");  //�����ŷָ��ַ�������ÿһ�е�����ת����4�����ַ����γɵ�����
            	inputProcessInt.add(StringToInt(inputProcessString));  //��str����ת����int����
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
	* @Description: ��ÿ�����̵��ļ��ж�ȡ��ʵ�ֵ�ָ��
	* @param fileName
	* @return ArrayList<int[]>   ����һ����ά���飬�����ArrayList�������Ҫд����̵�����
	* 					ÿһ�ж���һ�����̣�ÿһ�����̣���ά����ĳ�Ա���ֱ�洢
	* 					 ����id���������ȼ�����������ʱ��ͽ�����ָ��������
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
            tempString = reader.readLine();                  //��һ����˵���У�û��ʵ������
            while ((tempString = reader.readLine()) != null) {  // һ�ζ���һ�У�ֱ������nullΪ�ļ����� 
            	inputInstructionString = tempString.split(",");  //�����ŷָ��ַ�������ÿһ�е�����ת����4�����ַ����γɵ�����
            	inputInstructionInt.add(StringToInt(inputInstructionString));  //��str����ת����int����
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
	* String���������ֶ�ת����int����
	*/
	public static int[] StringToInt(String[] arrs){
	    int[] ints = new int[arrs.length];
	    for(int i=0;i<arrs.length;i++){
	        try {                 //�Է����ֽ����쳣����
	        	ints[i]= Integer.parseInt(arrs[i]);    //String���������ֶ�ת����int����
        	} catch (NumberFormatException e) {
        	    e.printStackTrace();
        	}
	    }
	    return ints;
	}
	
	/**
	* @Description: �����������浽·���ϣ����������򴴽�·��
	* @param content  �ı�����
	* @param fileName �ı�·��
	* @throws
	*/
	public static void Save(String content, String fileName) {
		byte[] sourceByte = content.getBytes();//�·���
	     String path = "E:/output";
	     if(null != sourceByte){
	         try {
	             File file = new File(path+fileName);//�ļ�·����·��+�ļ�����
	             if (!file.exists()) {   //�ļ��������򴴽��ļ����ȴ���Ŀ¼
	                 File dir = new File(file.getParent());
	                 dir.mkdirs();
	                 file.createNewFile();
	             }
	             FileOutputStream outStream = new FileOutputStream(file); //�ļ������������д���ļ�
	             outStream.write(sourceByte);
	             outStream.close();
	         } catch (Exception e) {
	             e.printStackTrace();
	         } 
	     }
	}
}