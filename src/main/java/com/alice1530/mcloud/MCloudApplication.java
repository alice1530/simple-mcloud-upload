package com.alice1530.mcloud;

import com.alice1530.mcloud.store.MCloudClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.util.FileCopyUtils;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;


@EnableScheduling
@SpringBootApplication
public class MCloudApplication {
	public static final Logger log = LoggerFactory.getLogger(MCloudApplication.class);
    public static void main(String[] args) {
    	//常规启动
//        SpringApplication.run(MCloudApplication.class, args);
		//无web模式启动
        new SpringApplicationBuilder(MCloudApplication.class)
				//关闭banner
				.bannerMode(Banner.Mode.OFF)
				//无web方式
				.web(WebApplicationType.NONE)
				.run(args);

    }

    @Autowired
	MCloudClientService clientService;
    @PostConstruct
    public void init() {
		try {
			File f = new File("application.properties");
			if (!f.exists()) {
				log.info("cp file application.properties to current running directory");
				// 复制配置文件到当前运行目录下
				InputStream i = this.getClass().getClassLoader().getResourceAsStream("application.properties");
				FileOutputStream o = new FileOutputStream(f);
				FileCopyUtils.copy(i, o);
				log.info("file application.properties copy ok !");
			} else {
				log.info("exists file application.properties");
			}


//			String path ="/Tools/360zip.zip";
//			Response download = clientService.download(path);
//			FileCopyUtils.copy(download.body().byteStream(),new FileOutputStream("D:\\Desktop\\Desktop\\xx\\360zip.zip"));
//			System.out.println("download = " + download);
//			String message = download.message();
//			System.out.println("message = " + message);

//			File tmp = new File("D:\\Desktop\\Desktop\\xx\\ffmpeg.exe");
//			long size = tmp.length();
//			System.out.println(ff.getName());
//			System.out.println(size);
//			String path = "/tmp/"+tmp.getName();
//			FileInputStream in  =new FileInputStream(tmp);
//			clientService.uploadPre(path,size,in);
//			clientService.createFolder("/qfdj/2022-07-12/aa/");
//			clientService.move("/tmp/ffmp_win.exe","/tmp2/");
//			clientService.rename(path,"ffmp_win.exe");

//			in.close();
		} catch (Exception e) {
			log.error(e.getMessage());
		}        
    }

/*
    @Bean
    public ServletRegistrationBean dispatcherRegistration(DispatcherServlet dispatcherServlet) {
        ServletRegistrationBean registration = new ServletRegistrationBean(dispatcherServlet);
        registration.addUrlMappings("/update");
        registration.setName("restServlet");
        return registration;
    }
*/
}
