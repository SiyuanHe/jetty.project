package org.eclipse.jetty.websocket.api;

import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.websocket.api.samples.AdapterConnectCloseSocket;
import org.eclipse.jetty.websocket.api.samples.AnnotatedBasicSocket;
import org.eclipse.jetty.websocket.api.samples.AnnotatedByteArraySocket;
import org.eclipse.jetty.websocket.api.samples.ListenerBasicSocket;
import org.eclipse.jetty.websocket.frames.BinaryFrame;
import org.eclipse.jetty.websocket.frames.CloseFrame;
import org.eclipse.jetty.websocket.frames.TextFrame;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

public class WebSocketEventDriverTest
{
    @Rule
    public TestName testname = new TestName();

    private WebSocketEventDriver newDriver(Object websocket)
    {
        EventMethodsCache methodsCache = new EventMethodsCache();
        methodsCache.register(websocket.getClass());
        WebSocketPolicy policy = WebSocketPolicy.newServerPolicy();
        return new WebSocketEventDriver(methodsCache,policy,websocket);
    }

    @Test
    public void testAdapter_ConnectClose()
    {
        AdapterConnectCloseSocket socket = new AdapterConnectCloseSocket();
        WebSocketEventDriver driver = newDriver(socket);

        LocalWebSocketConnection conn = new LocalWebSocketConnection(testname);
        driver.setConnection(conn);
        driver.onConnect();
        driver.onFrame(new CloseFrame(StatusCode.NORMAL));

        socket.capture.assertEventCount(2);
        socket.capture.assertEventStartsWith(0,"onWebSocketConnect");
        socket.capture.assertEventStartsWith(1,"onWebSocketClose");
    }

    @Test
    public void testAnnotated_ByteArray()
    {
        AnnotatedByteArraySocket socket = new AnnotatedByteArraySocket();
        WebSocketEventDriver driver = newDriver(socket);

        LocalWebSocketConnection conn = new LocalWebSocketConnection(testname);
        driver.setConnection(conn);
        driver.onConnect();
        driver.onFrame(new BinaryFrame("Hello World".getBytes(StringUtil.__UTF8_CHARSET)));
        driver.onFrame(new CloseFrame(StatusCode.NORMAL));

        socket.capture.assertEventCount(3);
        socket.capture.assertEventStartsWith(0,"onConnect");
        socket.capture.assertEvent(1,"onBinary([11],0,11)");
        socket.capture.assertEventStartsWith(2,"onClose(1000,");
    }

    @Test
    public void testAnnotated_ByteBuffer()
    {
        AnnotatedBasicSocket socket = new AnnotatedBasicSocket();
        WebSocketEventDriver driver = newDriver(socket);

        LocalWebSocketConnection conn = new LocalWebSocketConnection(testname);
        driver.setConnection(conn);
        driver.onConnect();
        driver.onFrame(new BinaryFrame("Hello World".getBytes(StringUtil.__UTF8_CHARSET)));
        driver.onFrame(new CloseFrame(StatusCode.NORMAL));

        socket.capture.assertEventCount(3);
        socket.capture.assertEventStartsWith(0,"onConnect");
        socket.capture.assertEvent(1,"onBinary(java.nio.HeapByteBuffer[pos=0 lim=11 cap=11])");
        socket.capture.assertEventStartsWith(2,"onClose(1000,");
    }

    @Test
    public void testListener_Text()
    {
        ListenerBasicSocket socket = new ListenerBasicSocket();
        WebSocketEventDriver driver = newDriver(socket);

        LocalWebSocketConnection conn = new LocalWebSocketConnection(testname);
        driver.setConnection(conn);
        driver.onConnect();
        driver.onFrame(new TextFrame("Hello World"));
        driver.onFrame(new CloseFrame(StatusCode.NORMAL));

        socket.capture.assertEventCount(3);
        socket.capture.assertEventStartsWith(0,"onWebSocketConnect");
        socket.capture.assertEventStartsWith(1,"onWebSocketText(\"Hello World\")");
        socket.capture.assertEventStartsWith(2,"onWebSocketClose(1000,");
    }
}