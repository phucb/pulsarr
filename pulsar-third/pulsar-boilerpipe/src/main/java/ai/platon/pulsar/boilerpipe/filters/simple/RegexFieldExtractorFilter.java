package ai.platon.pulsar.boilerpipe.filters.simple;

import ai.platon.pulsar.boilerpipe.document.TextBlock;
import ai.platon.pulsar.boilerpipe.document.BoiTextDocument;
import ai.platon.pulsar.boilerpipe.filters.TextBlockFilter;
import ai.platon.pulsar.boilerpipe.utils.ProcessingException;
import ai.platon.pulsar.boilerpipe.utils.ScentUtils;
import com.google.common.collect.ListMultimap;

import java.util.Map;

/**
 * Marks all blocks that contain a given label as "content".
 *
 * TODO : Combine with ArticleMetadataFilter
 */
public final class RegexFieldExtractorFilter implements TextBlockFilter {

  // TODO : configurable

  private int keyGroup = 1;
  private int valueGroup = 2;

  private final int maxTextLength;
  private final ListMultimap<String, String> regexFieldRules;

  public RegexFieldExtractorFilter(final ListMultimap<String, String> regexFieldRules, int maxTextLength) {
    this.regexFieldRules = regexFieldRules;
    this.maxTextLength = maxTextLength;
  }

  public boolean process(BoiTextDocument doc) throws ProcessingException {
    boolean changes = false;

    for (final TextBlock tb : doc.getTextBlocks()) {
      if (!tb.isContent()) {
        continue;
      }

      String text = tb.getText();
      if (text.length() < maxTextLength) {
        changes = tryExtractText(doc, text);
      } else {
        // try extract from the beginning
        changes = tryExtractText(doc, text.substring(0, maxTextLength));
        // try extract from the ending
        changes |= tryExtractText(doc, text.substring(text.length() - maxTextLength, text.length()));
      }
    }

    return changes;
  }

  private boolean tryExtractText(BoiTextDocument doc, String text) {
    Map<String, String> results = ScentUtils.extract(text, regexFieldRules, keyGroup, valueGroup);
    results.forEach(doc::setField);

    return !results.isEmpty();
  }
}
