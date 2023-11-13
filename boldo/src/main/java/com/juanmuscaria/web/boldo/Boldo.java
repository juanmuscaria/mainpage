package com.juanmuscaria.web.boldo;

import lombok.SneakyThrows;
import org.teavm.jso.dom.html.HTMLDocument;

public class Boldo {
  @SneakyThrows
  public static void teapot$index() {
    var document = HTMLDocument.current();
    var page = document.getElementById("page-body");
    page.setAttribute("hidden", "");
    //Thread.sleep(2000L);
    page.removeAttribute("hidden");
  }
}
