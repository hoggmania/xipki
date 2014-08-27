/*
 * Copyright (c) 2014 Lijun Liao
 *
 * TO-BE-DEFINE
 *
 */

package org.xipki.ca.client.shell;

import java.io.File;
import java.security.cert.X509Certificate;

import org.apache.felix.gogo.commands.Command;
import org.apache.felix.gogo.commands.Option;
import org.bouncycastle.asn1.pkcs.CertificationRequest;
import org.xipki.ca.common.CertificateOrError;
import org.xipki.ca.common.EnrollCertResult;
import org.xipki.security.common.IoCertUtil;

/**
 * @author Lijun Liao
 */

@Command(scope = "caclient", name = "enroll", description="Enroll certificate")
public class RAEnrollCertCommand extends ClientCommand
{

    @Option(name = "-p10",
            required = true, description = "Required. PKCS-10 request file")
    protected String p10File;

    @Option(name = "-profile",
            required = true, description = "Required. Certificate profile")
    protected String profile;

    @Option(name = "-out",
            required = false, description = "Where to save the certificate")
    protected String outputFile;

    @Option(name = "-user",
            required = false, description = "Username")
    protected String user;

    @Override
    protected Object doExecute()
    throws Exception
    {
        CertificationRequest p10Req = CertificationRequest.getInstance(
                IoCertUtil.read(p10File));
        EnrollCertResult result = raWorker.requestCert(p10Req, profile, null, user);

        X509Certificate cert = null;
        if(result != null)
        {
            String id = result.getAllIds().iterator().next();
            CertificateOrError certOrError = result.getCertificateOrError(id);
            cert = (X509Certificate) certOrError.getCertificate();
        }

        if(cert == null)
        {
            err("No certificate received from the server");
            return null;
        }

        if(outputFile == null)
        {
            outputFile = p10File.substring(0, p10File.length() - ".p10".length()) + ".der";
        }

        File certFile = new File(outputFile);
        saveVerbose("Certificate saved to file", certFile, cert.getEncoded());

        return null;
    }

}
