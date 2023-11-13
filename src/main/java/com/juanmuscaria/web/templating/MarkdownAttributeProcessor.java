package com.juanmuscaria.web.templating;

import org.commonmark.Extension;
import org.commonmark.ext.autolink.AutolinkExtension;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.ext.heading.anchor.HeadingAnchorExtension;
import org.commonmark.ext.image.attributes.ImageAttributesExtension;
import org.commonmark.ext.ins.InsExtension;
import org.commonmark.ext.task.list.items.TaskListItemsExtension;
import org.commonmark.node.Document;
import org.commonmark.node.Node;
import org.commonmark.node.Paragraph;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.NodeRenderer;
import org.commonmark.renderer.html.CoreHtmlNodeRenderer;
import org.commonmark.renderer.html.HtmlNodeRendererContext;
import org.commonmark.renderer.html.HtmlRenderer;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.engine.AttributeName;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.standard.processor.AbstractStandardExpressionAttributeTagProcessor;
import org.thymeleaf.templatemode.TemplateMode;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class MarkdownAttributeProcessor extends AbstractStandardExpressionAttributeTagProcessor {

  private static final String ATTR_NAME = "markdown";
  private static final int PRECEDENCE = 10000;
  // TODO: Use beans 🫘
  private final List<Extension> extensions = Arrays.asList(TaskListItemsExtension.create(),
    ImageAttributesExtension.create(), TablesExtension.create(), StrikethroughExtension.create(),
    InsExtension.create(), HeadingAnchorExtension.create(), AutolinkExtension.create());
  private final Parser markdownParser = Parser.builder().extensions(extensions).build();
  private final HtmlRenderer renderer = HtmlRenderer.builder().nodeRendererFactory((SkipParentWrapperParagraphsRenderer::new))
    .extensions(extensions).build();

  protected MarkdownAttributeProcessor(final String dialectPrefix) {
    super(TemplateMode.HTML, dialectPrefix, ATTR_NAME, PRECEDENCE, true);
  }

  @Override
  protected void doProcess(final ITemplateContext context, final IProcessableElementTag tag,
                           final AttributeName attributeName, final String attributeValue,
                           final Object expressionResult, final IElementTagStructureHandler structureHandler) {

    var html = renderer.render(markdownParser.parse(expressionResult.toString()));
    structureHandler.setBody(html, false);
  }

  private static class SkipParentWrapperParagraphsRenderer extends CoreHtmlNodeRenderer implements NodeRenderer {

    public SkipParentWrapperParagraphsRenderer(HtmlNodeRendererContext context) {
      super(context);
    }

    @Override
    public Set<Class<? extends Node>> getNodeTypes() {
      return Collections.singleton(Paragraph.class);
    }

    @Override
    public void render(Node node) {
      if (node.getParent() instanceof Document) {
        visitChildren(node);
      } else {
        visit((Paragraph) node);
      }
    }
  }
}
