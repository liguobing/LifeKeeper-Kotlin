package com.lixyz.lifekeeperforkotlin.net

import okhttp3.MediaType
import okhttp3.RequestBody
import okio.*

class CountingRequestBody(var delegate: RequestBody, private var listener: (max: Long, value: Long) -> Unit): RequestBody() {

    override fun contentType(): MediaType? {
        return delegate.contentType()
    }

    override fun contentLength(): Long {
        try {
            return delegate.contentLength()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return -1
    }

    override fun writeTo(sink: BufferedSink) {
        val countingSink = CountingSink(sink)
        val bufferedSink = countingSink.buffer()
        delegate.writeTo(bufferedSink)
        bufferedSink.flush()
    }

    inner class CountingSink(delegate: Sink): ForwardingSink(delegate) {
        private var bytesWritten: Long = 0

        override fun write(source: Buffer, byteCount: Long) {
            super.write(source, byteCount)
            bytesWritten += byteCount
            listener(contentLength(), bytesWritten)
        }
    }
}