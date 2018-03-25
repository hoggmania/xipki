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

package org.xipki.ca.dbtool.port.ca;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.xipki.ca.dbtool.jaxb.ca.CAConfigurationType;
import org.xipki.ca.dbtool.jaxb.ca.CAConfigurationType.CaHasProfiles;
import org.xipki.ca.dbtool.jaxb.ca.CAConfigurationType.CaHasPublishers;
import org.xipki.ca.dbtool.jaxb.ca.CAConfigurationType.CaHasRequestors;
import org.xipki.ca.dbtool.jaxb.ca.CAConfigurationType.CaHasUsers;
import org.xipki.ca.dbtool.jaxb.ca.CAConfigurationType.Caaliases;
import org.xipki.ca.dbtool.jaxb.ca.CAConfigurationType.Cas;
import org.xipki.ca.dbtool.jaxb.ca.CAConfigurationType.Cmpcontrols;
import org.xipki.ca.dbtool.jaxb.ca.CAConfigurationType.Crlsigners;
import org.xipki.ca.dbtool.jaxb.ca.CAConfigurationType.Environments;
import org.xipki.ca.dbtool.jaxb.ca.CAConfigurationType.Profiles;
import org.xipki.ca.dbtool.jaxb.ca.CAConfigurationType.Publishers;
import org.xipki.ca.dbtool.jaxb.ca.CAConfigurationType.Requestors;
import org.xipki.ca.dbtool.jaxb.ca.CAConfigurationType.Responders;
import org.xipki.ca.dbtool.jaxb.ca.CAConfigurationType.Sceps;
import org.xipki.ca.dbtool.jaxb.ca.CAConfigurationType.Users;
import org.xipki.ca.dbtool.jaxb.ca.CaHasProfileType;
import org.xipki.ca.dbtool.jaxb.ca.CaHasPublisherType;
import org.xipki.ca.dbtool.jaxb.ca.CaHasRequestorType;
import org.xipki.ca.dbtool.jaxb.ca.CaHasUserType;
import org.xipki.ca.dbtool.jaxb.ca.CaType;
import org.xipki.ca.dbtool.jaxb.ca.CaaliasType;
import org.xipki.ca.dbtool.jaxb.ca.CmpcontrolType;
import org.xipki.ca.dbtool.jaxb.ca.CrlsignerType;
import org.xipki.ca.dbtool.jaxb.ca.EnvironmentType;
import org.xipki.ca.dbtool.jaxb.ca.ObjectFactory;
import org.xipki.ca.dbtool.jaxb.ca.ProfileType;
import org.xipki.ca.dbtool.jaxb.ca.PublisherType;
import org.xipki.ca.dbtool.jaxb.ca.RequestorType;
import org.xipki.ca.dbtool.jaxb.ca.ResponderType;
import org.xipki.ca.dbtool.jaxb.ca.ScepType;
import org.xipki.ca.dbtool.jaxb.ca.UserType;
import org.xipki.ca.dbtool.port.DbPorter;
import org.xipki.common.util.XmlUtil;
import org.xipki.datasource.DataAccessException;
import org.xipki.datasource.DataSourceWrapper;

/**
 * TODO.
 * @author Lijun Liao
 * @since 2.0.0
 */

class CaConfigurationDbExporter extends DbPorter {

  private Marshaller marshaller;

  CaConfigurationDbExporter(DataSourceWrapper datasource, String destDir, AtomicBoolean stopMe,
      boolean evaluateOnly) throws DataAccessException, JAXBException {
    super(datasource, destDir, stopMe, evaluateOnly);

    JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
    marshaller = jaxbContext.createMarshaller();
    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
    marshaller.setSchema(DbPorter.retrieveSchema("/xsd/dbi-ca.xsd"));
  }

