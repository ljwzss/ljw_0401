package com.guigu.gmall.manage;






import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;


@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallManageWebApplicationTests {

	@Test
	public void contextLoads() {
	}
		@Test
		public void uploadFile() throws IOException, MyException {

		//得到一个路径
			String file = this.getClass().getResource("/tracker.conf").getFile();
		//把文件的内存加载到环境 或者内存中
			ClientGlobal.init(file);
		// 客户端服务端 相互连接的一个方法
			TrackerClient trackerClient = new TrackerClient();
			TrackerServer trackerServer = trackerClient.getConnection();
			StorageClient storageClient = new StorageClient(trackerServer, null);

			//放参数---保存的位置
			String[] upload_file = storageClient.upload_file("e://001.jpg", "jpg", null);
			//循环遍历
			for (int i = 0; i < upload_file.length; i++) {
				String s = upload_file[i];
				System.err.println("=====s"+s);
			}
		}
	}


