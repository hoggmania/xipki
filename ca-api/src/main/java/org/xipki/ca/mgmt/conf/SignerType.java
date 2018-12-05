/*
 *
 * Copyright (c) 2013 - 2018 Lijun Liao
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

package org.xipki.ca.mgmt.conf;

import org.xipki.util.FileOrBinary;
import org.xipki.util.FileOrValue;
import org.xipki.util.InvalidConfException;
import org.xipki.util.ValidatableConf;

/**
 * TODO.
 * @author Lijun Liao
 */

public class SignerType extends ValidatableConf {

  private String type;

  private FileOrValue conf;

  private FileOrBinary cert;

  private String name;

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public FileOrValue getConf() {
    return conf;
  }

  public void setConf(FileOrValue conf) {
    this.conf = conf;
  }

  public FileOrBinary getCert() {
    return cert;
  }

  public void setCert(FileOrBinary cert) {
    this.cert = cert;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public void validate() throws InvalidConfException {
    notEmpty(name, "name");
    notEmpty(type, "type");
    notNull(conf, "conf");
    validate(conf);
    validate(cert);
  }

}
