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
package org.apache.mina.api;

import java.net.SocketAddress;

/**
 * Connects to endpoint, communicates with the server, and fires events to
 * {@link org.apache.mina.service.IoHandler}s.
 *
 * @author <a href="http://mina.apache.org">Apache MINA Project</a>
 */
public interface IoClient extends IoService {
    /**
     * Returns the connect timeout in milliseconds. The default value is 1
     * minute.
     *
     * @return the connect timeout in milliseconds
     */
    long getConnectTimeoutMillis();

    /**
     * Sets the connect timeout in milliseconds. The default value is 1 minute.
     *
     * @param connectTimeoutInMillis Connection timeout in ms
     */
    void setConnectTimeoutMillis(long connectTimeoutInMillis);

    /**
     * Connects to the specified remote address.
     *
     * @param remoteAddress Remote {@link SocketAddress} to connect
     * @return the {@link IoFuture} instance which is completed when the
     *         connection attempt initiated by this call succeeds or fails.
     */
    IoFuture<IoSession> connect(SocketAddress remoteAddress);

    /**
     * Connects to the specified remote address binding to the specified local
     * address.
     *
     * @param remoteAddress Remote {@link SocketAddress} to connect
     * @param localAddress  Local {@link SocketAddress} to use while initiating connection to
     *                      remote {@link SocketAddress}
     * @return the {@link IoFuture} instance which is completed when the
     *         connection attempt initiated by this call succeeds or fails.
     */
    IoFuture<IoSession> connect(SocketAddress remoteAddress, SocketAddress localAddress);
}
