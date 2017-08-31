/**
 * ************************************************************************
 * * The contents of this file are subject to the MRPL 1.2
 * * (the  "License"),  being   the  Mozilla   Public  License
 * * Version 1.1  with a permitted attribution clause; you may not  use this
 * * file except in compliance with the License. You  may  obtain  a copy of
 * * the License at http://www.floreantpos.org/license.html
 * * Software distributed under the License  is  distributed  on  an "AS IS"
 * * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * * License for the specific  language  governing  rights  and  limitations
 * * under the License.
 * * The Original Code is FLOREANT POS.
 * * The Initial Developer of the Original Code is OROCUBE LLC
 * * All portions are Copyright (C) 2015 OROCUBE LLC
 * * All Rights Reserved.
 * ************************************************************************
 */
package com.floreantpos.demo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

import net.miginfocom.swing.MigLayout;

import com.floreantpos.Messages;
import com.floreantpos.actions.LogoutAction;
import com.floreantpos.config.TerminalConfig;
import com.floreantpos.main.Application;
import com.floreantpos.model.OrderType;
import com.floreantpos.swing.PosButton;
import com.floreantpos.swing.PosUIManager;
import com.floreantpos.ui.dialog.NumberSelectionDialog2;
import com.floreantpos.ui.dialog.POSMessageDialog;
import com.floreantpos.ui.views.order.RootView;
import com.floreantpos.ui.views.order.ViewPanel;

public class KitchenDisplayView extends ViewPanel implements ActionListener {

	public final static String VIEW_NAME = "KD"; //$NON-NLS-1$

	private static KitchenDisplayView instance;

	private KitchenTicketListPanel ticketPanel;

	private PosButton btnFilter;
	private Timer viewUpdateTimer;
	private PosButton btnLogout;

	private PosButton btnBack;

	private List<String> selectedPrinters;
	private List<OrderType> selectedOrderTypes;

	private boolean showHeader;

	public KitchenDisplayView(boolean showHeader) {
		this.showHeader = showHeader;
		setLayout(new BorderLayout(5, 5));
		JPanel firstTopPanel = new JPanel(new BorderLayout(5, 5));

		btnBack = new PosButton(Messages.getString("KitchenDisplayView.1")); //$NON-NLS-1$
		btnBack.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				RootView.getInstance().showDefaultView();
			}
		});

		btnFilter = new PosButton(Messages.getString("KitchenDisplayView.2")); //$NON-NLS-1$
		btnFilter.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				KitchenFilterDialog dialog = new KitchenFilterDialog();
				dialog.setSelectedPrinters(selectedPrinters);
				dialog.setSelectedOrderTypes(selectedOrderTypes);
				dialog.open();

				if (dialog.isCanceled())
					return;

				ticketPanel.getDataModel().setCurrentRowIndex(0);
				selectedPrinters = dialog.getSelectedPrinters();
				selectedOrderTypes = dialog.getSelectedOrderTypes();

				updateTicketView();
			}
		});

		JPanel topPanel = new JPanel(new MigLayout("right, ins 2 2 0 2,hidemode 3", "", "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		Dimension size = PosUIManager.getSize(60, 40);

		topPanel.add(btnFilter, "w " + size.width + "!,h " + size.height + "!"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		topPanel.setBackground(Color.white);

		PosButton btnOption = new PosButton(Messages.getString("KitchenDisplayView.8")); //$NON-NLS-1$
		btnOption.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				int value = NumberSelectionDialog2.takeIntInput(Messages.getString("KitchenDisplayView.9")); //$NON-NLS-1$
				if (value == -1)
					return;
				TerminalConfig.setKDSTicketsPerPage(value);
				updateTicketView();
			}
		});
		topPanel.add(btnOption, "w " + size.width + "!, h " + size.height + "!"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		topPanel.add(btnBack, "w " + size.width + "!, h " + size.height + "!"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		btnLogout = new PosButton(new LogoutAction(true, false)); //$NON-NLS-1$
		topPanel.add(btnLogout, "w " + size.width + "!, h " + size.height + "!, wrap"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		firstTopPanel.setBackground(Color.white);
		firstTopPanel.setPreferredSize(new Dimension(0, PosUIManager.getSize(50)));
		firstTopPanel.add(topPanel);
		firstTopPanel.add(new JSeparator(), BorderLayout.SOUTH);
		add(firstTopPanel, BorderLayout.NORTH);

		ticketPanel = new KitchenTicketListPanel();
		ticketPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		add(ticketPanel);
		viewUpdateTimer = new Timer(10 * 1000, this);
		viewUpdateTimer.setRepeats(true);
	}

	@Override
	public void setVisible(boolean b) {
		super.setVisible(b);
		if (b) {
			RootView.getInstance().getHeaderPanel().setVisible(showHeader);
			String currentView = TerminalConfig.getDefaultView();
			btnBack.setVisible(currentView != null && !currentView.equals(VIEW_NAME));
			updateTicketView();
			if (!viewUpdateTimer.isRunning()) {
				viewUpdateTimer.start();
			}
		}
		else {
			cleanup();
		}
	}

	public synchronized void cleanup() {
		viewUpdateTimer.stop();
		ticketPanel.reset();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand() != null && e.getActionCommand().equalsIgnoreCase("log out")) { //$NON-NLS-1$
			Application.getInstance().doLogout();
		}
		updateTicketView();
	}

	private synchronized void updateTicketView() {
		try {
			viewUpdateTimer.stop();
			ticketPanel.updateKDSView(selectedPrinters, selectedOrderTypes);
			revalidate();
			repaint();
		} catch (Exception e2) {
			POSMessageDialog.showError(this, e2.getMessage(), e2);
		} finally {
			viewUpdateTimer.restart();
		}
	}

	@Override
	public String getViewName() {
		return VIEW_NAME;
	}

	public synchronized static KitchenDisplayView getInstance() {
		if (instance == null) {
			instance = new KitchenDisplayView(false);
		}
		return instance;
	}
}
