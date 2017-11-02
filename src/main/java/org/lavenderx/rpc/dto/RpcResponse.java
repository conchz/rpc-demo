package org.lavenderx.rpc.dto;

import lombok.Data;

@Data
public class RpcResponse {

    private String requestId;
    private Exception exception;
    private Object result;

    public boolean hasException() {
        return exception != null;
    }
}
