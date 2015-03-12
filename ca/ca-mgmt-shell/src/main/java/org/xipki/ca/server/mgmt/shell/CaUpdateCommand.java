/*
 *
 * This file is part of the XiPKI project.
 * Copyright (c) 2014 - 2015 Lijun Liao
 * Author: Lijun Liao
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License version 3
 * as published by the Free Software Foundation with the addition of the
 * following permission added to Section 15 as permitted in Section 7(a):
 * FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY
 * THE AUTHOR LIJUN LIAO. LIJUN LIAO DISCLAIMS THE WARRANTY OF NON INFRINGEMENT
 * OF THIRD PARTY RIGHTS.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License.
 *
 * You can be released from the requirements of the license by purchasing
 * a commercial license. Buying such a license is mandatory as soon as you
 * develop commercial activities involving the XiPKI software without
 * disclosing the source code of your own applications.
 *
 * For more information, please contact Lijun Liao at this
 * address: lijun.liao@gmail.com
 */

package org.xipki.ca.server.mgmt.shell;

import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.xipki.ca.api.profile.CertValidity;
import org.xipki.ca.server.mgmt.api.CAManager;
import org.xipki.ca.server.mgmt.api.CAStatus;
import org.xipki.ca.server.mgmt.api.DuplicationMode;
import org.xipki.ca.server.mgmt.api.Permission;
import org.xipki.ca.server.mgmt.api.ValidityMode;
import org.xipki.common.ConfigurationException;
import org.xipki.common.util.SecurityUtil;
import org.xipki.security.api.SecurityFactory;

/**
 * @author Lijun Liao
 */

@Command(scope = "xipki-ca", name = "ca-update", description="update CA")
public class CaUpdateCommand extends CaCommand
{
    @Option(name = "-name",
            required = true, description = "required. CA name")
    private String caName;

    @Option(name = "-status",
            description = "CA status, active|inactive")
    private String caStatus;

    @Option(name = "-ocspUri",
            description = "OCSP URI or 'NULL', multi options is allowed",
            multiValued = true)
    private List<String> ocspUris;

    @Option(name = "-crlUri",
            description = "CRL URI or 'NULL', multi options is allowed",
            multiValued = true)
    private List<String> crlUris;

    @Option(name = "-deltaCrlUri",
            description = "delta CRL URI or 'NULL', multi options is allowed",
            multiValued = true)
    private List<String> deltaCrlUris;

    @Option(name = "-permission",
            description = "permission, multi options is allowed. allowed values are\n" + permissionsText,
            multiValued = true)
    private Set<String> permissions;

    @Option(name = "-maxValidity",
            description = "maximal validity")
    private String maxValidity;

    @Option(name = "-expirationPeriod",
            description = "days before expiration time of CA to issue certificates")
    private Integer expirationPeriod;

    @Option(name = "-crlSigner",
            description = "CRL signer name or 'NULL'")
    private String crlSignerName;

    @Option(name = "-cmpControl",
            description = "CMP control name or 'NULL'")
    private String cmpControlName;

    @Option(name = "-numCrls",
            description = "Number of CRLs to be kept in database")
    private Integer numCrls;

    @Option(name = "-cert",
            description = "CA certificate file")
    private String certFile;

    @Option(name = "-signerType",
            description = "CA signer type")
    private String signerType;

    @Option(name = "-signerConf",
            description = "CA signer configuration or 'NULL'")
    private String signerConf;

    @Option(name = "-dk", aliases = { "--duplicateKey" },
            description = "mode of duplicate key.\n"
                    + "\t1: forbidden\n"
                    + "\t2: forbiddenWithinProfile\n"
                    + "\t3: permitted")
    private String duplicateKeyS;

    @Option(name = "-ds", aliases = { "--duplicateSubject" },
            description = "mode of duplicate subject.\n"
                    + "\t1: forbidden\n"
                    + "\t2: forbiddenWithinProfile\n"
                    + "\t3: permitted")
    private String duplicateSubjectS;

    @Option(name = "-validityMode",
            description = "mode of valditity.\n"
                    + "\tSTRICT: Reject if the notBefore + validity behinds CA's notAfter \n"
                    + "\tLAX:    notBefore + validity after CA's notAfter is permitted\n"
                    + "\tCUTOFF: notAfter of issued certificates will be set to the earlier time of\n"
                    + "\t        notBefore + validity and CA's notAfter")
    private String validityModeS;

