/* Copyright (C) 2011 [Gobierno de Espana]
 * This file is part of "Cliente @Firma".
 * "Cliente @Firma" is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * You may contact the copyright holder at: soporte.afirma@seap.minhap.es
 */

package es.gob.afirma.ui.core.jse.certificateselection;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextPane;
import javax.swing.ListCellRenderer;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import es.gob.afirma.core.AOCancelledOperationException;
import es.gob.afirma.core.keystores.KeyUsage;
import es.gob.afirma.core.keystores.NameCertificateBean;
import es.gob.afirma.core.misc.AOUtil;
import es.gob.afirma.core.misc.Platform;

/** Di&aacute;logo de selecci&oacute;n de certificados con est&eacute;tica Windows 7. */
final class CertificateSelectionPanel extends JPanel implements ListSelectionListener {

	private static final long serialVersionUID = 6288294705582545804L;

	private static final String VERDANA_FONT_NAME = "Verdana"; //$NON-NLS-1$

	private static final int TITLE_FONT_SIZE = 14;
	private static final int TEXT_FONT_SIZE = 12;

	private static final Font TITLE_FONT = new Font(VERDANA_FONT_NAME, Font.BOLD, TITLE_FONT_SIZE);
	private static final Font TEXT_FONT = new Font(VERDANA_FONT_NAME, Font.PLAIN, TEXT_FONT_SIZE);

	/** Altura de un elemento de la lista de certificados. */
	private static final int CERT_LIST_ELEMENT_HEIGHT = 86;

	private JList<CertificateLine> certList;

	JScrollPane sPane;

	private JPanel certListPanel;

	private JPanel textMessagePanel;

	private int selectedIndex = -1;

	private NameCertificateBean[] certificateBeans;

	private final String dialogSubHeadline;

	CertificateSelectionPanel(final NameCertificateBean[] el,
			                  final CertificateSelectionDialog selectionDialog,
			                  final String dialogHeadline,
			                  final String dialogSubHeadline,
				              final boolean showControlButons,
				              final boolean allowExternalStores) {

		this.certificateBeans = el == null ? new NameCertificateBean[0] : el.clone();
		this.dialogSubHeadline = dialogSubHeadline;
		createUI(
			selectionDialog,
			dialogHeadline,
			showControlButons,
			allowExternalStores
		);
	}

