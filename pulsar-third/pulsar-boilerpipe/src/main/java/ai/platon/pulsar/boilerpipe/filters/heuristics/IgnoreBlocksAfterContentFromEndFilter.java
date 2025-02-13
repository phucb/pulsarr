package ai.platon.pulsar.boilerpipe.filters.heuristics;

import ai.platon.pulsar.boilerpipe.document.BlockLabels;
import ai.platon.pulsar.boilerpipe.document.TextBlock;
import ai.platon.pulsar.boilerpipe.document.BoiTextDocument;
import ai.platon.pulsar.boilerpipe.filters.TextBlockFilter;
import ai.platon.pulsar.boilerpipe.utils.ProcessingException;

import java.util.List;
import java.util.ListIterator;

/**
 * Marks all blocks as "non-content" that occur after blocks that have been marked
 * {@link BlockLabels#INDICATES_END_OF_TEXT}, and after any content block. This filter can be used
 * in conjunction with an upstream {@link TerminatingBlocksFinder}.
 *
 * @see TerminatingBlocksFinder
 */
public final class IgnoreBlocksAfterContentFromEndFilter extends HeuristicFilterBase implements
    TextBlockFilter {
  public static final IgnoreBlocksAfterContentFromEndFilter INSTANCE =
      new IgnoreBlocksAfterContentFromEndFilter();

  private IgnoreBlocksAfterContentFromEndFilter() {
  }

  public boolean process(BoiTextDocument doc) throws ProcessingException {
    boolean changes = false;

    int words = 0;

    List<TextBlock> blocks = doc.getTextBlocks();
    if (!blocks.isEmpty()) {
      ListIterator<TextBlock> it = blocks.listIterator(blocks.size());

      TextBlock tb;

      while (it.hasPrevious()) {
        tb = it.previous();
        if (tb.hasLabel(BlockLabels.INDICATES_END_OF_TEXT)) {
          tb.addLabel(BlockLabels.STRICTLY_NOT_CONTENT);
          tb.removeLabel(BlockLabels.MIGHT_BE_CONTENT);
          tb.setIsContent(false);
          changes = true;
        } else if (tb.isContent()) {
          words += tb.getNumWords();
          if (words > 200) {
            break;
          }
        }

      }
    }

    return changes;
  }
}