    private SecurityFactory securityFactory;

    public void setSecurityFactory(SecurityFactory securityFactory)
    {
        this.securityFactory = securityFactory;
    }

    @Override
    protected Object _doExecute()
    throws Exception
    {
        CAStatus status = null;
        if(caStatus != null)
        {
            status = CAStatus.getCAStatus(caStatus);
        }

        if(expirationPeriod != null && expirationPeriod < 0)
        {
            err("invalid expirationPeriod: " + expirationPeriod);
            return null;
        }

        X509Certificate caCert = null;
        if(certFile != null)
        {
            caCert = SecurityUtil.parseCert(certFile);
        }

        if(signerConf != null)
        {
            if("PKCS12".equalsIgnoreCase(signerType) || "JKS".equalsIgnoreCase(signerType))
            {
                signerConf = ShellUtil.canonicalizeSignerConf(signerType, signerConf, securityFactory.getPasswordResolver());
            }
        }

        DuplicationMode duplicateKey = null;
        if(duplicateKeyS != null)
        {
            duplicateKey = DuplicationMode.getInstance(duplicateKeyS);
            if(duplicateKey == null)
            {
                err("invalid duplication mode " + duplicateKeyS);
            }
        }

        DuplicationMode duplicateSubject = null;
        if(duplicateSubjectS != null)
        {
            duplicateSubject = DuplicationMode.getInstance(duplicateSubjectS);
            if(duplicateKey == null)
            {
                err("invalid duplication mode " + duplicateSubjectS);
            }
        }

        Set<Permission> _permissions = null;
        if (permissions != null && permissions.size() > 0)
        {
            _permissions = new HashSet<>();
            for(String permission : permissions)
            {
                Permission _permission = Permission.getPermission(permission);
                if(_permission == null)
                {
                    throw new ConfigurationException("invalid permission: " + permission);
                }
                _permissions.add(_permission);
            }
        }

        boolean clearCrlUris = false;
        if(crlUris != null)
        {
            for(String uri : crlUris)
            {
                if(CAManager.NULL.equalsIgnoreCase(uri))
                {
                    clearCrlUris = true;
                    break;
                }
            }
        }

        Set<String> _crlUris = null;

        if(clearCrlUris)
        {
            _crlUris = Collections.emptySet();
        }
        else
        {
            if(crlUris != null )
            {
                _crlUris = new HashSet<>(crlUris);
            }
        }

        boolean clearDeltaCrlUris = false;
        if(deltaCrlUris != null)
        {
            for(String uri : deltaCrlUris)
            {
                if(CAManager.NULL.equalsIgnoreCase(uri))
                {
                    clearDeltaCrlUris = true;
                    break;
                }
            }
        }

        Set<String> _deltaCrlUris = null;

        if(clearDeltaCrlUris)
        {
            _deltaCrlUris = Collections.emptySet();
        }
        else
        {
            if(deltaCrlUris != null )
            {
                _deltaCrlUris = new HashSet<>(deltaCrlUris);
            }
        }

        boolean clearOcspUris = false;
        if(ocspUris != null)
        {
            for(String uri : ocspUris)
            {
                if(CAManager.NULL.equalsIgnoreCase(uri))
                {
                    clearOcspUris = true;
                    break;
                }
            }
        }

        Set<String> _ocspUris = null;
        if(clearOcspUris)
        {
            _ocspUris = Collections.emptySet();
        }
        else
        {
            if (ocspUris != null)
            {
                _ocspUris = new HashSet<>(ocspUris);
            }
        }

        ValidityMode validityMode = null;
        if(validityModeS != null)
        {
            validityMode = ValidityMode.getInstance(validityModeS);
            if(validityMode == null)
            {
                throw new ConfigurationException("invalid validity mode: " + validityModeS);
            }
        }

        CertValidity _maxValidity = null;
        if(maxValidity != null)
        {
            _maxValidity = CertValidity.getInstance(maxValidity);
        }

        caManager.changeCA(
                caName,
                status,
                caCert,
                _crlUris,
                _deltaCrlUris,
                _ocspUris,
                _maxValidity,
                signerType,
                signerConf,
                crlSignerName,
                cmpControlName,
                duplicateKey,
                duplicateSubject,
                _permissions,
                numCrls,
                expirationPeriod,
                validityMode);

        out("updated CA " + caName);
        return null;
    }
}
