// ========================================================================
// Copyright 2011-2012 Mort Bay Consulting Pty. Ltd.
// ------------------------------------------------------------------------
// All rights reserved. This program and the accompanying materials
// are made available under the terms of the Eclipse Public License v1.0
// and Apache License v2.0 which accompanies this distribution.
// The Eclipse Public License is available at
// http://www.eclipse.org/legal/epl-v10.html
// The Apache License v2.0 is available at
// http://www.opensource.org/licenses/apache2.0.php
// You may elect to redistribute this code under either of these licenses.
// ========================================================================

package org.eclipse.jetty.spdy;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.List;

import org.eclipse.jetty.io.AbstractAsyncConnection;
import org.eclipse.jetty.io.AsyncConnection;
import org.eclipse.jetty.io.AsyncEndPoint;
import org.eclipse.jetty.npn.NextProtoNego;
import org.eclipse.jetty.util.BufferUtil;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

public class NextProtoNegoServerAsyncConnection extends AbstractAsyncConnection implements NextProtoNego.ServerProvider
{
    private final Logger logger = Log.getLogger(getClass());
    private final SocketChannel channel;
    private final SPDYServerConnector connector;
    private volatile boolean completed;

    public NextProtoNegoServerAsyncConnection(SocketChannel channel, AsyncEndPoint endPoint, SPDYServerConnector connector)
    {
        super(endPoint, connector.findExecutor());
        this.channel = channel;
        this.connector = connector;
    }

    @Override
    public void onFillable()
    {
        while (true)
        {
            int filled = fill();
            if (filled == 0 && !completed)
                fillInterested();
            if (filled <= 0)
                break;
        }
    }

    private int fill()
    {
        try
        {
            return getEndPoint().fill(BufferUtil.EMPTY_BUFFER);
        }
        catch (IOException x)
        {
            logger.debug(x);
            getEndPoint().close();
            return -1;
        }
    }

    @Override
    public void unsupported()
    {
        AsyncConnectionFactory asyncConnectionFactory = connector.getDefaultAsyncConnectionFactory();
        AsyncEndPoint endPoint = getEndPoint();
        AsyncConnection connection = asyncConnectionFactory.newAsyncConnection(channel, endPoint, connector);
        connector.replaceAsyncConnection(endPoint, connection);
        completed = true;
    }

    @Override
    public List<String> protocols()
    {
        return connector.provideProtocols();
    }

    @Override
    public void protocolSelected(String protocol)
    {
        AsyncConnectionFactory asyncConnectionFactory = connector.getAsyncConnectionFactory(protocol);
        AsyncEndPoint endPoint = getEndPoint();
        AsyncConnection connection = asyncConnectionFactory.newAsyncConnection(channel, endPoint, connector);
        connector.replaceAsyncConnection(endPoint, connection);
        completed = true;
    }
}