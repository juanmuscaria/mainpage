package com.juanmuscaria.web.mainpage.templating;

import org.commonmark.Extension;
import org.commonmark.ext.autolink.AutolinkExtension;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.ext.heading.anchor.HeadingAnchorExtension;
import org.commonmark.ext.image.attributes.ImageAttributesExtension;
import org.commonmark.ext.ins.InsExtension;
import org.commonmark.ext.ins.internal.InsTextContentNodeRenderer;
import org.commonmark.ext.task.list.items.TaskListItemsExtension;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.engine.AttributeName;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.standard.processor.AbstractStandardExpressionAttributeTagProcessor;
import org.thymeleaf.templatemode.TemplateMode;

import java.util.Arrays;
import java.util.List;

public class MarkdownAttributeProcessor extends AbstractStandardExpressionAttributeTagProcessor {

  private static final String ATTR_NAME = "markdown";
  private static final int PRECEDENCE = 10000;
  // TODO: Use beans
  private final List<Extension> extensions = Arrays.asList(TaskListItemsExtension.create(),
    ImageAttributesExtension.create(), TablesExtension.create(), StrikethroughExtension.create(),
    InsExtension.create(), HeadingAnchorExtension.create(), AutolinkExtension.create());
  private final Parser markdownParser = Parser.builder().extensions(extensions).build();
  private final HtmlRenderer renderer = HtmlRenderer.builder().extensions(extensions).build();

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
}
