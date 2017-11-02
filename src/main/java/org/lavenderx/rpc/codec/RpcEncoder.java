package org.lavenderx.rpc.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.lavenderx.rpc.util.SerializationUtil;

public class RpcEncoder extends MessageToByteEncoder {

    private final Class<?> genericClass;

    public RpcEncoder(Class<?> genericClass) {
        this.genericClass = genericClass;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object obj, ByteBuf out) throws Exception {
        if (genericClass.isInstance(obj)) {
            byte[] data = SerializationUtil.serialize(obj);
            out.writeInt(data.length);
            out.writeBytes(data);
        }
    }
}
