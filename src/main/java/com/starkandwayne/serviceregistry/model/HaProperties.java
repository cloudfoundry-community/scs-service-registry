package com.starkandwayne.serviceregistry.model;


import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class HaProperties {
   private URI uri;
   private int nodes = 1;
   private List peers = new ArrayList();

   public URI getUri() {
      return this.uri;
   }

   public void setUri(URI uri) {
      this.uri = uri;
   }

   public int getNodes() {
      return this.nodes;
   }

   public void setNodes(int nodes) {
      this.nodes = nodes;
   }

   public List getPeers() {
      return this.peers;
   }

   public void setPeers(List peers) {
      this.peers = peers;
   }
}