  public void export() throws Exception {
    CAConfigurationType caconf = new CAConfigurationType();
    caconf.setVersion(VERSION);

    System.out.println("exporting CA configuration from database");

    exportCmpcontrol(caconf);
    exportResponder(caconf);
    exportEnvironment(caconf);
    exportCrlsigner(caconf);
    exportRequestor(caconf);
    exportUser(caconf);
    exportPublisher(caconf);
    exportCa(caconf);
    exportProfile(caconf);
    exportCaalias(caconf);
    exportCaHasRequestor(caconf);
    exportCaHasUser(caconf);
    exportCaHasPublisher(caconf);
    exportCaHasProfile(caconf);
    exportScep(caconf);

    JAXBElement<CAConfigurationType> root = new ObjectFactory().createCAConfiguration(caconf);
    try {
      marshaller.marshal(root, new File(baseDir, FILENAME_CA_CONFIGURATION));
    } catch (JAXBException ex) {
      throw XmlUtil.convert(ex);
    }

    System.out.println(" exported CA configuration from database");
  }

  private void exportCmpcontrol(CAConfigurationType caconf) throws DataAccessException {
    Cmpcontrols cmpcontrols = new Cmpcontrols();
    caconf.setCmpcontrols(cmpcontrols);
    System.out.println("exporting table CMPCONTROL");

    final String sql = "SELECT NAME,CONF FROM CMPCONTROL";

    Statement stmt = null;
    ResultSet rs = null;
    try {
      stmt = createStatement();
      rs = stmt.executeQuery(sql);

      while (rs.next()) {
        CmpcontrolType cmpcontrol = new CmpcontrolType();
        cmpcontrols.getCmpcontrol().add(cmpcontrol);
        cmpcontrol.setName(rs.getString("NAME"));
        cmpcontrol.setConf(rs.getString("CONF"));
      }
    } catch (SQLException ex) {
      throw translate(sql, ex);
    } finally {
      releaseResources(stmt, rs);
    }

    System.out.println(" exported table CMPCONTROL");
  } // method exportCmpcontrol

  private void exportEnvironment(CAConfigurationType caconf) throws DataAccessException {
    System.out.println("exporting table ENVIRONMENT");
    Environments environments = new Environments();
    final String sql = "SELECT NAME,VALUE2 FROM ENVIRONMENT";

    Statement stmt = null;
    ResultSet rs = null;
    try {
      stmt = createStatement();
      rs = stmt.executeQuery(sql);

      while (rs.next()) {
        EnvironmentType environment = new EnvironmentType();
        environment.setName(rs.getString("NAME"));
        environment.setValue(rs.getString("VALUE2"));
        environments.getEnvironment().add(environment);
      }
    } catch (SQLException ex) {
      throw translate(sql, ex);
    } finally {
      releaseResources(stmt, rs);
    }

    caconf.setEnvironments(environments);
    System.out.println(" exported table ENVIRONMENT");
  } // method exportEnvironment

  private void exportCrlsigner(CAConfigurationType caconf) throws DataAccessException, IOException {
    System.out.println("exporting table CRLSIGNER");
    Crlsigners crlsigners = new Crlsigners();
    String sql = "SELECT NAME,SIGNER_TYPE,SIGNER_CONF,SIGNER_CERT,CRL_CONTROL FROM CRLSIGNER";

    Statement stmt = null;
    ResultSet rs = null;
    try {
      stmt = createStatement();
      rs = stmt.executeQuery(sql);

      while (rs.next()) {
        String name = rs.getString("NAME");

        CrlsignerType crlsigner = new CrlsignerType();
        crlsigner.setName(name);
        crlsigner.setSignerType(rs.getString("SIGNER_TYPE"));
        crlsigner.setSignerConf(buildFileOrValue(
            rs.getString("SIGNER_CONF"), "ca-conf/signerconf-crlsigner-" + name));
        crlsigner.setSignerCert(buildFileOrBase64Binary(
            rs.getString("SIGNER_CERT"), "ca-conf/signercert-crlsigner-" + name + ".der"));
        crlsigner.setCrlControl(rs.getString("CRL_CONTROL"));

        crlsigners.getCrlsigner().add(crlsigner);
      }
    } catch (SQLException ex) {
      throw translate(sql, ex);
    } finally {
      releaseResources(stmt, rs);
    }

    caconf.setCrlsigners(crlsigners);
    System.out.println(" exported table CRLSIGNER");
  } // method exportCrlsigner

