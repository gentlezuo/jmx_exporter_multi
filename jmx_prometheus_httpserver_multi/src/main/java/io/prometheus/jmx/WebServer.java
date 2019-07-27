package io.prometheus.jmx;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class WebServer {

    private static final Logger logger = Logger.getLogger(WebServer.class.getName());

    public static void main(String[] args) throws Exception {


        if (args.length < 1) {
            System.err.println("Usage: WebServer<thread numbers> <yaml configuration file>");
            System.exit(1);
        }
        int numOfThread = args.length <2 ? Integer.parseInt(args[0]) : 100;
        String fileName=args.length<2 ? args[0]: args[1];
        new BuildInfoCollector().register();
        List<Map<String, Object>> list = (List<Map<String, Object>>) new Yaml().load(new FileReader(new File(fileName)));
        //采用线程池，多个任务
        ExecutorService executorService = Executors.newFixedThreadPool(numOfThread);
        for (Map<String, Object> o : list) {
            logger.info((String) o.get("jobname") + " submit");
            executorService.submit(new ScraperTask(o));
        }
        logger.info("started...");
    }
}
