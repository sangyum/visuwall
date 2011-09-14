/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *  
 *    http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License. 
 *  
 */
package org.apache.mina.http;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.mina.api.IdleStatus;
import org.apache.mina.api.IoFilter;
import org.apache.mina.api.IoSession;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.filterchain.ReadFilterChainController;
import org.apache.mina.filterchain.WriteFilterChainController;
import org.apache.mina.http.api.DefaultHttpResponse;
import org.apache.mina.http.api.HttpEndOfContent;
import org.apache.mina.http.api.HttpMethod;
import org.apache.mina.http.api.HttpRequest;
import org.apache.mina.http.api.HttpStatus;
import org.apache.mina.http.api.HttpVersion;
import org.apache.mina.service.OneThreadSelectorStrategy;
import org.apache.mina.service.SelectorFactory;
import org.apache.mina.transport.tcp.NioSelectorProcessor;
import org.apache.mina.transport.tcp.nio.NioTcpServer;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpTest {

    private static final Logger LOG = LoggerFactory.getLogger(HttpTest.class);

    @Test
    public void simpleServer() throws Exception {

        OneThreadSelectorStrategy strategy = new OneThreadSelectorStrategy(new SelectorFactory(
                NioSelectorProcessor.class));
        NioTcpServer acceptor = new NioTcpServer(strategy);
        acceptor.setFilters(new LoggingFilter("INCOMING"), new HttpServerCodec(), new LoggingFilter("DECODED"),
                new DummyHttpSever());

        acceptor.bind(new InetSocketAddress(8080));
        Thread.sleep(20000);
        acceptor.unbindAll();

    }

    private class DummyHttpSever implements IoFilter {

        @Override
        public void sessionCreated(IoSession session) {

        }

        @Override
        public void sessionOpened(IoSession session) {
            // TODO Auto-generated method stub

        }

        @Override
        public void sessionClosed(IoSession session) {
            // TODO Auto-generated method stub

        }

        @Override
        public void sessionIdle(IoSession session, IdleStatus status) {
            // TODO Auto-generated method stub

        }

        private HttpRequest incomingRequest;

        private List<ByteBuffer> body;

        @Override
        public void messageReceived(IoSession session, Object message, ReadFilterChainController controller) {
            if (message instanceof HttpRequest) {
                incomingRequest = (HttpRequest) message;
                body = new ArrayList<ByteBuffer>();

                // check if this request is going to be followed by and HTTP body or not
                if (incomingRequest.getMethod() != HttpMethod.POST && incomingRequest.getMethod() != HttpMethod.PUT) {
                    sendResponse(session, incomingRequest);
                } else {

                }
            } else if (message instanceof ByteBuffer) {
                body.add((ByteBuffer) message);
            } else if (message instanceof HttpEndOfContent) {
                // we received all the post content, send the crap back
                sendResponse(session, incomingRequest);
            }

        }

        public void sendResponse(IoSession session, HttpRequest request) {
            Map<String, String> headers = new HashMap<String, String>();
            headers.put("Server", "Apache MINA Dummy test server/0.0.");
            headers.put("Date", DateUtil.getCurrentAsString());
            headers.put("Connection", "Close");
            String strContent = "Hello ! we reply to request !";
            ByteBuffer content = ByteBuffer.wrap(strContent.getBytes());

            // compute content len
            headers.put("Content-Length", String.valueOf(content.remaining()));
            session.write(new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SUCCESS_OK, headers));
            session.write(content);
            session.write(new HttpEndOfContent());

        }

        @Override
        public void messageWriting(IoSession session, Object message, WriteFilterChainController controller) {
            // we just push the message in the chain
            controller.callWriteNextFilter(session, message);
        }

    }
}
