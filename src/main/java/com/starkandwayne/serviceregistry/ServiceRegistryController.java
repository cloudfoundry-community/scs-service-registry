package com.starkandwayne.serviceregistry;

import java.util.LinkedList;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cf-config")
public class ServiceRegistryController {
    
    @GetMapping(
        value = "/session-data",
        produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<CloudFoundrySessionData> getCfSessionData() {
        return ResponseEntity.ok().body(CloudFoundrySessionData.GetEnvironment());
    }

    @GetMapping(
        value = "/peers",
        produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Peers> getPeers() {
        return ResponseEntity.ok().body(ServiceRegistryApplication.CurrentLoadedPeers);
        }

    @PostMapping(
        value = "/peers",
        consumes = {MediaType.APPLICATION_JSON_VALUE},
        produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Peers> postPeers(@RequestBody LinkedList<PeerData> listpeers) {
        Peers peers = new Peers();
        peers.CurrentPeers = listpeers;
        ServiceRegistryApplication.CurrentLoadedPeers = peers;
        ServiceRegistryApplication.RestartApplication();
        return ResponseEntity.ok().body(ServiceRegistryApplication.CurrentLoadedPeers);
        }
}