  private void exportCaalias(CAConfigurationType caconf) throws DataAccessException {
    System.out.println("exporting table CAALIAS");
    Caaliases caaliases = new Caaliases();
    final String sql = "SELECT NAME,CA_ID FROM CAALIAS";

    Statement stmt = null;
    ResultSet rs = null;
    try {
      stmt = createStatement();
      rs = stmt.executeQuery(sql);

      while (rs.next()) {
        CaaliasType caalias = new CaaliasType();
        caalias.setName(rs.getString("NAME"));
        caalias.setCaId(rs.getInt("CA_ID"));

        caaliases.getCaalias().add(caalias);
      }
    } catch (SQLException ex) {
      throw translate(sql, ex);
    } finally {
      releaseResources(stmt, rs);
    }

    caconf.setCaaliases(caaliases);
    System.out.println(" exported table CAALIAS");
  } // method exportCaalias

  private void exportRequestor(CAConfigurationType caconf) throws DataAccessException, IOException {
    System.out.println("exporting table REQUESTOR");
    Requestors requestors = new Requestors();
    final String sql = "SELECT ID,NAME,CERT FROM REQUESTOR";

    Statement stmt = null;
    ResultSet rs = null;
    try {
      stmt = createStatement();
      rs = stmt.executeQuery(sql);

      while (rs.next()) {
        String name = rs.getString("NAME");

        RequestorType requestor = new RequestorType();
        requestor.setId(rs.getInt("ID"));
        requestor.setName(name);
        requestor.setCert(buildFileOrBase64Binary(
            rs.getString("CERT"), "ca-conf/cert-requestor-" + name + ".der"));
        requestors.getRequestor().add(requestor);
      }
    } catch (SQLException ex) {
      throw translate(sql, ex);
    } finally {
      releaseResources(stmt, rs);
    }

    caconf.setRequestors(requestors);
    System.out.println(" exported table REQUESTOR");
  } // method exportRequestor

  private void exportUser(CAConfigurationType caconf) throws DataAccessException, IOException {
    System.out.println("exporting table TUSER");
    Users users = new Users();
    final String sql = "SELECT ID,NAME,ACTIVE,PASSWORD FROM TUSER";

    Statement stmt = null;
    ResultSet rs = null;
    try {
      stmt = createStatement();
      rs = stmt.executeQuery(sql);

      while (rs.next()) {
        UserType user = new UserType();
        user.setId(rs.getInt("ID"));
        user.setName(rs.getString("NAME"));
        user.setActive(rs.getInt("ACTIVE"));
        user.setPassword(rs.getString("PASSWORD"));
        users.getUser().add(user);
      }
    } catch (SQLException ex) {
      throw translate(sql, ex);
    } finally {
      releaseResources(stmt, rs);
    }

    caconf.setUsers(users);
    System.out.println(" exported table TUSER");
  } // method exportUser

  private void exportResponder(CAConfigurationType caconf) throws DataAccessException, IOException {
    System.out.println("exporting table RESPONDER");
    Responders responders = new Responders();
    final String sql = "SELECT NAME,TYPE,CONF,CERT FROM RESPONDER";

    Statement stmt = null;
    ResultSet rs = null;
    try {
      stmt = createStatement();
      rs = stmt.executeQuery(sql);

      while (rs.next()) {
        String name = rs.getString("NAME");

        ResponderType responder = new ResponderType();
        responder.setName(name);
        responder.setType(rs.getString("TYPE"));
        responder.setConf(buildFileOrValue(rs.getString("CONF"), "ca-conf/conf-responder-" + name));
        responder.setCert(buildFileOrBase64Binary(
            rs.getString("CERT"), "ca-conf/cert-responder-" + name + ".der"));
        responders.getResponder().add(responder);
      }
    } catch (SQLException ex) {
      throw translate(sql, ex);
    } finally {
      releaseResources(stmt, rs);
    }

    caconf.setResponders(responders);
    System.out.println(" exported table RESPONDER");
  } // method exportResponder

