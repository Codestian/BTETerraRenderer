package com.mndk.bteterrarenderer.core.tile;

import com.mndk.bteterrarenderer.core.util.IOUtil;
import com.mndk.bteterrarenderer.core.util.processor.MappedQueueResourceCacheProcessor;
import com.mndk.bteterrarenderer.dep.terraplusplus.HttpResourceManager;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.InputStream;
import java.net.URL;
import java.util.function.Function;

public class TileResourceFetcher<TileId, QueueKey> extends MappedQueueResourceCacheProcessor<TileId, QueueKey, URL, ByteBuf> {

    private final Function<TileId, QueueKey> keyToQueueKeyFunction;

    protected TileResourceFetcher(int nThreads, Function<TileId, QueueKey> keyToQueueKeyFunction) {
        super(nThreads, 3, 1000 * 60 * 5 /* cacheExpireMilliseconds = 5 minutes */, 10000, false);
        this.keyToQueueKeyFunction = keyToQueueKeyFunction;
    }

    @Override
    protected ByteBuf processResource(URL url) throws Exception {
        InputStream stream = HttpResourceManager.download(url.toString());
        ByteBuf buf = Unpooled.copiedBuffer(IOUtil.readAllBytes(stream));
        stream.close();
        return buf;
    }

    @Override
    protected void deleteResource(ByteBuf byteBuf) {}

    @Override
    protected QueueKey keyToQueueKey(TileId tileId) {
        return keyToQueueKeyFunction.apply(tileId);
    }
}