/*
 * Copyright (c) 2017 Bosch Software Innovations GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/index.php
 *
 * Contributors:
 *    Bosch Software Innovations GmbH - initial contribution
 */
package org.eclipse.ditto.services.connectivity.messaging.internal;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Collection;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.model.base.exceptions.DittoRuntimeExceptionBuilder;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.connectivity.Connection;
import org.eclipse.ditto.model.connectivity.ConnectionConfigurationInvalidException;
import org.eclipse.ditto.model.connectivity.credentials.ClientCertificateCredentials;
import org.eclipse.ditto.model.connectivity.credentials.CredentialsVisitor;
import org.eclipse.ditto.signals.commands.connectivity.exceptions.ConnectionUnavailableException;

/**
 * Create SSL context from connection credentials.
 */
public final class SSLContextCreator implements CredentialsVisitor<SSLContext> {

    private static final String PRIVATE_KEY_LABEL = "PRIVATE KEY";
    private static final Pattern PRIVATE_KEY_REGEX = Pattern.compile(pemRegex(PRIVATE_KEY_LABEL));
    private static final KeyFactory RSA_KEY_FACTORY;
    private static final CertificateFactory X509_CERTIFICATE_FACTORY;

    static {
        try {
            RSA_KEY_FACTORY = KeyFactory.getInstance("RSA");
            X509_CERTIFICATE_FACTORY = CertificateFactory.getInstance("X.509");
        } catch (final NoSuchAlgorithmException e) {
            throw new Error("FATAL: failed to load RSA key or key manager factory", e);
        } catch (final CertificateException e) {
            throw new Error("FATAL: failed to load X.509 certificate factory", e);
        }
    }

    @Nullable
    private final String trustedCertificates;
    private final DittoHeaders dittoHeaders;

    private SSLContextCreator(@Nullable final String trustedCertificates,
            final DittoHeaders dittoHeaders) {
        this.trustedCertificates = trustedCertificates;
        this.dittoHeaders = dittoHeaders;
    }

    public static SSLContextCreator of(@Nullable final String trustedCertificates,
            @Nullable final DittoHeaders dittoHeaders) {
        return new SSLContextCreator(trustedCertificates, dittoHeaders != null ? dittoHeaders : DittoHeaders.empty());
    }

    @Override
    public SSLContext clientCertificate(final ClientCertificateCredentials credentials) {
        final String clientKeyPem = credentials.getClientKey().orElse(null);
        final String clientCertificatePem = credentials.getClientCertificate().orElse(null);
        final KeyManagerFactory keyManagerFactory = newKeyManagerFactory(clientKeyPem, clientCertificatePem);
        final TrustManagerFactory trustManagerFactory = newTrustManagerFactory(trustedCertificates);
        return newTLSContext(keyManagerFactory, trustManagerFactory);
    }

    @Nullable
    private TrustManagerFactory newTrustManagerFactory(@Nullable final String trustedCertificates) {
        if (trustedCertificates != null) {
            try {
                final byte[] caCertsPem = trustedCertificates.getBytes(StandardCharsets.US_ASCII);
                final Collection<? extends Certificate> caCerts =
                        X509_CERTIFICATE_FACTORY.generateCertificates(new ByteArrayInputStream(caCertsPem));
                final KeyStore keystore = newKeystore();
                for (final Certificate caCert : caCerts) {
                    keystore.setCertificateEntry("ca", caCert);
                }
                final TrustManagerFactory trustManagerFactory =
                        TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                trustManagerFactory.init(keystore);
                return trustManagerFactory;
            } catch (final Exception e) {
                throw fatalError("Engine failed to configure trusted server or CA certificates")
                        .cause(e)
                        .build();
            }
        } else {
            return null;
        }
    }

    @Nullable
    private KeyManagerFactory newKeyManagerFactory(@Nullable final String clientKeyPem,
            @Nullable final String clientCertificatePem) {

        if (clientKeyPem != null && clientCertificatePem != null) {
            final KeyStore keystore = newKeystore();
            final PrivateKey privateKey = getClientPrivateKey(clientKeyPem);
            final Certificate certificate = getClientCertificate(clientCertificatePem);
            setPrivateKey(keystore, privateKey, certificate);
            setCertificate(keystore, certificate);

            try {
                final KeyManagerFactory keyManagerFactory =
                        KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                keyManagerFactory.init(keystore, new char[0]);
                return keyManagerFactory;
            } catch (final Exception e) {
                throw fatalError("Engine failed to configure client key and client certificate")
                        .cause(e)
                        .build();
            }
        } else {
            return null;
        }
    }

    private void setPrivateKey(final KeyStore keystore, final PrivateKey privateKey, final Certificate... certs) {
        try {
            keystore.setKeyEntry("key", privateKey, new char[0], certs);
        } catch (final KeyStoreException e) {
            throw fatalError("Engine failed to configure client key")
                    .cause(e)
                    .build();
        }
    }

