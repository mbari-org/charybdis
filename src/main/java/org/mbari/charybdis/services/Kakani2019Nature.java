package org.mbari.charybdis.services;

import io.helidon.webserver.Routing.Rules;
import io.helidon.common.http.MediaType;
import io.helidon.webserver.ServerRequest;
import io.helidon.webserver.ServerResponse;
import io.helidon.webserver.Service;
import org.mbari.charybdis.DataGroup;
// import org.mbari.jcommons.util.Logging;

// import java.util.logging.Logger;

/**
 * Kakani2019Nature
 */
public class Kakani2019Nature implements Service {

  private final Annosaurus annosaurus;
  private final VampireSquid vampireSquid;
  // private final Logging log = new Logging(getClass());

  public Kakani2019Nature(Annosaurus annosaurus, VampireSquid vampireSquid) {
    this.annosaurus = annosaurus;
    this.vampireSquid = vampireSquid;
  }

  @Override
  public void update(Rules rules) {
    rules.get("/", this::defaultHandler);
  }

  private void defaultHandler(ServerRequest request, ServerResponse response) {
    // Get annotations, then get media, then package them together
    response.headers().contentType(MediaType.APPLICATION_JSON);
    annosaurus.findByLinkNameAndLinkValue("comment", "Nature20190609559")
        .thenApply(as -> vampireSquid.findMediaForAnnotations(as)
                .thenApply(ms -> new DataGroup(as, ms))
                .thenApply(obj -> annosaurus.getGson().toJson(obj))
                .thenAccept(response::send));

  }



}