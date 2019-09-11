package com.guigu.gmall.manage.controller;


import org.apache.commons.lang3.StringUtils;
import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@CrossOrigin
// 文件上传
public class FileUploadController {
    //图片显示的一个路径 需要注意
    @Value("${fileServer.url}")
    String fileServerUrl;

    @PostMapping("fileUpload")
    public String fileUpload(@RequestParam("file")MultipartFile file) throws IOException, MyException {
        //得到一个路径
        String confPath = this.getClass().getResource("/tracker.conf").getFile();
        //把文件的内存加载到环境 或者内存中
        ClientGlobal.init(confPath);
        // 客户端服务端 相互连接的一个方法
        TrackerClient trackerClient = new TrackerClient();
        TrackerServer trackerServer = trackerClient.getConnection();
        StorageClient storageClient = new StorageClient(trackerServer, null);

        String filename= file.getOriginalFilename();
        String extName = StringUtils.substringAfterLast(filename, ".");
        //放参数---保存的位置
        String[] upload_file = storageClient.upload_file(file.getBytes(), extName, null);
        //循环遍历
        String fileUrl=fileServerUrl;
        for (int i = 0; i < upload_file.length; i++) {
            String s = upload_file[i];
           fileUrl+="/"+s;
        }
        return fileUrl;
    }
}
