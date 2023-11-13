package com.juanmuscaria.web.boldo.xterm;

import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;

public abstract class Options implements JSObject {
  @JSProperty
  public native void setCols(int cols);

  @JSProperty
  public native void setRows(int rows);

}
