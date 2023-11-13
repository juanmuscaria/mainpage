package com.juanmuscaria.web.boldo;

public class Client {

  public static void main(String[] args) {
    if (args.length > 0) {
      switch (args[0]) {
        case "testPage":
          BoldoTest.boldoTest();
          break;
        case "teapot/index":
          Boldo.teapot$index();
        default:
          break;
      }
    }
  }
}
