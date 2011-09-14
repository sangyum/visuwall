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
package org.apache.mina.transport.udp;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.Set;

import org.apache.mina.service.server.AbstractIoServer;

/**
 * TODO
 * 
 * @author <a href="http://mina.apache.org">Apache MINA Project</a>
 */
public class AbstractUdpServer extends AbstractIoServer {
    /**
     * Create an new AbsractUdpServer instance
     */
    protected AbstractUdpServer() {
        super();
    }

    @Override
    public Set<SocketAddress> getLocalAddresses()
    {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public void bind( SocketAddress... localAddress ) throws IOException
    {
        // TODO Auto-generated method stub

    }


    @Override
    public void unbindAll() throws IOException
    {
        // TODO Auto-generated method stub

    }


    @Override
    public void unbind( SocketAddress... localAddresses ) throws IOException
    {
        // TODO Auto-generated method stub

    }

}
