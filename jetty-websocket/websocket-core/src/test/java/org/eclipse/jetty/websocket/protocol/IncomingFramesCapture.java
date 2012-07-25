// ========================================================================
// Copyright 2011-2012 Mort Bay Consulting Pty. Ltd.
// ------------------------------------------------------------------------
// All rights reserved. This program and the accompanying materials
// are made available under the terms of the Eclipse Public License v1.0
// and Apache License v2.0 which accompanies this distribution.
//
//     The Eclipse Public License is available at
//     http://www.eclipse.org/legal/epl-v10.html
//
//     The Apache License v2.0 is available at
//     http://www.opensource.org/licenses/apache2.0.php
//
// You may elect to redistribute this code under either of these licenses.
//========================================================================
package org.eclipse.jetty.websocket.protocol;

import static org.hamcrest.Matchers.*;

import java.util.LinkedList;

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.websocket.api.WebSocketException;
import org.eclipse.jetty.websocket.io.IncomingFrames;
import org.junit.Assert;

public class IncomingFramesCapture implements IncomingFrames
{
    private static final Logger LOG = Log.getLogger(IncomingFramesCapture.class);
    private LinkedList<WebSocketFrame> frames = new LinkedList<>();
    private LinkedList<WebSocketException> errors = new LinkedList<>();

    public void assertErrorCount(int expectedCount)
    {
        Assert.assertThat("Captured error count",errors.size(),is(expectedCount));
    }

    public void assertFrameCount(int expectedCount)
    {
        Assert.assertThat("Captured frame count",frames.size(),is(expectedCount));
    }

    public void assertHasErrors(Class<? extends WebSocketException> errorType, int expectedCount)
    {
        Assert.assertThat(errorType.getSimpleName(),getErrorCount(errorType),is(expectedCount));
    }

    public void assertHasFrame(OpCode op)
    {
        Assert.assertThat(op.name(),getFrameCount(op),greaterThanOrEqualTo(1));
    }

    public void assertHasFrame(OpCode op, int expectedCount)
    {
        Assert.assertThat(op.name(),getFrameCount(op),is(expectedCount));
    }

    public void assertHasNoFrames()
    {
        Assert.assertThat("Has no frames",frames.size(),is(0));
    }

    public void assertNoErrors()
    {
        Assert.assertThat("Has no errors",errors.size(),is(0));
    }

    public int getErrorCount(Class<? extends WebSocketException> errorType)
    {
        int count = 0;
        for(WebSocketException error: errors) {
            if (errorType.isInstance(error))
            {
                count++;
            }
        }
        return count;
    }

    public LinkedList<WebSocketException> getErrors()
    {
        return errors;
    }

    public int getFrameCount(OpCode op)
    {
        int count = 0;
        for(WebSocketFrame frame: frames) {
            if (frame.getOpCode() == op)
            {
                count++;
            }
        }
        return count;
    }

    public LinkedList<WebSocketFrame> getFrames()
    {
        return frames;
    }

    @Override
    public void incoming(WebSocketException e)
    {
        LOG.debug(e);
        errors.add(e);
    }

    @Override
    public void incoming(WebSocketFrame frame)
    {
        frames.add(frame);
    }
}