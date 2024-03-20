package org.mbari.charybdis.domain;

import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.Media;

import java.util.List;

/**
 * @author Brian Schlining
 * @since 2019-10-04T11:13:00
 */
//public class DataGroup {
//  private final List<Annotation> annotations;
//  private final List<Media> media;
//
//  public DataGroup(List<Annotation> annotations, List<Media> media) {
//    this.annotations = annotations;
//    this.media = media;
//  }
//
//  public List<Annotation> getAnnotations() {
//    return annotations;
//  }
//
//  public List<Media> getMedia() {
//    return media;
//  }
//}

public record DataGroup(List<Annotation> annotations, List<Media> media) {
  public List<Media> getMedia() {
    return media;
  }
}
