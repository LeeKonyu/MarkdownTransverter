package com.lky.main;

import com.lky.constant.StaticConstants;
import com.lky.upload.UseJsoup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * @author : 猕猴桃
 * @create 2020/3/12 11:27
 */
public class Execute {

    private static String htmlLocalPath;
    private static String picPath;
    static {
        htmlLocalPath = StaticConstants.htmlLocalPath;
        picPath = StaticConstants.picPath;
    }

    public static void getHtmlPath(String htmlLocalPath){
        File file = new File(htmlLocalPath);
        BufferedReader reader=null;
        String temp=null;
        int line=1;
        try{
            reader=new BufferedReader(new FileReader(file));
            while((temp=reader.readLine())!=null){
                System.out.println("您创建的txt文件的第" + line + "行的网址为"+":" + temp);
                System.out.println("开始执行第"+line+"个网址的转换");
                UseJsoup.useJsoup(temp);
                line++;
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally{
            if(reader!=null){
                try{
                    reader.close();
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    //下载完图片后删除保存图片的文件夹
    public static void delFile(File file){
        if (file.isDirectory()){
            for (File f : file.listFiles()){
                delFile(f);
            }
        }
        file.delete();
    }


    //执行所有操作的方法
    public static void execute(){
        getHtmlPath(htmlLocalPath);
        delFile(new File(picPath));
    }

    public static void main(String[] args) {
        Execute.execute();
    }
}
