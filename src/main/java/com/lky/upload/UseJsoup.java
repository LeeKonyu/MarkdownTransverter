package com.lky.upload;

import com.lky.constant.StaticConstants;
import com.lky.oss.AliyunOSSClientUtil;
import com.lky.utils.FileUtil;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.security.SecureRandom;

/**
 * @author : 猕猴桃
 * @create 2020/3/10 19:30
 */
public class UseJsoup {

    private static String picPath;
    private static String mdPath;

    static {
        picPath = StaticConstants.picPath;
        mdPath = StaticConstants.mdPath;
    }

    //得到公众号第一张照片：即thumbnail
    static String firstImage = null;

    static int dirNumber = 1;

    /**
     * 更改图片地址
     * @param src：图片地址
     * @return
     */
    public static String stringFormat(String src){
        src = src.substring(0,src.lastIndexOf("0?")+1);
        return src;
    }

    /**
     * 对该类connect方法进行重写
     * @param url
     * @return
     */
    public static Connection connectUrl(String url){
        //再次判断url是否为空（因为写的爬虫不稳定），如果为空跳出异常
        if (url == null || url.length() == 0) {
            System.out.println(url+"------------");
            throw new IllegalArgumentException("String must not be empty");
        }
        else {
            //如果不为空，解析该URL
            return Jsoup.connect(url);
        }
    }

    //对OOS存储的url进行更改，防止重名覆盖
    public static String ossFormat(String newUrl){
        String suffixName = newUrl.substring(newUrl.lastIndexOf("."));
        String prefixName = newUrl.substring(0,newUrl.lastIndexOf("."));
        String randomName = prefixName + System.currentTimeMillis() + "" + new SecureRandom().nextInt(0x0400) + suffixName;
        return randomName;
    }


    /**
     * 调用useJsoup方法开始解析网站进行爬图片
     * @param allUrl：要爬取、解析网站的URL
     */

    public static void  useJsoup(String allUrl) {
        try {
            //进行目标网址
            Document docAll = Jsoup.connect(allUrl).data("query", "Java")
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.132 Safari/537.36")
                    .maxBodySize(0)
                    .get();
            String categories = "LearningYard学苑";
            //获取页面标题
            //String title = docAll.title();
            Element title = docAll.select("div.like_comment_media_title").first();
            //获取页面内容
            Element content = docAll.select("div.rich_media_content").first();
            //获取该网址下img标签
            Elements images = content.getElementsByTag("img");

            //下载的第几张图片（得到的第几个url）
            int numberOfPicUrl=1;
            //对img标签进行遍历
            for (Element image : images)
            {
                //获取图片路径
                String src = image.attr("data-src");
                //更改元素，加了!origin之后data-src解析不出来
                image.removeAttr("data-src");

                //对图片路径进行格式化,便于调试观看
                src = stringFormat(src);
                //System.out.println(src);
                //获取该图片的类型
                String type = image.attr("data-type");
                //判断该图片路径值是否为空，如果为空会产生java.lang.IllegalArgumentException: String must not be empty异常，被困了一天才找到解决办法！！！坑！。
                if (src==null || src.length()==0){
                    System.out.println("该url为空");
                }else {
                    //图片路径不为空，调用download方法对图片进行下载,下载后会上传到阿里云OSS图床返回url
                    String newUrl = downloadPic(numberOfPicUrl,src,type,picPath);
                    if (numberOfPicUrl==1){
                        //得到firstImage做thumbnail
                        firstImage = newUrl;
                    }
                    //在此将页面src更改成网页URL
                    if (type.equals("gif")) {
                        //如果为gif类型的图片，就对url加!animation字段
                        image.attr("src",newUrl + "!animation");
                    }else {
                        //如果为origin类型的图片，就对ur加!origin字段
                        image.attr("src", newUrl + "!origin");
                    }
                    //将图片下载到本地、将图片从本地上传到OSS并返回url后对下一张图片进行操作
                    numberOfPicUrl++;
                }
            }
            //循环完后，使用downloadMd方法将文件下载到本地并生成md文件
            downloadMd(dirNumber,mdPath,title.text(),categories,firstImage,content.html());
            dirNumber++;
        } catch (Exception e) {
            System.out.println(e.getMessage() + "出现错误，请检查一下是否是您输入的网址是否正确");
        }
    }

    /**
     * 将爬取的图片放到本地
     * @param i：遍历的次数
     * @param src：图片URL
     * @param type：图片类型
     * @throws Exception
     */
    public static String downloadPic(int i,String src,String type,String picPath) throws Exception{
        FileUtil.createDir(picPath + dirNumber +"//");
        //创建文件，图片下载的目的地
        File file = FileUtil.createFile(picPath + dirNumber +"//" + i + "."+type);
        //将流从内存输出到文件
        FileOutputStream fo = new FileOutputStream(file);
        //获取过滤输入流到内存
        BufferedInputStream in = connectUrl(src).ignoreContentType(true).execute().bodyStream();
        byte[] buf = new byte[1024];
        int length = 0;
        //进行写入
        while ((length = in.read(buf, 0, buf.length)) != -1) {
            fo.write(buf, 0, length);
        }
        //释放资源
        in.close();
        fo.close();

        //生成url
        return AliyunOSSClientUtil.UseOSSClient(i,file);
    }


    /**
     * 将爬取的内容生成md文件放在本地
     * @param mdPath：爬取文件的地址
     */
    public static void downloadMd(int mdNumber,String mdPath,String title,String categories,String firstImage,String content) {
        try {
            FileUtil.createDir(mdPath);
            File file = FileUtil.createFile(mdPath + "第"+ mdNumber + "个网址对应的文件" + ".md");
            FileWriter fw = new FileWriter(file);
            fw.write("---\n" +
                    "title: " + title + "\n" +
                    "date: 2020-02-28 06:26:55\n" +
                    "categories: " + categories + "\n"+
                    "tags:\n" +
                    "thumbnail: " + firstImage + "!thumbnail\n" +
                    "---\n\n"+content);
            fw.close();
            System.out.println("第" + mdNumber + "个md文件已经生成");
        } catch (Exception e) {
            System.out.println(e.getMessage()+ "连接超时");
            e.printStackTrace();
        }
    }


}