    private void setCertificate(final KeyStore keystore, final Certificate certificate) {
        try {
            keystore.setCertificateEntry("cert", certificate);
        } catch (final KeyStoreException e) {
            throw fatalError("Engine failed to configure client certificate")
                    .cause(e)
                    .build();
        }
    }

    private PrivateKey getClientPrivateKey(final String privateKeyPem) {
        final Matcher matcher = PRIVATE_KEY_REGEX.matcher(privateKeyPem);
        final Supplier<DittoRuntimeExceptionBuilder> errorSupplier = () -> {
            final JsonPointer errorLocation = Connection.JsonFields.CREDENTIALS.getPointer()
                    .append(ClientCertificateCredentials.JsonFields.CLIENT_KEY.getPointer());
            return badFormat(errorLocation, PRIVATE_KEY_LABEL, "PKCS #8")
                    .description("Please format your client key as PEM-encoded unencrypted PKCS #8.");
        };
        if (!matcher.matches()) {
            throw errorSupplier.get().build();
        } else {
            final String content = matcher.group(1).replaceAll("\\s", "");
            final byte[] bytes = decodeBase64(content);
            final KeySpec keySpec = new PKCS8EncodedKeySpec(bytes);
            try {
                return RSA_KEY_FACTORY.generatePrivate(keySpec);
            } catch (final InvalidKeySpecException e) {
                throw errorSupplier.get().cause(e).build();
            }
        }
    }

    private Certificate getClientCertificate(final String certificatePem) {
        final byte[] asciiBytes = certificatePem.getBytes(StandardCharsets.US_ASCII);
        try {
            return X509_CERTIFICATE_FACTORY.generateCertificate(new ByteArrayInputStream(asciiBytes));
        } catch (final CertificateException e) {
            final JsonPointer errorLocation = Connection.JsonFields.CREDENTIALS.getPointer()
                    .append(ClientCertificateCredentials.JsonFields.CLIENT_CERTIFICATE.getPointer());
            throw badFormat(errorLocation, "CERTIFICATE", "DER")
                    .build();
        }
    }

    private SSLContext newTLSContext(@Nullable final KeyManagerFactory keyManagerFactory,
            @Nullable final TrustManagerFactory trustManagerFactory) {
        try {
            final SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            final KeyManager[] keyManagers = keyManagerFactory != null ? keyManagerFactory.getKeyManagers() : null;
            final TrustManager[] trustManagers =
                    trustManagerFactory != null ? trustManagerFactory.getTrustManagers() : null;
            sslContext.init(keyManagers, trustManagers, null);
            return sslContext;
        } catch (final NoSuchAlgorithmException | KeyManagementException e) {
            throw fatalError("Cannot start TLS 1.2 engine")
                    .cause(e)
                    .build();
        }
    }

    private KeyStore newKeystore() {
        try {
            final KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            // initialize an empty keystore
            keyStore.load(null, null);
            return keyStore;
        } catch (final Exception e) {
            throw fatalError("Cannot initialize client-side security for connection")
                    .cause(e)
                    .build();
        }
    }

    private DittoRuntimeExceptionBuilder<ConnectionUnavailableException> fatalError(final String whatHappened) {
        return ConnectionUnavailableException.newBuilder("unimportant")
                .message(String.format("Fatal error: %s.", whatHappened))
                .description("Please contact the service team.")
                .dittoHeaders(dittoHeaders);
    }

    private DittoRuntimeExceptionBuilder<ConnectionConfigurationInvalidException> badFormat(
            final JsonPointer errorLocation,
            final String label,
            final String binaryFormat) {
        final String message = String.format("%s: bad format. " +
                        "Expect PEM-encoded %s data specified by RFC-7468 starting with '<-----BEGIN %s----->'",
                errorLocation.toString(), binaryFormat, label);
        return ConnectionConfigurationInvalidException.newBuilder(message)
                .dittoHeaders(dittoHeaders);
    }

    /**
     * Create PEM regex specified by RFC-7468 section 3 "ABNF".
     *
     * @param label the label.
     * @return regex to capture base64text of textualmsg.
     */
    private static String pemRegex(final String label) {
        final String preeb = String.format("\\s*+-----BEGIN %s-----", label);
        final String posteb = String.format("-----END %s-----\\s*+", label);
        final String contentWhitespaceGroup = "([A-Za-z0-9+/=\\s]*+)";
        return String.format("%s%s%s", preeb, contentWhitespaceGroup, posteb);
    }

    private static byte[] decodeBase64(final String content) {
        return Base64.getDecoder().decode(content.replace("\\s", ""));
    }
}
