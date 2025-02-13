package ai.platon.pulsar.boilerpipe.filters;

import ai.platon.pulsar.boilerpipe.document.BoiTextDocument;
import ai.platon.pulsar.boilerpipe.utils.ProcessingException;

/**
 * A generic {@link TextBlockFilter}. Takes a {@link BoiTextDocument} and processes it somehow.
 */
public interface TextBlockFilter {
  /**
   * Processes the given document <code>doc</code>.
   *
   * @param doc The {@link BoiTextDocument} that is to be processed.
   * @return <code>true</code> if changes have been made to the {@link BoiTextDocument}.
   * @throws ProcessingException
   */
  boolean process(final BoiTextDocument doc) throws ProcessingException;
}
