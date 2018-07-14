package com.hzgc.compare.worker;

import com.hzgc.compare.worker.conf.Config;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class WorkerAccept {
    private static Logger LOG = Logger.getLogger(WorkerAccept.class);
    private int port;
    private Worker worker;
    private Config conf;

    public WorkerAccept(Worker worker){
        this.worker = worker;
        this.conf = Config.getConf();
        port = conf.getValue(Config.WORKER_PORT, 8888);
    }

    public void accept()  {
        BufferedReader in;
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
            Socket socket = null;
            while (true){
                socket = serverSocket.accept();
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String line = in.readLine();
                if("stop".equals(line)) {
                    LOG.info("Stop this worker");
                    worker.stop();
                    System.exit(0);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
