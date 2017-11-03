package org.lavenderx.rpc.server;

import org.lavenderx.rpc.config.RpcConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class RpcBootstrap {

    public static void main(String[] args) {
        ApplicationContext ctx = new AnnotationConfigApplicationContext(RpcConfig.class);
    }
}
