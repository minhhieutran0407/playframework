/*
 * Copyright (C) 2009-2019 Lightbend Inc. <https://www.lightbend.com>
 */
package play.i18n;

import org.apache.commons.lang3.ArrayUtils;
import play.libs.Scala;
import play.mvc.Http;
import scala.collection.Seq;
import scala.collection.mutable.Buffer;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/** The messages API. */
@Singleton
public class MessagesApi {

  private final play.api.i18n.MessagesApi messages;

  @Inject
  public MessagesApi(play.api.i18n.MessagesApi messages) {
    this.messages = messages;
  }

  /**
   * @return the Scala versions of the Messages API.
   * @deprecated As of release 2.6.0. Use {@link #asScala()}
   */
  @Deprecated
  public play.api.i18n.MessagesApi scalaApi() {
    return asScala();
  }

  /** @return the Scala versions of the Messages API. */
  public play.api.i18n.MessagesApi asScala() {
    return messages;
  }

  /**
   * Converts the varargs to a scala buffer, takes care of wrapping varargs into a intermediate list
   * if necessary
   *
   * @param args the message arguments
   * @return scala type for message processing
   */
  private static Buffer<Object> convertArgsToScalaBuffer(final Object... args) {
    return scala.collection.JavaConverters.asScalaBufferConverter(wrapArgsToListIfNeeded(args))
        .asScala();
  }

  /**
   * Wraps arguments passed into a list if necessary.
   *
   * <p>Returns the first value as is if it is the only argument and a subtype of `java.util.List`
   * Otherwise, it calls Arrays.asList on args
   *
   * @param args arguments as a List
   */
  @SafeVarargs
  private static <T> List<T> wrapArgsToListIfNeeded(final T... args) {
    List<T> out;
    if (ArrayUtils.isNotEmpty(args) && args.length == 1 && args[0] instanceof List) {
      out = (List<T>) args[0];
    } else {
      out = Arrays.asList(args);
    }
    return out;
  }

  /**
   * Translates a message.
   *
   * <p>Uses `java.text.MessageFormat` internally to format the message.
   *
   * @param lang the message lang
   * @param key the message key
   * @param args the message arguments
   * @return the formatted message or a default rendering if the key wasn't defined
   */
  public String get(play.api.i18n.Lang lang, String key, Object... args) {
    Buffer<Object> scalaArgs = convertArgsToScalaBuffer(args);
    return messages.apply(key, scalaArgs, lang);
  }

  /**
   * Translates the first defined message.
   *
   * <p>Uses `java.text.MessageFormat` internally to format the message.
   *
   * @param lang the message lang
   * @param keys the messages keys
   * @param args the message arguments
   * @return the formatted message or a default rendering if the key wasn't defined
   */
  public String get(play.api.i18n.Lang lang, List<String> keys, Object... args) {
    Buffer<String> keyArgs = scala.collection.JavaConverters.asScalaBufferConverter(keys).asScala();
    Buffer<Object> scalaArgs = convertArgsToScalaBuffer(args);
    return messages.apply(keyArgs.toSeq(), scalaArgs, lang);
  }

  /**
   * Check if a message key is defined.
   *
   * @param lang the message lang
   * @param key the message key
   * @return a Boolean
   */
  public Boolean isDefinedAt(play.api.i18n.Lang lang, String key) {
    return messages.isDefinedAt(key, lang);
  }

  /**
   * Get a messages context appropriate for the given candidates.
   *
   * <p>Will select a language from the candidates, based on the languages available, and fallback
   * to the default language if none of the candidates are available.
   *
   * @param candidates the candidate languages
   * @return the most appropriate Messages instance given the candidate languages
   */
  public Messages preferred(Collection<Lang> candidates) {
    Seq<Lang> cs = Scala.asScala(candidates);
    play.api.i18n.Messages msgs = messages.preferred((Seq) cs);
    return new MessagesImpl(new Lang(msgs.lang()), this);
  }

  /**
   * Get a messages context appropriate for the given request.
   *
   * <p>Will select a language from the request, based on the languages available, and fallback to
   * the default language if none of the candidates are available.
   *
   * @param request the incoming request
   * @return the preferred messages context for the request
   */
  public Messages preferred(Http.RequestHeader request) {
    play.api.i18n.Messages msgs = messages.preferred(request);
    return new MessagesImpl(new Lang(msgs.lang()), this);
  }

  public String langCookieName() {
    return messages.langCookieName();
  }

  public boolean langCookieSecure() {
    return messages.langCookieSecure();
  }

  public boolean langCookieHttpOnly() {
    return messages.langCookieHttpOnly();
  }
}
