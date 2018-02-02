package es.gob.afirma.standalone.configurator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.security.cert.X509Certificate;

import org.junit.Ignore;
import org.junit.Test;

import es.gob.afirma.core.misc.AOUtil;
import es.gob.afirma.standalone.configurator.CertUtil.CertPack;

/** Pruebas de las utilidades de certificados.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s. */
public final class TestCertUtil {

	/** para pruebas sin JUnit.
	 * @param args No se usa.
	 * @throws Exception En cualquier error. */
	public static void main(final String[] args) throws Exception {
		new TestCertUtil().testSslpkcs12Generation();
	}

	/** prueba de generaci&oacute;n de PKCS#12 SSL.
	 * @throws Exception En cualquier error. */
	@SuppressWarnings("static-method")
	@Test
	@Ignore
	public void testSslpkcs12Generation() throws Exception {
		final CertPack certPack = CertUtil.getCertPackForHostSsl(
            "tomcat",          // Alias del certificado SSL //$NON-NLS-1$
            "12341234",        // Contrasena del PKCS#12 //$NON-NLS-1$
            "34.251.104.141",  // IP del host SSL //$NON-NLS-1$
            "cn=ca-clavefirma" // Nombre de la CA SSL //$NON-NLS-1$
        );
		final byte[] p12Bytes = certPack.getPkcs12();
		try (
			final OutputStream fos = new FileOutputStream(
				File.createTempFile("PKCS12_SSL_", ".P12") //$NON-NLS-1$ //$NON-NLS-2$
			);
		) {
			fos.write(p12Bytes);
			fos.flush();
		}
		final byte[] caCert = certPack.getCaCertificate().getEncoded();
		try (
			final OutputStream fos = new FileOutputStream(
				File.createTempFile("CA_SSL_", ".cer") //$NON-NLS-1$ //$NON-NLS-2$
			);
		) {
			fos.write(caCert);
			fos.flush();
		}
	}

	/** Prueba la generaci&oacute;n del certificado y almac&eacute;n de claves.
	 * @throws Exception Cuando ocurre un error. */
	@SuppressWarnings("static-method")
	@Test
	public void testCertGeneration() throws Exception {
		final CertPack cp = CertUtil.getCertPackForLocalhostSsl("AutoFirma", "654321"); //$NON-NLS-1$ //$NON-NLS-2$
		System.out.println(AOUtil.getCN((X509Certificate) cp.getCaCertificate()));
		System.out.println(AOUtil.getCN((X509Certificate) cp.getSslCertificate()));
		System.out.println();
		System.out.println();
		System.out.println(AOUtil.hexify(cp.getPkcs12(), true));

		try (
			final OutputStream fos = new FileOutputStream(File.createTempFile("SSLCERT_", ".cer")) //$NON-NLS-1$ //$NON-NLS-2$
		) {
			fos.write(cp.getSslCertificate().getEncoded());
		}

		try (
			final OutputStream fos = new FileOutputStream(File.createTempFile("CACERT_", ".cer")) //$NON-NLS-1$ //$NON-NLS-2$
		) {
			fos.write(cp.getCaCertificate().getEncoded());
		}
	}

}
