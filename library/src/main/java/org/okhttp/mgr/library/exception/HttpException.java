package org.okhttp.mgr.library.exception;

/**
 * Created by huwentao on 16/10/8.
 */
public class HttpException extends Exception {
    public static HttpException INSTANCE(String message) {
        return new HttpException(message);
    }

    public HttpException() {
        super();
    }

    public HttpException(String message) {
        super(message);
    }

    public HttpException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public HttpException(Throwable throwable) {
        super(throwable);
    }
}
