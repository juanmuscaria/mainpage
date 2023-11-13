package com.juanmuscaria.web.boldo.xterm;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;
import org.w3c.dom.html.HTMLElement;

public abstract class XTermJsTerminal implements JSObject {
  public abstract void open(JSObject element);

  public abstract void open(HTMLElement element);

  @JSProperty
  public native void setOptions(Options options);

  public abstract void write(String data);

  //  @JSBody(params = { "callback" }, script = "onBinary(callback);")
  public abstract void onBinary(IEvent callback);

  //  @JSBody(params = { "callback" }, script = "this.onData(callback);")
  public abstract void onData(IEvent callback);

  public abstract void dispose();

  @JSBody(params = {"options"}, script = "return new Terminal(options);")
  public static native XTermJsTerminal create(Options options);

  @JSBody(script = "return new Terminal();")
  public static native XTermJsTerminal create();
}