	private void createUI(final CertificateSelectionDialog selectionDialog,
			              final String dialogHeadline,
			              final boolean showControlButons,
			              final boolean allowExternalStores) {

		setLayout(new GridBagLayout());

		setBackground(Color.WHITE);
		setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));

		final GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(13, 15, 8, 15);
		c.weightx = 1.0;
		c.weighty = 0.0;
		c.gridx = 0;
		c.gridy = 0;

		final JLabel mainMessage = new JLabel(
			dialogHeadline != null ?
				dialogHeadline :
					CertificateSelectionDialogMessages.getString("CertificateSelectionPanel.0") //$NON-NLS-1$
		);
		mainMessage.setFont(TITLE_FONT);
		mainMessage.setForeground(Color.decode("0x0033BC")); //$NON-NLS-1$
		this.add(mainMessage, c);

		c.insets = new Insets(13, 0, 8, 5);
		c.weightx = 0.0;
		c.gridx++;

		if (showControlButons) {
			final JButton refresh = new JButton(
				new ImageIcon(
					CertificateSelectionPanel.class.getResource("/resources/toolbar/ic_autorenew_black_18dp.png"), //$NON-NLS-1$
					CertificateSelectionDialogMessages.getString("UtilToolBar.1") //$NON-NLS-1$
				)
			);
			refresh.setBorder(BorderFactory.createEmptyBorder());
			refresh.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			refresh.getAccessibleContext().setAccessibleDescription(
				CertificateSelectionDialogMessages.getString("UtilToolBar.1") //$NON-NLS-1$
			);
			refresh.setToolTipText(CertificateSelectionDialogMessages.getString("UtilToolBar.1")); //$NON-NLS-1$
			refresh.addActionListener(
				new ActionListener() {
					@Override
					public void actionPerformed(final ActionEvent e) {
						UtilActions.doRefresh(selectionDialog, CertificateSelectionPanel.this);
					}
				}
			);
			refresh.setBackground(Color.WHITE);
			this.add(refresh, c);

			c.gridx++;

			if (allowExternalStores) {
				final JButton open = new JButton(
					new ImageIcon(
						CertificateSelectionPanel.class.getResource("/resources/toolbar/ic_open_in_browser_black_18dp.png"), //$NON-NLS-1$
						CertificateSelectionDialogMessages.getString("UtilToolBar.2") //$NON-NLS-1$
					)
				);
				open.setBorder(BorderFactory.createEmptyBorder());
				open.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				open.getAccessibleContext().setAccessibleDescription(
					CertificateSelectionDialogMessages.getString("UtilToolBar.2") //$NON-NLS-1$
				);
				open.setToolTipText(CertificateSelectionDialogMessages.getString("UtilToolBar.2")); //$NON-NLS-1$
				open.addActionListener(
					new ActionListener() {
						@Override
						public void actionPerformed(final ActionEvent e) {
							UtilActions.doOpen(selectionDialog, CertificateSelectionPanel.this);
						}
					}
				);
				open.setBackground(Color.WHITE);
				this.add(open, c);
			}

			c.insets = new Insets(13, 0, 8, 15);
			c.gridx++;

			final JButton help = new JButton(
				new ImageIcon(
					CertificateSelectionPanel.class.getResource("/resources/toolbar/ic_help_black_18dp.png"), //$NON-NLS-1$
					CertificateSelectionDialogMessages.getString("UtilToolBar.3") //$NON-NLS-1$
				)
			);
			help.setBorder(BorderFactory.createEmptyBorder());
			help.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			help.getAccessibleContext().setAccessibleDescription(
				CertificateSelectionDialogMessages.getString("UtilToolBar.3") //$NON-NLS-1$
			);
			help.setToolTipText(CertificateSelectionDialogMessages.getString("UtilToolBar.3")); //$NON-NLS-1$
			help.addActionListener(
				new ActionListener() {
					@Override
					public void actionPerformed(final ActionEvent e) {
						UtilActions.doHelp();
					}
				}
			);
			help.setBackground(Color.WHITE);
			this.add(help, c);
		}

		c.gridwidth = 4;
		c.insets = new Insets(0, 15, 4, 15);
		c.gridx = 0;
		c.gridy++;

		this.textMessagePanel = new JPanel();
		this.textMessagePanel.setLayout(new GridBagLayout());
		this.textMessagePanel.setOpaque(false);
		this.textMessagePanel.setBorder(null);
		this.add(this.textMessagePanel, c);

		c.insets = new Insets(4, 15, 8, 15);
		c.gridy++;

		this.add(new JSeparator(), c);

		c.insets = new Insets(8, 18, 13, 18);
		c.weighty = 1.0;
		c.gridy++;

		this.certListPanel = new JPanel();
		this.certListPanel.setLayout(new GridBagLayout());
		this.certListPanel.setBorder(null);


		this.certList = new JList<>();
		this.certList.setCellRenderer(new CertListCellRendered());

		updateCertListInfo(this.certificateBeans);

		this.certList.addListSelectionListener(this);
		final CertLinkMouseListener mouseListener = new CertLinkMouseListener();
		this.certList.addMouseMotionListener(mouseListener);
		this.certList.addMouseListener(mouseListener);

		this.add(this.certListPanel, c);
	}

	/** Recarga el di&aacute;logo para mostrar un grupo distinto de certificados.
	 * @param certs Conjunto de datos de los certificados a mostrar. */
	void refresh(final NameCertificateBean[] certs) {

		this.certificateBeans = certs.clone();

		updateCertListInfo(certs);

		// Mostramos y seleccionamos el primer elemento
		if (certs.length > 0) {
			this.certList.setSelectedIndex(0);
			this.sPane.getVerticalScrollBar().setValue(0);
		}
	}

	private static List<CertificateLine> createCertLines(NameCertificateBean[] certBeans) {
		final List<CertificateLine> certLines = new ArrayList<>();
		for (final NameCertificateBean nameCert : certBeans) {
			CertificateLine certLine;
		    try {
		    	certLine = createCertLine(nameCert.getName(), nameCert.getCertificate() );
		    }
		    catch(final Exception e) {
		        continue;
		    }
			certLine.setPreferredSize(new Dimension(0, CERT_LIST_ELEMENT_HEIGHT));
			certLines.add(certLine);
		}
		return certLines;
	}

	/**
	 * Actualiza el texto mostrado en el di&aacute;logo seg&uacute;n el numero de
	 * certificados mostrados y el propio listado de certificados.
	 * @param certs Certificados.
	 */
	private void updateCertListInfo(final NameCertificateBean[] certs) {

		final List<CertificateLine> certLines = createCertLines(certs);

		// Actualizamos el mensaje del dialogo en base al numero de certificados
		// Mostramos un texto de cabecera si corresponde
		this.textMessagePanel.removeAll();
		if (certLines.size() <= 1) {
			String msg;
			if (certLines.size() == 1) {
				msg = this.dialogSubHeadline != null ?
						this.dialogSubHeadline :
							CertificateSelectionDialogMessages.getString("CertificateSelectionPanel.1"); //$NON-NLS-1$
			}
			else {
				msg = CertificateSelectionDialogMessages.getString("CertificateSelectionPanel.8"); //$NON-NLS-1$
			}

			final JTextPane textMessage = new JTextPane();
			textMessage.setOpaque(false);
			textMessage.setText(msg);
			textMessage.setFont(TEXT_FONT);
			textMessage.setBorder(null);
			textMessage.setPreferredSize(new Dimension(370, 40));

			final GridBagConstraints tmC = new GridBagConstraints();
			tmC.fill = GridBagConstraints.BOTH;
			tmC.weightx = 1.0;
			tmC.weighty = 1.0;

			this.textMessagePanel.add(textMessage, tmC);
		}

		// Actualizamos el listado de certificados
		this.certList.setListData(certLines.toArray(new CertificateLine[certLines.size()]));
		this.certList.setVisibleRowCount(Math.max(Math.min(4, certLines.size()), 1));

		if (certLines.size() > 0) {
			this.certList.setSelectedIndex(0);
			this.selectedIndex = 0;
		}
		else {
			this.selectedIndex = -1;
		}

		final JScrollPane jScrollPane = new JScrollPane(
				this.certList,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
			);
		jScrollPane.setBorder(null);

		int dialogHeight = CERT_LIST_ELEMENT_HEIGHT * this.certList.getVisibleRowCount() + 3;
		final Toolkit toolkit = Toolkit.getDefaultToolkit();
		if (toolkit != null) {
			final int screenHeight = (int) toolkit.getScreenSize().getHeight();
			dialogHeight = (int) Math.min(dialogHeight, screenHeight * 0.8);
		}
		jScrollPane.setPreferredSize(new Dimension(500, dialogHeight));

		final GridBagConstraints ic = new GridBagConstraints();
		ic.fill = GridBagConstraints.BOTH;
		ic.weightx = 1.0;
		ic.weighty = 1.0;

		if (this.sPane != null) {
			this.certListPanel.remove(this.sPane);
		}
		this.sPane = jScrollPane;
		this.certListPanel.add(this.sPane, ic);
	}

	/** Selecciona la lista de certificados. */
    void selectCertificateList() {
		this.certList.requestFocusInWindow();
	}

	/** Recupera el alias del certificado seleccionado.
	 * @return Alias del certificado seleccionado o {@code null} si no se seleccion&oacute; ninguno. */
	String getSelectedCertificateAlias() {
		return this.selectedIndex == -1 ? null : this.certificateBeans[this.selectedIndex].getAlias();
	}

	/**
	 * Obtiene el n&uacute;mero de certificados que se muestran al usuario.
	 * @return N&uacute;mero de certificados mostrados.
	 */
	int getShowedCertsCount() {
		return this.certificateBeans == null ? 0 : this.certificateBeans.length;
	}

	/** Agrega un gestor de eventos de rat&oacute;n a la lista de certificados para poder
	 * gestionar a trav&eacute;s de &eacute;l eventos especiales sobre la lista.
	 * @param listener Manejador de eventos de rat&oacute;n. */
	void addCertificateListMouseListener(final MouseListener listener) {
		this.certList.addMouseListener(listener);
	}

	private static CertificateLine createCertLine(final String friendlyName, final X509Certificate cert) {
		final CertificateLine certLine = new CertificateLine(friendlyName, cert);
		certLine.setFocusable(true);
		return certLine;
	}

	private static final class CertificateLine extends JPanel {

		/** Serial Version */
		private static final long serialVersionUID = 5012625058876812352L;

		private static final Font SUBJECT_FONT = new Font(VERDANA_FONT_NAME, Font.BOLD, 14);
		private static final Font DETAILS_FONT = new Font(VERDANA_FONT_NAME, Font.PLAIN, 11);

		private static final long EXPIRITY_WARNING_LEVEL = 1000*60*60*25*7;

		private JLabel propertiesLink = null;

		private final String friendlyName;
		private final X509Certificate cert;

		private static ImageIcon getIcon(final X509Certificate cert) {
			final long notAfter = cert.getNotAfter().getTime();
			final long actualDate = new Date().getTime();
			if (actualDate >= notAfter || actualDate <= cert.getNotBefore().getTime()) {
				return CertificateIconManager.getExpiredIcon(cert);
			}
			if (notAfter - actualDate < EXPIRITY_WARNING_LEVEL) {
				return CertificateIconManager.getWarningIcon(cert);
			}
			return CertificateIconManager.getNormalIcon(cert);
		}

		CertificateLine(final String friendlyName, final X509Certificate cert) {
			this.friendlyName = friendlyName;
			this.cert = cert;
			createUI();
		}

		X509Certificate getCertificate() {
			return this.cert;
		}

		/** {@inheritDoc} */
		@Override
		public String toString() {
			return this.friendlyName;
		}

		private void createUI() {
			setLayout(new GridBagLayout());

			setBackground(Color.WHITE);

			final GridBagConstraints c = new GridBagConstraints();
			c.gridx = 1;
			c.gridy = 1;
			c.gridheight = 4;

			final ImageIcon imageIcon = getIcon(this.cert);
			final JLabel icon = new JLabel(imageIcon);
			setToolTipText(imageIcon.getDescription());

			c.insets = new Insets(2, 2, 2, 5);
			add(icon, c);

			c.fill = GridBagConstraints.HORIZONTAL;
			c.weightx = 1.0;
			c.gridx++;
			c.gridheight = 1;
			c.insets = new Insets(5, 0, 0, 5);

			final JLabel alias = new JLabel(this.friendlyName);
			alias.setFont(SUBJECT_FONT);
			add(alias, c);

			c.gridy++;
			c.insets = new Insets(0, 0, 0, 5);

			final JLabel issuer = new JLabel(
				CertificateSelectionDialogMessages.getString("CertificateSelectionPanel.2") + " " + AOUtil.getCN(this.cert.getIssuerDN().toString()) + //$NON-NLS-1$ //$NON-NLS-2$
					". " + CertificateSelectionDialogMessages.getString("CertificateSelectionPanel.6") + " " + new KeyUsage(this.cert).toString() //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			);
			issuer.setFont(DETAILS_FONT);
			add(issuer, c);

			c.gridy++;

			final JLabel dates = new JLabel(
				CertificateSelectionDialogMessages.getString(
					"CertificateSelectionPanel.3", //$NON-NLS-1$
					new String[] {
						formatDate(this.cert.getNotBefore()),
						formatDate(this.cert.getNotAfter())
					}
				)
			);
			dates.setFont(DETAILS_FONT);
			add(dates, c);

			c.gridy++;

			this.propertiesLink = new JLabel(
		        "<html><u>" + //$NON-NLS-1$
        		CertificateSelectionDialogMessages.getString("CertificateSelectionPanel.5") + //$NON-NLS-1$
		        "</u></html>" //$NON-NLS-1$
	        );
			// Omitimos la muestra de detalles de certificados en OS X porque el SO en vez de mostrar los detalles
			// inicia su importacion
			if (!Platform.OS.MACOSX.equals(Platform.getOS())) {
				this.propertiesLink.setFont(DETAILS_FONT);
				add(this.propertiesLink, c);
			}
		}

		/** Devuelve la fecha con formato.
		 * @param date Fecha.
		 * @return Texto que representativo de la fecha. */
		private static String formatDate(final Date date) {
			return new SimpleDateFormat("dd/MM/yyyy").format(date); //$NON-NLS-1$
		}

		/** Recupera el rect&aacute;ngulo ocupado por el enlace para la carga del certificado.
		 * @return Recuadro con el enlace. */
		Rectangle getCertificateLinkBounds() {
			return this.propertiesLink.getBounds();
		}
	}

	/** Renderer para mostrar la informaci&oacute;n de un certificado. */
	private static final class CertListCellRendered implements ListCellRenderer<CertificateLine> {

		CertListCellRendered() {
			/* Limitamos la visibilidad del constructor */
		}

		/** {@inheritDoc} */
		@Override
		public Component getListCellRendererComponent(final JList<? extends CertificateLine> list, final CertificateLine value,
				final int index, final boolean isSelected, final boolean cellHasFocus) {

			final CertificateLine line = value;
			if (isSelected) {
				line.setBackground(Color.decode("0xD9EAFF")); //$NON-NLS-1$
				line.setBorder(BorderFactory.createCompoundBorder(
						BorderFactory.createLineBorder(Color.WHITE, 1),
						BorderFactory.createLineBorder(Color.decode("0x84ACDD"), 1))); //$NON-NLS-1$

			}
			else {
				line.setBackground(Color.WHITE);
				line.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 4));
			}

			return line;
		}

	}

	/** {@inheritDoc} */
	@Override
	public void valueChanged(final ListSelectionEvent e) {
		this.selectedIndex = this.certList.getSelectedIndex();
	}

	/** Manejador de eventos de raton para la lista de certificados. */
	private final class CertLinkMouseListener extends MouseAdapter {

		private boolean entered = false;

		CertLinkMouseListener() {
			// Vacio
		}

		/** {@inheritDoc} */
		@Override
		public void mouseClicked(final MouseEvent me) {
			final JList<?> tmpList = (JList<?>) me.getSource();
			final CertificateLine tmpLine = (CertificateLine) tmpList.getSelectedValue();
			if (tmpLine != null &&
				me.getClickCount() == 1 &&
						me.getY() < CERT_LIST_ELEMENT_HEIGHT * tmpList.getModel().getSize() &&
					tmpLine.getCertificateLinkBounds().contains(me.getX(), me.getY() % CERT_LIST_ELEMENT_HEIGHT)) {
						try {
							CertificateUtils.openCert(
									CertificateSelectionPanel.this,
									tmpLine.getCertificate());
						}
						catch (final AOCancelledOperationException e) {
							/* No hacemos nada */
						}
			}
		}

		/** {@inheritDoc} */
		@Override
		public void mouseMoved(final MouseEvent me) {
			final JList<?> tmpList = (JList<?>) me.getSource();
			final CertificateLine tmpLine = (CertificateLine) tmpList.getSelectedValue();
			if (tmpLine != null) {
				if (me.getY() < CERT_LIST_ELEMENT_HEIGHT * tmpList.getModel().getSize() &&
						tmpLine.getCertificateLinkBounds().contains(me.getX(), me.getY() % CERT_LIST_ELEMENT_HEIGHT)) {
					if (!this.entered) {
						tmpList.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
						this.entered = true;
					}
				}
				else if (this.entered) {
					tmpList.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					this.entered = false;
				}
			}
		}

	}
}
