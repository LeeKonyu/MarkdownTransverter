package com.lky.utils;

import java.io.File;
import java.io.IOException;

/**
 * @author : 猕猴桃
 * @create 2020/3/12 11:00
 */
public class FileUtil {

    /**
     * 创建文件夹
     * @param dirPath:要创建的文件目录
     */
    public static void createDir(String dirPath){
        File file = new File(dirPath);
        if (!file.exists()){
            System.out.println(file + "该文件夹不存在，创建目录");
            file.mkdirs();
        }
    }

    public static File createFile(String picPath){
        File file = new File(picPath);
        if (!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }
}
