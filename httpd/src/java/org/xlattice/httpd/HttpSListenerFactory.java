package org.xlattice.httpd;

import org.xlattice.transport.CnxListenerFactory;

public class HttpSListenerFactory implements CnxListenerFactory {

    public HttpSListenerFactory() {}

    public HttpSListener getInstance() {
        return new HttpSListener();
    }
}