  private void exportPublisher(CAConfigurationType caconf) throws DataAccessException, IOException {
    System.out.println("exporting table PUBLISHER");
    Publishers publishers = new Publishers();
    final String sql = "SELECT ID,NAME,TYPE,CONF FROM PUBLISHER";

    Statement stmt = null;
    ResultSet rs = null;
    try {
      stmt = createStatement();
      rs = stmt.executeQuery(sql);

      while (rs.next()) {
        String name = rs.getString("NAME");

        PublisherType publisher = new PublisherType();
        publisher.setId(rs.getInt("ID"));
        publisher.setName(name);
        publisher.setType(rs.getString("TYPE"));
        publisher.setConf(buildFileOrValue(rs.getString("CONF"), "ca-conf/conf-publisher-" + name));

        publishers.getPublisher().add(publisher);
      }
    } catch (SQLException ex) {
      throw translate(sql, ex);
    } finally {
      releaseResources(stmt, rs);
    }

    caconf.setPublishers(publishers);
    System.out.println(" exported table PUBLISHER");
  } // method exportPublisher

  private void exportProfile(CAConfigurationType caconf) throws DataAccessException, IOException {
    System.out.println("exporting table PROFILE");
    Profiles profiles = new Profiles();
    final String sql = "SELECT ID,NAME,ART,TYPE,CONF FROM PROFILE";

    Statement stmt = null;
    ResultSet rs = null;
    try {
      stmt = createStatement();
      rs = stmt.executeQuery(sql);

      while (rs.next()) {
        String name = rs.getString("NAME");

        ProfileType profile = new ProfileType();
        profile.setId(rs.getInt("ID"));
        profile.setName(name);
        profile.setArt(rs.getInt("ART"));
        profile.setType(rs.getString("TYPE"));
        profile.setConf(buildFileOrValue(rs.getString("CONF"), "ca-conf/certprofile-" + name));

        profiles.getProfile().add(profile);
      }
    } catch (SQLException ex) {
      throw translate(sql, ex);
    } finally {
      releaseResources(stmt, rs);
    }

    caconf.setProfiles(profiles);
    System.out.println(" exported table PROFILE");
  } // method exportProfile

