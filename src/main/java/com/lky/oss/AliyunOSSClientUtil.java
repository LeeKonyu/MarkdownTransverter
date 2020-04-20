package com.lky.oss;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.Bucket;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectResult;
import com.lky.constant.StaticConstants;
import jdk.nashorn.internal.objects.Global;
import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author : 猕猴桃
 * @create 2020/3/9 22:03
 */
public class AliyunOSSClientUtil {
    //log日志
    private static Logger logger = Logger.getLogger(AliyunOSSClientUtil.class);
    //阿里云API的内或外网域名
    private static String endpoint;
    //阿里云API的密钥Access Key ID
    private static String accessKeyId;
    //阿里云API的密钥Access Key Secret
    private static String accessKeySecret;
    //阿里云API的bucket名称
    private static String bucketName;
    //阿里云API的文件夹名称
    private static String fileOSSDir;

    //初始化文件夹下的文件数为1
    static int numberOfFileList=1;
    // 设置URL过期时间为1小时。
    static Date expiration = new Date(new Date().getTime() + 3600 * 1000);

    static URL url = null;
    final static Map<Integer,String> urls = new HashMap<Integer,String>();

    //初始化属性
    static{
        endpoint = StaticConstants.endpoint;
        accessKeyId = StaticConstants.accessKeyId;
        accessKeySecret = StaticConstants.accessKeySecret;
        bucketName = StaticConstants.bucketName;
        fileOSSDir = StaticConstants.fileOSSDir;
    }

/*    *//**
     * 通过反射获取变量
     * @param field
     * @return
     *//*
    public static Object getConst(String field){
        try {
            return AliyunOSSClientUtil.class.getField(field).get(null);
        } catch (Exception e) {}
        return null;
    }*/


    /**
     * 获取阿里云OSS客户端对象
     * @return ossClient
     */

    public static OSS getOSSClient(){
        return new OSSClientBuilder().build(endpoint,accessKeyId,accessKeySecret);
    }

    /**
     * 对url进行格式化
     * @param transUrl
     * @return
     * @throws MalformedURLException
     */
    public static URL urlFormart(URL transUrl) throws MalformedURLException {
        String stringUrl= transUrl.toString().substring(0,transUrl.toString().lastIndexOf("?Expires"));
        URL url = new URL(stringUrl);
        return url;
    }

    /**
     * 上传文件到OSS并返回格式化的url
     * @param fileOSSDir
     * @param bucketName
     * @param file
     * @param expiration
     * @param numberOfFileList
     * @return
     * @throws MalformedURLException
     */
    public static String uploadFile(String fileOSSDir,String bucketName,File file,Date expiration,int numberOfFileList) throws MalformedURLException {
        String objectName = fileOSSDir + System.currentTimeMillis() + "" + new SecureRandom().nextInt(0x0400) + file.getName();
        getOSSClient().putObject(bucketName,objectName,file);
        // 生成以GET方法访问的签名URL，访客可以直接通过浏览器访问相关内容。
        url = getOSSClient().generatePresignedUrl(bucketName,objectName,expiration);
        System.out.println("您上传的该网页的第"+numberOfFileList+"个图片的url："+url);

        return urlFormart(url).toString();
    }

    /*public static void UseOSSClientFiles(int numberOfFileList,File[] listFiles) throws MalformedURLException {
        for (File file : listFiles){
            if (file.isFile()) {
                urls.put(numberOfFileList,uploadFile(fileOSSDir, bucketName, file, expiration, numberOfFileList));
                numberOfFileList++;
            } else {
                System.out.println("该File不是文件，而是文件夹");
            }
        }
        // 在网上查找内容，了解到OSS实例为单例模式对象。不能在for循环中进行关闭（关闭之后无法继续下载），所以判断当循环执行完毕后进行关闭并结束进程
        if (numberOfFileList > listFiles.length) {
            getOSSClient().shutdown();
            System.out.println("shutdown");
            return;
        }
    }*/

    /**
     * 
     * @param numberOfFile：第几张图片
     * @param file
     * @return 返回OSS生成的URL地址
     * @throws MalformedURLException
     */
    public static String UseOSSClient(int numberOfFile,File file) throws MalformedURLException {
        // 将url存到map中方便取出
        urls.put(numberOfFile, uploadFile(fileOSSDir, bucketName, file, expiration, numberOfFile));
        // 取出url返回到UseJsoup类中进行更改网页img标签的src
        String getUrl =urls.get(numberOfFile);
        return getUrl;
        // 在网上查找内容，了解到OSS实例为单例模式对象。不能在for循环中进行关闭（关闭之后无法继续下载），所以判断当循环执行完毕后进行关闭并结束进程
    }
    
}
