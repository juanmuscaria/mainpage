package com.juanmuscaria.web.boldo;

import org.teavm.jso.dom.html.HTMLDocument;

public class Client {

  public static void main(String[] args) {
    var document = HTMLDocument.current();
    var root = document.createElement("root");
    root.setId("root");
    root.appendChild(document.createTextNode("Hello from java running in the browser"));
    document.getBody().appendChild(root);
    System.out.println("Look mom I'm running java on the browser.");
  }
}
