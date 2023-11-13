package com.juanmuscaria.web.boldo;

import com.juanmuscaria.web.boldo.xterm.XTermJsTerminal;
import lombok.SneakyThrows;
import org.teavm.jso.dom.html.HTMLDocument;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class BoldoTest {
  @SneakyThrows
  static void boldoTest() {
    var document = HTMLDocument.current();
    var root = document.createElement("div");
    root.setId("root");
    root.appendChild(document.createTextNode("Runtime generated element"));
    document.getBody().appendChild(root);


    var termTag = document.createElement("div");
    termTag.setId("terminal");
    document.getBody().appendChild(termTag);

    var terminalInputStream = new PipedInputStream();
    var pipeOut = new PipedOutputStream(terminalInputStream);

    var jsTerminal = XTermJsTerminal.create();
    jsTerminal.onBinary(data -> {
      try {
        pipeOut.write(data.stringValue().getBytes(StandardCharsets.UTF_8));
      } catch (IOException ignored) {
      }
      return null;
    });
    jsTerminal.onData(data -> {
      try {
        pipeOut.write(data.stringValue().getBytes(StandardCharsets.UTF_8));
      } catch (IOException ignored) {
      }
      return null;
    });
    jsTerminal.open(termTag);

    var terminalPrinter = new PrintStream(new TerminalOut(jsTerminal));
    System.setOut(terminalPrinter);

    System.out.println("Terminal output stream\r");
    var buffer = new StringBuilder();
    new Thread(() -> {
      try {
        while (true) {
          var value = terminalInputStream.read();
          if (value == '\r') {
            System.out.print("\r\n");
            var args = buffer.toString().split(" ");
            if (args.length > 0) {
              var cmdArgs = Arrays.copyOfRange(args, 1, args.length);
              int exitCode = -1;
//          switch (args[0].toLowerCase()) {
//            case "test": {
//              exitCode = new CommandLine(new TestCommand()).execute(cmdArgs);
//              break;
//            }
//          }
//          System.out.println("Command exited with code: " + exitCode + "\r");
            }
            buffer.setLength(0);
          } else {
            System.out.write(value);
            buffer.append((char) value);
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }).start();

  }

  private static class TerminalOut extends OutputStream {
    private final XTermJsTerminal terminal;

    private TerminalOut(XTermJsTerminal terminal) {
      this.terminal = terminal;
    }

    @Override
    public void write(int b) {
      terminal.write(String.valueOf((char) b));
    }
  }
}
