package org.uengine.uenginecloudconfig;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

import java.text.NumberFormat;

@SpringBootApplication
@EnableConfigServer
public class UengineCloudConfigApplication {

	private static Log logger = LogFactory.getLog(UengineCloudConfigApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(UengineCloudConfigApplication.class, args);

		Runtime runtime = Runtime.getRuntime();
		final NumberFormat format = NumberFormat.getInstance();
		final long maxMemory = runtime.maxMemory();
		final long allocatedMemory = runtime.totalMemory();
		final long freeMemory = runtime.freeMemory();
		final long mb = 1024 * 1024;
		final String mega = "MB";
		logger.info("========================== Memory Info ==========================");
		logger.info("Free memory: " + format.format(freeMemory / mb) + mega);
		logger.info("Allocated memory: " + format.format(allocatedMemory / mb) + mega);
		logger.info("Max memory: " + format.format(maxMemory / mb) + mega);
		logger.info("=================================================================\n");
	}
}
