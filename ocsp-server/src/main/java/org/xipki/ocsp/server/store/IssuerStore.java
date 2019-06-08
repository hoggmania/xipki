/*
 *
 * Copyright (c) 2013 - 2019 Lijun Liao
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.xipki.ocsp.server.store;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xipki.ocsp.api.RequestIssuer;

/**
 * Issuer store.
 *
 * @author Lijun Liao
 * @since 2.0.0
 */

class IssuerStore {

  private List<IssuerEntry> issuers = new ArrayList<>();

  private Set<Integer> ids = Collections.emptySet();

  private Map<Integer, CrlInfo> crlInfos = new HashMap<>();

  public IssuerStore() {
  }

  public void setIssuers(List<IssuerEntry> issuers) {
    Set<Integer> newIds = new HashSet<>();
    for (IssuerEntry issuer : issuers) {
      int id = issuer.getId();
      if (newIds.contains(id)) {
        throw new IllegalArgumentException(
            "issuer with the same id " + id + " duplicated");
      }
      newIds.add(id);
    }

    // to accelerate the switch
    List<IssuerEntry> copy = new ArrayList<IssuerEntry>(issuers);
    this.ids = Collections.unmodifiableSet(newIds);
    this.issuers = copy;
  }

  public int size() {
    return ids.size();
  }

  public Set<Integer> getIds() {
    return ids;
  }

  public Integer getIssuerIdForFp(RequestIssuer reqIssuer) {
    IssuerEntry issuerEntry = getIssuerForFp(reqIssuer);
    return (issuerEntry == null) ? null : issuerEntry.getId();
  }

  public IssuerEntry getIssuerForId(int id) {
    for (IssuerEntry entry : issuers) {
      if (entry.getId() == id) {
        return entry;
      }
    }

    return null;
  }

  public IssuerEntry getIssuerForFp(RequestIssuer reqIssuer) {
    for (IssuerEntry entry : issuers) {
      if (entry.matchHash(reqIssuer)) {
        return entry;
      }
    }

    return null;
  }

  public void addIssuer(IssuerEntry issuer) {
    this.issuers.add(issuer);

    Set<Integer> newIds = new HashSet<>(this.ids);
    newIds.add(issuer.getId());
    this.ids = Collections.unmodifiableSet(newIds);
  }

  public void setCrlInfos(Map<Integer, CrlInfo> crlInfos) {
    this.crlInfos = crlInfos == null
        ? Collections.emptyMap() : new HashMap<Integer, CrlInfo>(crlInfos);
  }

  public CrlInfo getCrlInfo(int crlInfoId) {
    return crlInfos.get(crlInfoId);
  }

  public Set<Integer> getCrlIds() {
    return Collections.unmodifiableSet(crlInfos.keySet());
  }
}