  private void exportCa(CAConfigurationType caconf) throws DataAccessException, IOException {
    System.out.println("exporting table CA");
    Cas cas = new Cas();
    String sql = "SELECT ID,NAME,SN_SIZE,STATUS,CRL_URIS,OCSP_URIS,MAX_VALIDITY,CERT,SIGNER_TYPE,"
        + "SIGNER_CONF,CRLSIGNER_NAME,PERMISSION,NUM_CRLS,EXPIRATION_PERIOD,KEEP_EXPIRED_CERT_DAYS,"
        + "REV,RR,RT,RIT,DUPLICATE_KEY,DUPLICATE_SUBJECT,SAVE_REQ,DELTACRL_URIS,VALIDITY_MODE,"
        + "CACERT_URIS,ART,NEXT_CRLNO,RESPONDER_NAME,CMPCONTROL_NAME,EXTRA_CONTROL FROM CA";

    Statement stmt = null;
    ResultSet rs = null;
    try {
      stmt = createStatement();
      rs = stmt.executeQuery(sql);

      while (rs.next()) {
        String name = rs.getString("NAME");

        CaType ca = new CaType();
        ca.setId(rs.getInt("ID"));
        ca.setName(name);
        ca.setArt(rs.getInt("ART"));
        ca.setSnSize(rs.getInt("SN_SIZE"));
        ca.setNextCrlNo(rs.getLong("NEXT_CRLNO"));
        ca.setStatus(rs.getString("STATUS"));
        ca.setCrlUris(rs.getString("CRL_URIS"));
        ca.setDeltacrlUris(rs.getString("DELTACRL_URIS"));
        ca.setOcspUris(rs.getString("OCSP_URIS"));
        ca.setCacertUris(rs.getString("CACERT_URIS"));
        ca.setMaxValidity(rs.getString("MAX_VALIDITY"));
        ca.setCert(buildFileOrBase64Binary(
            rs.getString("CERT"), "ca-conf/cert-ca-" + name + ".der"));
        ca.setSignerType(rs.getString("SIGNER_TYPE"));
        ca.setSignerConf(buildFileOrValue(
            rs.getString("SIGNER_CONF"), "ca-conf/signerconf-ca-" + name));
        ca.setCrlsignerName(rs.getString("CRLSIGNER_NAME"));
        ca.setResponderName(rs.getString("RESPONDER_NAME"));
        ca.setCmpcontrolName(rs.getString("CMPCONTROL_NAME"));
        ca.setDuplicateKey(rs.getInt("DUPLICATE_KEY"));
        ca.setDuplicateSubject(rs.getInt("DUPLICATE_SUBJECT"));
        ca.setSaveReq(rs.getInt("SAVE_REQ"));
        ca.setPermission(rs.getInt("PERMISSION"));
        ca.setExpirationPeriod(rs.getInt("EXPIRATION_PERIOD"));
        ca.setKeepExpiredCertDays(rs.getInt("KEEP_EXPIRED_CERT_DAYS"));
        ca.setValidityMode(rs.getString("VALIDITY_MODE"));
        ca.setExtraControl(rs.getString("EXTRA_CONTROL"));
        ca.setNumCrls(rs.getInt("NUM_CRLS"));

        boolean revoked = rs.getBoolean("REV");
        ca.setRevoked(revoked);
        if (revoked) {
          ca.setRevReason(rs.getInt("RR"));
          ca.setRevTime(rs.getLong("RT"));
          ca.setRevInvTime(rs.getLong("RIT"));
        }

        cas.getCa().add(ca);
      }
    } catch (SQLException ex) {
      throw translate(sql, ex);
    } finally {
      releaseResources(stmt, rs);
    }

    caconf.setCas(cas);
    System.out.println(" exported table CA");
  } // method exportCa

  private void exportCaHasRequestor(CAConfigurationType caconf) throws DataAccessException {
    System.out.println("exporting table CA_HAS_REQUESTOR");
    CaHasRequestors caHasRequestors = new CaHasRequestors();
    final String sql = "SELECT CA_ID,REQUESTOR_ID,RA,PERMISSION,PROFILES FROM CA_HAS_REQUESTOR";

    Statement stmt = null;
    ResultSet rs = null;
    try {
      stmt = createStatement();
      rs = stmt.executeQuery(sql);

      while (rs.next()) {
        CaHasRequestorType caHasRequestor = new CaHasRequestorType();
        caHasRequestor.setCaId(rs.getInt("CA_ID"));
        caHasRequestor.setRequestorId(rs.getInt("REQUESTOR_ID"));
        caHasRequestor.setRa(rs.getBoolean("RA"));
        caHasRequestor.setPermission(rs.getInt("PERMISSION"));
        caHasRequestor.setProfiles(rs.getString("PROFILES"));

        caHasRequestors.getCaHasRequestor().add(caHasRequestor);
      }
    } catch (SQLException ex) {
      throw translate(sql, ex);
    } finally {
      releaseResources(stmt, rs);
    }

    caconf.setCaHasRequestors(caHasRequestors);
    System.out.println(" exported table CA_HAS_REQUESTOR");
  } // method exportCaHasRequestor

