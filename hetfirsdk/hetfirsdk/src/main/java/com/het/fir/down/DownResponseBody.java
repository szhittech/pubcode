package com.het.fir.down;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;


public class DownResponseBody extends ResponseBody {
    private Response originalResponse;
    private IDownProgress progress;
    private long bytesReaded = 0;
    private long contentLength = 0;

    public DownResponseBody(Response originalResponse, long startsPoint, long totalsize, IDownProgress downProgress) {
        this.originalResponse = originalResponse;
        progress = downProgress;
        bytesReaded += startsPoint;
        contentLength = contentLength();
        if (totalsize > 0) {
            contentLength = totalsize;
        }
    }

    @Override
    public MediaType contentType() {
        return originalResponse.body().contentType();
    }

    @Override
    public long contentLength() {
        return originalResponse.body().contentLength();
    }

    @Override
    public BufferedSource source() {
        return Okio.buffer(new ForwardingSource(originalResponse.body().source()) {

            @Override
            public long read(Buffer sink, long byteCount) throws IOException {
                long bytesRead = super.read(sink, byteCount);
                // read() returns the number of bytes read, or -1 if this source is exhausted.
                if (bytesRead != -1) {
                    bytesReaded += bytesRead != -1 ? bytesRead : 0;
                    float progess = (float) bytesReaded / (float) contentLength * 100;
//                    Logc.e("=====================>" + bytesReaded + "," + (int) progess + "%");
                    if (progress != null) {
                        progress.onProgress(bytesReaded, contentLength, bytesRead == -1);
                    }
                }
                return bytesRead;
            }
        });
    }

    public interface IDownProgress {
        void onProgress(long read, long contentLength, boolean done);
    }
}
