package com.juanmuscaria.web.boldo.xterm;

import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;
import org.teavm.jso.core.JSString;

@JSFunctor
public interface IEvent extends JSObject {
  JSObject onEvent(JSString data);
}