  private void exportCaHasUser(CAConfigurationType caconf) throws DataAccessException {
    System.out.println("exporting table CA_HAS_USER");
    CaHasUsers caHasUsers = new CaHasUsers();
    final String sql = "SELECT ID,CA_ID,USER_ID,PERMISSION,PROFILES FROM CA_HAS_USER";

    Statement stmt = null;
    ResultSet rs = null;
    try {
      stmt = createStatement();
      rs = stmt.executeQuery(sql);

      while (rs.next()) {
        CaHasUserType caHasUser = new CaHasUserType();
        caHasUser.setId(rs.getInt("ID"));
        caHasUser.setCaId(rs.getInt("CA_ID"));
        caHasUser.setUserId(rs.getInt("USER_ID"));
        caHasUser.setPermission(rs.getInt("PERMISSION"));
        caHasUser.setProfiles(rs.getString("PROFILES"));

        caHasUsers.getCaHasUser().add(caHasUser);
      }
    } catch (SQLException ex) {
      throw translate(sql, ex);
    } finally {
      releaseResources(stmt, rs);
    }

    caconf.setCaHasUsers(caHasUsers);
    System.out.println(" exported table CA_HAS_USER");
  } // method exportCaHasRequestor

  private void exportCaHasPublisher(CAConfigurationType caconf) throws DataAccessException {
    System.out.println("exporting table CA_HAS_PUBLISHER");
    CaHasPublishers caHasPublishers = new CaHasPublishers();
    final String sql = "SELECT CA_ID,PUBLISHER_ID FROM CA_HAS_PUBLISHER";

    Statement stmt = null;
    ResultSet rs = null;
    try {
      stmt = createStatement();
      rs = stmt.executeQuery(sql);

      while (rs.next()) {
        CaHasPublisherType caHasPublisher = new CaHasPublisherType();
        caHasPublisher.setCaId(rs.getInt("CA_ID"));
        caHasPublisher.setPublisherId(rs.getInt("PUBLISHER_ID"));

        caHasPublishers.getCaHasPublisher().add(caHasPublisher);
      }
    } catch (SQLException ex) {
      throw translate(sql, ex);
    } finally {
      releaseResources(stmt, rs);
    }

    caconf.setCaHasPublishers(caHasPublishers);
    System.out.println(" exported table CA_HAS_PUBLISHER");
  } // method exportCaHasPublisher

  private void exportScep(CAConfigurationType caconf) throws DataAccessException, IOException {
    System.out.println("exporting table SCEP");
    Sceps sceps = new Sceps();
    caconf.setSceps(sceps);

    final String sql = "SELECT NAME,CA_ID,ACTIVE,PROFILES,RESPONDER_NAME,CONTROL FROM SCEP";

    Statement stmt = null;
    ResultSet rs = null;
    try {
      stmt = createStatement();
      rs = stmt.executeQuery(sql);

      while (rs.next()) {
        int caId = rs.getInt("CA_ID");

        ScepType scep = new ScepType();
        scep.setName(rs.getString("NAME"));
        scep.setCaId(caId);
        scep.setActive(rs.getInt("ACTIVE"));
        scep.setProfiles(rs.getString("PROFILES"));
        scep.setResponderName(rs.getString("RESPONDER_NAME"));
        scep.setControl(rs.getString("CONTROL"));
        sceps.getScep().add(scep);
      }
    } catch (SQLException ex) {
      throw translate(sql, ex);
    } finally {
      releaseResources(stmt, rs);
    }

    System.out.println(" exported table SCEP");
  } // method exportScep

  private void exportCaHasProfile(CAConfigurationType caconf) throws DataAccessException {
    System.out.println("exporting table CA_HAS_PROFILE");
    CaHasProfiles caHasProfiles = new CaHasProfiles();
    final String sql = "SELECT CA_ID,PROFILE_ID FROM CA_HAS_PROFILE";

    Statement stmt = null;
    ResultSet rs = null;
    try {
      stmt = createStatement();
      rs = stmt.executeQuery(sql);

      while (rs.next()) {
        CaHasProfileType caHasProfile = new CaHasProfileType();
        caHasProfile.setCaId(rs.getInt("CA_ID"));
        caHasProfile.setProfileId(rs.getInt("PROFILE_ID"));

        caHasProfiles.getCaHasProfile().add(caHasProfile);
      }
    } catch (SQLException ex) {
      throw translate(sql, ex);
    } finally {
      releaseResources(stmt, rs);
    }

    caconf.setCaHasProfiles(caHasProfiles);
    System.out.println(" exported table CA_HAS_PROFILE");
  } // method exportCaHasProfile

}
