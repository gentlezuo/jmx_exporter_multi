package io.prometheus.jmx;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.HTTPServer;

import javax.management.MalformedObjectNameException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;

/**
 * @description: task
 * @author: gentlezuo
 * @create: 2019-07-27
 **/

public class ScraperTask implements Runnable {

    Map<String, Object> config;

    public ScraperTask(Map<String, Object> config) {
        this.config = config;
    }

    @Override
    public void run() {
        String[] addr = ((String) config.get("socket")).split(":");
        final CollectorRegistry defaultRegistry = new CollectorRegistry(true);
        try {
            new JmxCollector(config).register(defaultRegistry);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MalformedObjectNameException e) {
            e.printStackTrace();
        }
        InetSocketAddress socket = new InetSocketAddress(addr[0], Integer.parseInt(addr[1]));
        try {
            new HTTPServer(socket, defaultRegistry);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}