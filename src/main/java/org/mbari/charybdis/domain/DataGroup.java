package org.mbari.charybdis.domain;


import org.mbari.vars.annosaurus.sdk.r1.models.Annotation;
import org.mbari.vars.vampiresquid.sdk.r1.models.Media;

import java.util.List;

/**
 * @author Brian Schlining
 * @since 2019-10-04T11:13:00
 */

public record DataGroup(List<Annotation> annotations, List<Media> media) {
  public List<Media> getMedia() {
    return media;
  }
}
