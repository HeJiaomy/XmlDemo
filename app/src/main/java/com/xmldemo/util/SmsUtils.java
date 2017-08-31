package com.xmldemo.util;

import android.content.Context;
import android.util.Log;
import android.util.Xml;

import com.xmldemo.bean.SmsBean;
import com.xmldemo.dao.SmsDao;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;


/**
 * Created by hejiao on 2017/8/30.
 */

public class SmsUtils {

    public static boolean backupSms_android(Context context) {

        try {

            //获取短信数据
            ArrayList<SmsBean> allSms = SmsDao.getAllSms();
            //获取xmlSerializer对象
            XmlSerializer xs = Xml.newSerializer();
            //2.设置XmlSerializer的一些参数，比如：设置xml写入到哪个文件中
            //os:xml文件写入流   encoding：流的编码
            xs.setOutput(context.openFileOutput("backup.xml", Context.MODE_PRIVATE), "utf-8");
            //3.序列化一个xml的声明头
            //encoding:xml文件的编码  standalone:是否独立
            xs.startDocument("utf-8", true);
            //4.序列化一个根节点的开始节点
            //namespace:命名空间  name： 标签的名称
            xs.startTag(null, "Smss");
            //5.循环遍历list集合序列化一条条短信
            for (SmsBean smsBean : allSms) {
                xs.startTag(null, "Sms");
                //name:属性的名称  value：属性值
                xs.attribute(null, "id", smsBean.id + "");

                xs.startTag(null, "num");
                xs.text(smsBean.num);
                xs.endTag(null, "num");

                xs.startTag(null, "msg");
                xs.text(smsBean.msg);
                xs.endTag(null, "msg");

                xs.startTag(null, "date");
                xs.text(smsBean.date);
                xs.endTag(null, "date");

                xs.endTag(null, "Sms");
            }
            //6.序列化一个根节点的结束节点
            xs.endTag(null, "Smss");

            //7.将xml写入到文件中，完成xml的序列化
            xs.endDocument();

            return true;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean backupSms(Context context) {
        //获取短信内容
        ArrayList<SmsBean> allSms = SmsDao.getAllSms();
        //将数据以xml格式封装到StringBuffer中
        StringBuffer sb = new StringBuffer();
        sb.append("<?xml version='1.0' encoding='utf-8' standalone='yes' ?>");
        //封装根节点
        sb.append("<Smss>");
        //循环遍历list集合封装的所有短信
        for (SmsBean smsBean : allSms) {
            sb.append("<Sms id= \"" + smsBean.id + "\">");

            sb.append("<num>");
            sb.append(smsBean.num);
            sb.append("</num>");

            sb.append("<msg>");
            sb.append(smsBean.msg);
            sb.append("</msg>");

            sb.append("<date>");
            sb.append(smsBean.date);
            sb.append("</date>");

            sb.append("</Sms>");
        }

        sb.append("</Smss>");

        //将StringBuffer中的xml字符串写入私有目录文件中
        try {
            FileOutputStream fileOutputStream = context.openFileOutput("backupsms.xml", Context.MODE_PRIVATE);
            fileOutputStream.write(sb.toString().getBytes());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    //解析xml文件读取短信内容
    public static int restore(Context mContext) {
        ArrayList<SmsBean> arrayList= null;
        SmsBean smsBean= null;

        try {

            //1.通过Xml获取一个XmlPullParser对象
            XmlPullParser xpp = Xml.newPullParser();

            /**
            //通过context获取一个资产管理者对象
            AssetManager assets= mContext.getAssets();
            //通过资产管理者对象能获取一个文件读取流
            InputStream inputStream= assets.open("backup.xml");
            xpp.setInput(inputStream,"utf-8");

             */
            //2.设置XmlPullParser对象的参数，需要解析的是哪个xml文件,设置一个文件读取流
            xpp.setInput(mContext.openFileInput("backup.xml"), "utf-8");
            //3.获取当前xml行的事件类型
            int type= xpp.getEventType();
            //4.判断事件类型是否是文档结束的事件类型
            while (type!= XmlPullParser.END_DOCUMENT){

                String currentTagName= xpp.getName();
                //判断当前行的事件类型是开始标签还是结束标签
                switch (type){
                    case XmlPullParser.START_TAG:
                        if(currentTagName.equals("Smss")){
                            arrayList= new ArrayList<>();
                        }else if(currentTagName.equals("Sms")){
                            smsBean= new SmsBean();
                            smsBean.id= Integer.parseInt(xpp.getAttributeValue(null,"id"));
                        }else if (currentTagName.equals("num")){
                            smsBean.num= xpp.nextText();
                        }else if (currentTagName.equals("msg")) {
                            smsBean.msg = xpp.nextText();
                        }else if (currentTagName.equals("date")) {
                            smsBean.date = xpp.nextText();
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        //当前结束标签是Sms的话，一条短信数据封装完成， 可以加入list中
                        if(currentTagName.equals("Sms")){
                            arrayList.add(smsBean);
                        }
                        break;
                }
                //5.如果不是文档结束的事件类型，循环遍历解析每一行的数据。解析一行后，获取下一行的事件类型
                type= xpp.next();
            }
            for (int i=0; i<arrayList.size(); i++) {
                Log.e("ArrayList---", Arrays.toString(arrayList.toArray()));
            }
            return arrayList.size();
        }catch (FileNotFoundException | XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

}
